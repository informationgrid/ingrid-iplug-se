/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.client.Client;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcess;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import de.ingrid.iplug.se.nutchController.StatusProvider;
import de.ingrid.iplug.se.nutchController.StatusProvider.State;
import de.ingrid.iplug.se.utils.DBUtils;
import de.ingrid.iplug.se.utils.ElasticSearchUtils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.utils.UrlErrorPagableFilter;
import de.ingrid.iplug.se.webapp.container.Instance;

@RestController
@RequestMapping("/") // in web.xml it is dispatched from "/rest/..."!!!
@SessionAttributes("plugDescription")
public class RestDataController extends InstanceController {

	private static final Log LOG = LogFactory.getLog(RestDataController.class.getName());

    private static final String NO_RESULT_INDEX = "_noresult_";

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

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

		// fix domains without slashes
		try {
			URL tmpUrl = new URL(url.getUrl());
			if (tmpUrl.getPath().isEmpty()) {
				url.setUrl(url.getUrl().concat("/"));
			}
		} catch (MalformedURLException e) {
		}
		List<String> limits = new ArrayList<String>();
		for (String limit : url.getLimitUrls()) {
			try {
				URL tmpUrl = new URL(limit);
				if (tmpUrl.getPath().isEmpty()) {
					limits.add(limit.concat("/"));
				} else {
					limits.add(limit);
				}
			} catch (MalformedURLException e) {
				limits.add(limit);
			}
		}
		url.setLimitUrls(limits);
		List<String> excludes = new ArrayList<String>();
		for (String exclude : url.getExcludeUrls()) {
			try {
				URL tmpUrl = new URL(exclude);
				if (tmpUrl.getPath().isEmpty()) {
					excludes.add(exclude.concat("/"));
				} else {
					excludes.add(exclude);
				}
			} catch (MalformedURLException e) {
				excludes.add(exclude);
			}
		}
		url.setExcludeUrls(excludes);

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

    @SuppressWarnings("unchecked")
	@RequestMapping(value = { "urls/{instance}" }, method = RequestMethod.GET)
	public JSONObject getUrls(@PathVariable("instance") String name,
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
	        @RequestParam(value = "urlfilter", required = false, defaultValue = "") String urlFilter,
	        @RequestParam(value = "metafilter", required = false, defaultValue = "") String[] metaOptions,
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
			criteria.add(criteriaBuilder.like(criteriaBuilder.lower(urlTable.<String> get("url")),
			        "%" + urlFilter.toLowerCase() + "%"));
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

		countQuery.select(criteriaBuilder.count(urlTable)).where(
		        criteriaBuilder.and(criteria.toArray(new Predicate[0])));
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

		List<Url> resultList = em.createQuery(createQuery).setFirstResult(page * pageSize).setMaxResults(pageSize)
		        .getResultList();

		JSONObject json = new JSONObject();
		json.put("data", resultList);
		json.put("totalUrls", count);

		return json;
	}

    @RequestMapping(value = { "urlerrors/{instance}" }, method = RequestMethod.GET)
	public JSONObject getUrlErrors(@PathVariable("instance") String name,
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
	        @RequestParam(value = "urlfilter", required = false, defaultValue = "") String urlFilter,
	        @RequestParam(value = "statusfilter", required = false, defaultValue = "") String[] statusFilter,
	        @RequestParam(value = "sort", required = false, defaultValue = "") int[] sort) {
		// @RequestParam(value = "column[]", required = false, defaultValue =
		// "") List<String> sortColumn) {

		Path path = Paths.get(SEIPlug.conf.getInstancesDir(), name, "statistic", "url_error_report", "data.json");

		JSONParser parser = new JSONParser();
		Reader reader = null;
		UrlErrorPagableFilter pager = null;

		if (path.toFile().exists()) {
    		try {
    			reader = Files.newBufferedReader(path, Charset.forName("UTF-8"));
    			pager = new UrlErrorPagableFilter(page, pageSize, urlFilter, statusFilter);
    			parser.parse(reader, pager);
    		} catch (IOException e) {
    			LOG.error("Error open '" + path.toString() + "'.", e);
    		} catch (ParseException e) {
    			LOG.error("Error parsing JSON File '" + path.toString() + "'.", e);
    		} finally {
    			if (reader != null) {
    				try {
    					reader.close();
    				} catch (IOException e) {
    				}
    			}
    		}
		}

		JSONObject json = new JSONObject();
		if (pager == null) {
			json.put("data", "");
			json.put("totalUrls", 0);
		} else {
			json.put("data", pager.getResult());
			json.put("totalUrls", pager.getTotalResults());
		}

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
	public ResponseEntity<Map<String, String>> toggleInstanceActive(
	        @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject,
	        @PathVariable("name") String name, @PathVariable("value") String value) {


		List<String> activeInstances = JettyStarter.getInstance().config.indexSearchInTypes;
		// always remove type that leads to no result (in case it was set)
		activeInstances.remove( NO_RESULT_INDEX );

		if ("on".equals(value)) {
			activeInstances.add(name);
		} else {
			activeInstances.remove(name);
		}

		// add a type which returns no result, if no instance is activated
		if (activeInstances.size() == 0) {
		    activeInstances.add( NO_RESULT_INDEX );
		}

		// write immediately configuration
		JettyStarter.getInstance().config.writePlugdescriptionToProperties(pdCommandObject);

		return generateOkResponse();
	}

	@RequestMapping(value = "/instance/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteInstance(@PathVariable("id") String name) throws Exception {

        // stop all nutch processes first
        Instance instance = InstanceController.getInstanceData( name );
        nutchController.stop( instance );

        // remove instance directory
        String dir = SEIPlug.conf.getInstancesDir();
        Path directoryToDelete = Paths.get( dir, name );
        try {
            FileUtils.removeRecursive( directoryToDelete );
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<String>( HttpStatus.INTERNAL_SERVER_ERROR );
        }

        // remove instance (type) from index
        Client client = elasticSearch.getObject().client();
        if (ElasticSearchUtils.typeExists( name, client )) {
            ElasticSearchUtils.deleteType( name, client );
        }

        // remove url from database belonging to this instance
        EntityManager em = DBManager.INSTANCE.getEntityManager();

        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Url> criteriaDelete = cb.createCriteriaDelete( Url.class );
        Root<Url> urlTable = criteriaDelete.from(Url.class);
        Predicate instanceCriteria = cb.equal( urlTable.get("instance"), name );

        criteriaDelete.from( Url.class );
        criteriaDelete.where( instanceCriteria );

        em.createQuery( criteriaDelete ).executeUpdate();
        em.flush();
        em.getTransaction().commit();

        return new ResponseEntity<String>( HttpStatus.OK );
    }

	@RequestMapping(value = { "status/{instance}" }, method = RequestMethod.GET)
	public ResponseEntity<Collection<State>> getStatus(@PathVariable("instance") String name) {
		Instance instance = getInstanceData(name);
		NutchProcess nutchProcess = nutchController.getNutchProcess(instance);

		if (nutchProcess == null || (nutchProcess != null && nutchProcess.getState() == Thread.State.TERMINATED)) {
			StatusProvider statusProvider = new StatusProvider(instance.getWorkingDirectory());
			Collection<State> states = statusProvider.getStates();
			// HttpStatus needs to be 200 (OK), otherwise IE 11 won't receive any attached data!!!
			return new ResponseEntity<Collection<State>>(states.isEmpty() ? null : states, HttpStatus.OK);
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
	public ResponseEntity<String> checkUrl(@PathVariable("instance") String instanceName, @RequestBody String urlString)
	        throws IOException, InterruptedException {
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

	public void setElasticSearch(ElasticsearchNodeFactoryBean esBean) {
        this.elasticSearch = esBean;
    }

  @RequestMapping(value = "/updateMetadata", method = RequestMethod.POST)
  public ResponseEntity<Map<String, String>> updateMetadataConfig(@RequestParam("instance") String name, @RequestBody String json) throws IOException {
      String confFile = SEIPlug.conf.getInstancesDir() + "/" + name + "/conf/urlMaintenance.json";

      // check if json can be converted correctly
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      UrlMaintenanceSettings settings = gson.fromJson(json, UrlMaintenanceSettings.class);

      Map<String, String> result = new HashMap<String, String>();
      // only write then json content to file
      if (settings != null) {
          // File fos = new File( SEIPlug.conf.getInstancesDir() + "/" + name
          // + "/conf/urlMaintenance.json" );
          // BufferedWriter writer = new BufferedWriter( new FileWriter( fos )
          // );
          // writer.write( json );
          // writer.close();
          Writer out = new FileWriter(confFile);
          gson.toJson(settings, out);
          out.close();
          result.put("result", "OK");
          return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
      }
      result.put("result", "Error");
      return new ResponseEntity<Map<String, String>>(result, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
