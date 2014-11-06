package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcess;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import de.ingrid.iplug.se.nutchController.StatusProvider;
import de.ingrid.iplug.se.nutchController.StatusProvider.State;
import de.ingrid.iplug.se.utils.DBUtils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;

@RestController
@RequestMapping("/rest")
@SessionAttributes("plugDescription")
public class RestDataController extends InstanceController {

    @Autowired
    private NutchController nutchController;

    @RequestMapping(value = { "/test" }, method = RequestMethod.GET)
    public String test() {
        return "OK";
    }

    @RequestMapping(value = { "url/{id}" }, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Url> getUrl(@PathVariable("id") Long id) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        Url url = em.find(Url.class, id);

        return new ResponseEntity<Url>(url, url != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = { "url" }, method = RequestMethod.POST)
    public ResponseEntity<Url> addUrl(@RequestBody Url url) {
        DBUtils.addUrl(url);
        return new ResponseEntity<Url>(url, HttpStatus.OK);
    }

    @RequestMapping(value = { "url/{id}" }, method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, String>> deleteUrl(@PathVariable("id") Long id) {
        return deleteUrls(new Long[] { id });
    }

    @RequestMapping(value = { "urls" }, method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, String>> deleteUrls(@RequestBody Long[] ids) {
        DBUtils.deleteUrls(ids);
        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "OK");
        return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
    }

    @RequestMapping(value = { "urls/{instance}" }, method = RequestMethod.GET)
    public JSONObject getUrls(@PathVariable("instance") String name, @RequestParam(value = "page", required = false, defaultValue = "0") int page, @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "urlfilter", required = false, defaultValue = "") String urlFilter, @RequestParam(value = "metafilter", required = false, defaultValue = "") String[] metaOptions,
            @RequestParam(value = "sort", required = false, defaultValue = "") int[] sort) {
        // @RequestParam(value = "column[]", required = false, defaultValue =
        // "") List<String> sortColumn) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Url> createQuery = criteriaBuilder.createQuery(Url.class);
        Root<Url> urlTable = createQuery.from(Url.class);

        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

        List<Predicate> criteria = new ArrayList<Predicate>();

        // filter by instance
        criteria.add(criteriaBuilder.equal(urlTable.<String> get("instance"), name));

        // filter URL (case-insensitive)
        if (!urlFilter.isEmpty()) {
            criteria.add(criteriaBuilder.like(criteriaBuilder.lower(urlTable.<String> get("url")), "%" + urlFilter.toLowerCase() + "%"));
        }

        // filter metadata
        for (String meta : metaOptions) {
            Join<Url, Metadata> j = urlTable.join("metadata", JoinType.LEFT);
            String[] metaSplit = meta.split(":");
            if (metaSplit.length == 2) {
                criteria.add(criteriaBuilder.equal(j.get("metaKey"), metaSplit[0]));
                criteria.add(criteriaBuilder.equal(j.get("metaValue"), metaSplit[1]));
            }
        }

        countQuery.select(criteriaBuilder.count(urlTable)).where(criteriaBuilder.and(criteria.toArray(new Predicate[0])));
        Long count = em.createQuery(countQuery).getSingleResult();

        createQuery.select(urlTable).where(criteriaBuilder.and(criteria.toArray(new Predicate[0])));

        // sort if necessary
        if (sort.length == 2) {
            Expression<?> column = getColumnForSort(urlTable, sort[0]);
            if (column != null) {
                if (sort[1] == 0) {
                    createQuery.orderBy(criteriaBuilder.desc(column));
                } else {
                    createQuery.orderBy(criteriaBuilder.asc(column));
                }
            }
        }

        List<Url> resultList = em.createQuery(createQuery).setFirstResult(page * pageSize).setMaxResults(pageSize).getResultList();

        JSONObject json = new JSONObject();
        json.put("data", resultList);
        json.put("totalUrls", count);

        return json;
    }

    private Expression<?> getColumnForSort(Root<Url> urlTable, int columnPos) {
        switch (columnPos) {
        case 1:
            return urlTable.get("url");
        case 2:
            return urlTable.get("status");
        }
        return null;
    }

    @RequestMapping(value = { "instance/{name}/{value}" }, method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> toggleInstanceActive(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject, @PathVariable("name") String name, @PathVariable("value") String value) {

        List<String> activeInstances = SEIPlug.conf.activeInstances;
        if ("on".equals(value)) {
            activeInstances.add(name);
        } else {
            activeInstances.remove(name);
        }

        // write immediately configuration
        JettyStarter.getInstance().config.writePlugdescriptionToProperties(pdCommandObject);

        return generateOkResponse();
    }

    @RequestMapping(value = { "status/{instance}" }, method = RequestMethod.GET)
    public ResponseEntity<Collection<State>> getStatus(@PathVariable("instance") String name) {
        Instance instance = getInstanceData(name);
        NutchProcess nutchProcess = nutchController.getNutchProcess(instance);

        if (nutchProcess == null || (nutchProcess != null && nutchProcess.getState() == Thread.State.TERMINATED)) {
            StatusProvider statusProvider = new StatusProvider(instance.getWorkingDirectory());
            Collection<State> states = statusProvider.getStates();
            return new ResponseEntity<Collection<State>>(states.isEmpty() ? null : states, HttpStatus.FOUND);
        }

        return new ResponseEntity<Collection<State>>(nutchProcess.getStatusProvider().getStates(), HttpStatus.OK);
    }

    @RequestMapping(value = { "status/{instance}/statistic" }, method = RequestMethod.GET)
    public ResponseEntity<String> getStatistic(@PathVariable("instance") String name) throws IOException {

        Path path = Paths.get(SEIPlug.conf.getInstancesDir(), name, "statistic", "host", "crawldb");
        String content = FileUtils.readFile(path);

        return new ResponseEntity<String>(content, HttpStatus.OK);
    }

    @RequestMapping(value = { "status/{instance}/hadoop" }, method = RequestMethod.GET)
    public ResponseEntity<String> getHadoopLog(@PathVariable("instance") String name) throws IOException {

        Path path = Paths.get(SEIPlug.conf.getInstancesDir(), name, "logs", "hadoop.log");
        String content = FileUtils.tail(path.toFile(), 1000);

        return new ResponseEntity<String>(content, HttpStatus.OK);
    }

    @RequestMapping(value = { "url/{instance}/check" }, method = RequestMethod.POST)
    public ResponseEntity<String> checkUrl(@PathVariable("instance") String instanceName, @RequestBody String urlString) throws IOException, InterruptedException {
        Instance instance = getInstanceData(instanceName);

        NutchProcess process = NutchProcessFactory.getUrlTesterProcess(instance, urlString);
        process.start();

        long start = System.currentTimeMillis();
        boolean timeout = true;
        while ((System.currentTimeMillis() - start) < 30000) {
            Thread.sleep(1000);
            if (process.getStatus() != NutchProcess.STATUS.RUNNING) {
                timeout = false;
                break;
            }
        }
        String result = process.getConsoleOutput();
        if (timeout) {
            if (process.getStatus() == NutchProcess.STATUS.RUNNING) {
                process.stopExecution();
                result = "Timeout (30 sec)";
            }
        }

        return new ResponseEntity<String>(result, HttpStatus.OK);
    }

    private ResponseEntity<Map<String, String>> generateOkResponse() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("result", "OK");
        return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
    }
}