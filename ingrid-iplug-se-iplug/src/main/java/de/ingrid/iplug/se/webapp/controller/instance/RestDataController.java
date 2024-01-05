/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.webapp.controller.instance;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.ingrid.admin.Config;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.elasticsearch.*;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.InstanceAdmin;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcess;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import de.ingrid.utils.statusprovider.StatusProvider;
import de.ingrid.utils.statusprovider.StatusProvider.State;
import de.ingrid.iplug.se.utils.DBUtils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.utils.UrlErrorPagableFilter;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.utils.IngridCall;
import de.ingrid.utils.tool.UrlTool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import java.util.*;

@RestController
@RequestMapping("/rest") // in web.xml it is dispatched from "/rest/..."!!!
@SessionAttributes("plugDescription")
public class RestDataController extends InstanceController {

	private static final Log LOG = LogFactory.getLog(RestDataController.class.getName());

    private static final String NO_RESULT_INDEX = "_noresult_";

    @Autowired
    private NutchProcessFactory nutchProcessFactory;

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

	@Autowired
	private NutchController nutchController;

	private IIndexManager indexManager;

	@Autowired
	private Config baseConfig;

	@Autowired
	private Configuration seConfig;

	private final InMemoryUserDetailsManager userDetailsManager;

	@Autowired
	private CommunicationService _communicationInterface;

	@Autowired
    private StatusProviderService statusProviderService;

	@Autowired
	public RestDataController(IndexManager indexManager, IBusIndexManager iBusIndexManager, ElasticConfig elasticConfig, InMemoryUserDetailsManager userDetailsManager) {
		this.indexManager = elasticConfig.esCommunicationThroughIBus ? iBusIndexManager : indexManager;
		this.userDetailsManager = userDetailsManager;
		addAdminUsers();
	}

	private void addAdminUsers() {
		Map<String, Object> adminsFromDatabase = getAdminsFromDatabase(null, 0, 1000, null);
		List<InstanceAdmin> hits = (List<InstanceAdmin>) adminsFromDatabase.get("hits");
		hits.forEach(hit -> {
			String pw_hash = BCrypt.hashpw(hit.getPassword(), BCrypt.gensalt());
			userDetailsManager.createUser(createUserDetails(hit.getLogin(), pw_hash));
		});
	}

	private static UserDetails createUserDetails(String login, String pw_hash) {
		return User.withUsername(login)
				.password(pw_hash)
				.roles("instanceAdmin")
				.build();
	}

	@RequestMapping(value = { "/test" }, method = RequestMethod.GET)
	public String test() {
		return "OK";
	}


    @RequestMapping(value = { "admin/{id}" }, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<InstanceAdmin> getInstanceAdmin(@PathVariable("id") Long id) {
        InstanceAdmin admin = getUserFromDatabase(id);

        return new ResponseEntity<>(admin, admin != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
	
	private InstanceAdmin getUserFromDatabase(Long id) {
		EntityManager em = DBManager.INSTANCE.getEntityManager();
		return em.find(InstanceAdmin.class, id);
	}

    @RequestMapping(value = { "admin/{instance}" }, method = RequestMethod.POST)
    public ResponseEntity<?> addAdmin(@PathVariable("instance") String name, @RequestBody InstanceAdmin admin, HttpServletRequest request, HttpServletResponse response) {
        DBUtils.addAdmin(admin);
		String pw_hash = BCrypt.hashpw(admin.getPassword(), BCrypt.gensalt());
		userDetailsManager.createUser(createUserDetails(admin.getLogin(), pw_hash));
        return new ResponseEntity<>(admin, HttpStatus.OK);
    }

    @RequestMapping(value = { "isduplicateadmin/{instance}/{login}" }, method = RequestMethod.GET)
    public ResponseEntity<String> isDuplicateAdmin(@PathVariable("instance") String name, @PathVariable("login") String login) {
        if (DBUtils.isAdminForInstance(login, name)) {
            return new ResponseEntity<>("true", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("false", HttpStatus.OK);
        }
    }

    @RequestMapping(value = { "admin/{id}" }, method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, String>> deleteAdmin(@PathVariable("id") Long id) {
        return deleteAdmins(new Long[] { id });
    }

    @RequestMapping(value = { "admins" }, method = RequestMethod.DELETE)
    public ResponseEntity<Map<String, String>> deleteAdmins(@RequestBody Long[] ids) {
		for (Long id : ids) {
			InstanceAdmin user = getUserFromDatabase(id);
			userDetailsManager.deleteUser(user.getLogin());
		}
        DBUtils.deleteAdmins(ids);
        Map<String, String> result = new HashMap<>();
        result.put("result", "OK");
        return new ResponseEntity<>(result, HttpStatus.OK);
    }	
	
    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "admins/{instance}" }, method = RequestMethod.GET)
    public JSONObject getAdmins(@PathVariable("instance") String name,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
            @RequestParam(value = "sort", required = false, defaultValue = "") int[] sort, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // @RequestParam(value = "column[]", required = false, defaultValue =
        // "") List<String> sortColumn) {
        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }
        Map<String, Object> resultList = getAdminsFromDatabase(name, page, pageSize, sort);

        JSONObject json = new JSONObject();
        json.put("data", resultList.get("hits"));
        json.put("totalAdmins", resultList.get("count"));

        return json;
    }
	
	private Map<String, Object> getAdminsFromDatabase(String instanceName, int page, int pageSize, int[] sort) {
		EntityManager em = DBManager.INSTANCE.getEntityManager();

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<InstanceAdmin> createQuery = criteriaBuilder.createQuery(InstanceAdmin.class);
		Root<InstanceAdmin> adminTable = createQuery.from(InstanceAdmin.class);

		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

		List<Predicate> criteria = new ArrayList<>();

		// filter by instance
		if (instanceName != null) {
			criteria.add(criteriaBuilder.equal(adminTable.<String>get("instance"), instanceName));
		}

		countQuery.select(criteriaBuilder.count(adminTable)).where(
				criteriaBuilder.and(criteria.toArray(new Predicate[0])));
		Long count = em.createQuery(countQuery).getSingleResult();

		createQuery.select(adminTable).where(criteriaBuilder.and(criteria.toArray(new Predicate[0])));

		// sort if necessary
		if (sort != null && sort.length == 2) {
			Expression<?> column = getColumnForSortAdminTable(adminTable, sort[0]);
			if (column != null) {
				if (sort[1] == 0) {
					createQuery.orderBy(criteriaBuilder.desc(column));
				} else {
					createQuery.orderBy(criteriaBuilder.asc(column));
				}
			}
		}

		Map<String, Object> result = new HashMap<>();
		result.put("hits", em.createQuery(createQuery).setFirstResult(page * pageSize).setMaxResults(pageSize)
				.getResultList());
		result.put("count", count);
		return result;
	}

    @RequestMapping(value = { "url/{id}" }, method = RequestMethod.GET, produces = "application/json")
	public ResponseEntity<Url> getUrl(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
		EntityManager em = DBManager.INSTANCE.getEntityManager();
		Url url = em.find(Url.class, id);

		return new ResponseEntity<>(url, url != null ? HttpStatus.OK : HttpStatus.NOT_FOUND);
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
		List<String> limits = new ArrayList<>();
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
		List<String> excludes = new ArrayList<>();
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
		return new ResponseEntity<>(url, HttpStatus.OK);
	}

	@RequestMapping(value = { "url/{id}" }, method = RequestMethod.DELETE)
	public ResponseEntity<Map<String, String>> deleteUrl(@PathVariable("id") Long id, HttpServletRequest request, HttpServletResponse response) {
		return deleteUrls(new Long[] { id });
	}

	@RequestMapping(value = { "urls" }, method = RequestMethod.DELETE)
	public ResponseEntity<Map<String, String>> deleteUrls(@RequestBody Long[] ids) {
		DBUtils.deleteUrls(ids);
		Map<String, String> result = new HashMap<>();
		result.put("result", "OK");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

    @SuppressWarnings("unchecked")
	@RequestMapping(value = { "urls/{instance}" }, method = RequestMethod.GET)
	public JSONObject getUrls(@PathVariable("instance") String name,
	        @RequestParam(value = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(value = "pagesize", required = false, defaultValue = "10") int pageSize,
	        @RequestParam(value = "urlfilter", required = false, defaultValue = "") String urlFilter,
	        @RequestParam(value = "metafilter", required = false, defaultValue = "") String[] metaOptions,
	        @RequestParam(value = "sort", required = false, defaultValue = "") int[] sort, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// @RequestParam(value = "column[]", required = false, defaultValue =
		// "") List<String> sortColumn) {
        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }

        EntityManager em = DBManager.INSTANCE.getEntityManager();

		CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		CriteriaQuery<Url> createQuery = criteriaBuilder.createQuery(Url.class);
		Root<Url> urlTable = createQuery.from(Url.class);

		CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);

		List<Predicate> criteria = new ArrayList<>();

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
	        @RequestParam(value = "sort", required = false, defaultValue = "") int[] sort, HttpServletRequest request, HttpServletResponse response) throws IOException {
		// @RequestParam(value = "column[]", required = false, defaultValue =
		// "") List<String> sortColumn) {

        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }
		Path path = Paths.get(seConfig.getInstancesDir(), name, "statistic", "url_error_report", "data.json");

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

    private Expression<?> getColumnForSortAdminTable(Root<InstanceAdmin> adminTable, int columnPos) {
        switch (columnPos) {
        case 1:
            return adminTable.get("login");
        }
        return null;
    }

	@RequestMapping(value = { "instance/{name}/{value}" }, method = RequestMethod.POST)
	public ResponseEntity<Map<String, String>> toggleInstanceActive(
	        @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject,
	        @PathVariable("name") String name, @PathVariable("value") String value, HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }

        // activate/deactivate instance via iBus
		toggleIndexInIBus(name, "on".equals(value));

		return generateOkResponse();
	}

	private void toggleIndexInIBus(String name, boolean activate) throws Exception {
		IngridCall ingridCall = new IngridCall();
		ingridCall.setTarget("iBus");
		ingridCall.setParameter(SEIPlug.baseConfig.uuid + "=>" + SEIPlug.baseConfig.index + "_" + name + ":default");

		if (activate) {
			ingridCall.setMethod("activateIndex");
		} else {
			ingridCall.setMethod("deactivateIndex");
		}
		_communicationInterface.getIBus().call(ingridCall);
	}

	@RequestMapping(value = "/instance/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteInstance(@PathVariable("id") String name, HttpServletRequest request, HttpServletResponse response) throws Exception {

        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }

        // stop all nutch processes first
        Instance instance = InstanceController.getInstanceData( name );
        nutchController.stop( instance );

        // remove instance directory
        String dir = seConfig.getInstancesDir();
        Path directoryToDelete = Paths.get( dir, name );
        try {
            FileUtils.removeRecursive( directoryToDelete );
        } catch (IOException e) {
			LOG.error("Error deleting instance", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // remove instance index
		String indexName = baseConfig.index + "_" + name;

		try {
			if (indexManager.indexExists(indexName)) {
				indexManager.deleteIndex(indexName);
			}
		} catch (Exception e) {
			LOG.error("Error removing index: " + indexName, e);
		}


        // remove url from database belonging to this instance
        EntityManager em = DBManager.INSTANCE.getEntityManager();

        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Url> criteriaDeleteUrls = cb.createCriteriaDelete( Url.class );
        Root<Url> urlTable = criteriaDeleteUrls.from(Url.class);
        Predicate instanceCriteria = cb.equal( urlTable.get("instance"), name );

        criteriaDeleteUrls.from( Url.class );
        criteriaDeleteUrls.where( instanceCriteria );

        em.createQuery( criteriaDeleteUrls ).executeUpdate();

        CriteriaDelete<InstanceAdmin> criteriaDeleteInstanceAdmins = cb.createCriteriaDelete( InstanceAdmin.class );
        Root<InstanceAdmin> adminTable = criteriaDeleteInstanceAdmins.from(InstanceAdmin.class);
        instanceCriteria = cb.equal( urlTable.get("instance"), name );

        criteriaDeleteInstanceAdmins.from( InstanceAdmin.class );
        criteriaDeleteInstanceAdmins.where( instanceCriteria );

        em.createQuery( criteriaDeleteInstanceAdmins ).executeUpdate();

        em.flush();
        em.getTransaction().commit();

        return new ResponseEntity<>(HttpStatus.OK);
    }

	@RequestMapping(value = { "status/{instance}" }, method = RequestMethod.GET)
	public ResponseEntity<Collection<State>> getStatus(@PathVariable("instance") String name, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
	    if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }

        Instance instance = getInstanceData(name);
		NutchProcess nutchProcess = nutchController.getNutchProcess(instance);

		if (nutchProcess == null || (nutchProcess != null && nutchProcess.getState() == Thread.State.TERMINATED)) {
			StatusProvider statusProvider = new StatusProvider(instance.getWorkingDirectory());
			Collection<State> states = statusProvider.getStates();
			// HttpStatus needs to be 200 (OK), otherwise IE 11 won't receive any attached data!!!
			return new ResponseEntity<>(states.isEmpty() ? null : states, HttpStatus.OK);
		}

		return new ResponseEntity<>(nutchProcess.getStatusProvider().getStates(), HttpStatus.OK);
	}

	@RequestMapping(value = { "status/{instance}/statistic" }, method = RequestMethod.GET)
	public ResponseEntity<String> getStatistic(@PathVariable("instance") String name, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }
		Path path = Paths.get(seConfig.getInstancesDir(), name, "statistic", "host", "crawldb");
		String content = FileUtils.readFile(path);

		return new ResponseEntity<>(content, HttpStatus.OK);
	}

	@RequestMapping(value = { "status/{instance}/hadoop" }, method = RequestMethod.GET)
	public ResponseEntity<String> getHadoopLog(@PathVariable("instance") String name, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }
		Path path = Paths.get(seConfig.getInstancesDir(), name, "logs", "hadoop.log");
		String content = FileUtils.tail(path.toFile(), 1000);

		return new ResponseEntity<>(content, HttpStatus.OK);
	}

	@RequestMapping(value = { "status/{instance}/import_log" }, method = RequestMethod.GET)
	public ResponseEntity<String> getImportLog(@PathVariable("instance") String name, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (hasNoAccessToInstance(name, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }
		Path path = Paths.get(SEIPlug.conf.getInstancesDir(), name, "logs", "import.log");
		String content = FileUtils.tail(path.toFile(), 1000);

		return new ResponseEntity<>(content, HttpStatus.OK);
	}

	@RequestMapping(value = { "url/{instance}/check" }, method = RequestMethod.POST)
	public ResponseEntity<String> checkUrl(@PathVariable("instance") String instanceName, @RequestBody String urlString, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
        if (hasNoAccessToInstance(instanceName, request, response)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }

        Instance instance = getInstanceData(instanceName);

		NutchProcess process = nutchProcessFactory.getUrlTesterProcess(instance, UrlTool.getEncodedUnicodeUrl(urlString));
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

		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	private ResponseEntity<Map<String, String>> generateOkResponse() {
		Map<String, String> result = new HashMap<>();
		result.put("result", "OK");
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	public void setElasticSearch(ElasticsearchNodeFactoryBean esBean) {
        this.elasticSearch = esBean;
    }

  @RequestMapping(value = "/updateMetadata", method = RequestMethod.POST)
  public ResponseEntity<Map<String, String>> updateMetadataConfig(@RequestParam("instance") String name, @RequestBody String json, HttpServletRequest request, HttpServletResponse response) throws IOException {
      if (hasNoAccessToInstance(name, request, response)) {
          response.sendError(HttpStatus.FORBIDDEN.value());
          return null;
      }
      String confFile = seConfig.getInstancesDir() + "/" + name + "/conf/urlMaintenance.json";

      // check if json can be converted correctly
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      UrlMaintenanceSettings settings = gson.fromJson(json, UrlMaintenanceSettings.class);

      Map<String, String> result = new HashMap<>();
      // only write then json content to file
      if (settings != null) {
          // File fos = new File( seConfig.getInstancesDir() + "/" + name
          // + "/conf/urlMaintenance.json" );
          // BufferedWriter writer = new BufferedWriter( new FileWriter( fos )
          // );
          // writer.write( json );
          // writer.close();
          Writer out = new FileWriter(confFile);
          gson.toJson(settings, out);
          out.close();
          result.put("result", "OK");
          return new ResponseEntity<>(result, HttpStatus.OK);
      }
      result.put("result", "Error");
      return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
