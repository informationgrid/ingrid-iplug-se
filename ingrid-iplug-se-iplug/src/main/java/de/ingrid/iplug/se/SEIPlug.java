/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
/*
 * Copyright (c) 1997-2006 by wemove GmbH
 */
package de.ingrid.iplug.se;

import de.ingrid.admin.Config;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.instance.InstanceController;
import de.ingrid.utils.*;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.tool.QueryUtil;
import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.h2.tools.Recover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Search Engine iPlug to crawl websites and prepare for InGrid Portal.
 *
 * @author joachim@wemove.com
 */
@ImportResource({"/springapp-servlet.xml", "/override/*.xml"})
@SpringBootApplication(scanBasePackages = "de.ingrid")
@ComponentScan(
        basePackages = "de.ingrid",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.object.DefaultDataType"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.object.BasePlug"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.BaseWebappApplication"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.controller.RedirectController"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.controller.SchedulingController"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.ingrid.admin.metadata.DefaultIPlugOperatorInjectorAdapter"),
        })
public class SEIPlug extends HeartBeatPlug {

    /**
     * The logging object
     */
    private static Logger log = Logger.getLogger(SEIPlug.class);

    public static Configuration conf;

    /**
     * The <code>PlugDescription</code> object passed at startup
     */
    private PlugDescription fPlugDesc = null;

    /**
     * Workingdirectory of the iPlug instance as absolute path
     */
    // private String fWorkingDir = ".";

    /**
     * Unique Plug-iD
     */
    private String fPlugId = null;

    // private static final long serialVersionUID =
    // SEIPlug.class.getName().hashCode();

    @Autowired
    private IndexImpl index;

    @Autowired
    private ElasticConfig elasticConfig;

    @Autowired
    private IBusIndexManager iBusIndexManager;

    private Configuration seConfig;

//    private static ElasticsearchNodeFactoryBean esBean;

//    private static NutchController nutchController;

    private static EntityManager em;

    public static Config baseConfig;

    public SEIPlug() {
        super(30000, null, null, null, null);
    };

    @Autowired
    public SEIPlug(IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors,
                   Configuration seConfig, Config baseConfig) throws SQLException {
        super(30000, new PlugDescriptionFieldFilters(), injector, preProcessors, postProcessors);
//        SEIPlug.esBean = esBean;
//        SEIPlug.nutchController = nutchController;
        this.seConfig = seConfig;
        conf = seConfig;

        SEIPlug.baseConfig = baseConfig;
        try {
            baseConfig.initialize();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (seConfig != null) {
            seConfig.initialize();
        } else {
            log.info("No external configuration found.");
        }

        init();
    }

    private void init() throws SQLException {
        // set the directory of the database to the configured one
        Map<String, String> properties = new HashMap<String, String>();
        Path dbDir = Paths.get(seConfig.databaseDir);
        properties.put("javax.persistence.jdbc.url", "jdbc:h2:" + dbDir.toFile().getAbsolutePath() + "/urls;MVCC=true;AUTO_SERVER=TRUE");

        // get an entity manager instance (initializes properties in the
        // DBManager)
        EntityManagerFactory emf = null;
        // for development use the settings from the persistence.xml
        if ("iplug-se-dev".equals(seConfig.databaseID)) {
            emf = Persistence.createEntityManagerFactory(seConfig.databaseID);
        } else {
            emf = Persistence.createEntityManagerFactory(seConfig.databaseID, properties);
        }
        DBManager.INSTANCE.intialize(emf);
        em = null;
        try {
            em = DBManager.INSTANCE.getEntityManager();
        } catch( PersistenceException e) {
            log.error( "Database seems to be corrupt. Starting recovery process ..." );
            Recover.main( "-dir", dbDir.toString() );
            log.error( "Done. Please execute SQL file manually." );
            System.exit( -1 );
        }

        // apply test-data during development
        if ("iplug-se-dev".equals(seConfig.databaseID)) {
            setupTestData(em);

        } else {
            // do database migrations
            Flyway flyway = new Flyway();
            String dbUrl = DBManager.INSTANCE.getProperty("javax.persistence.jdbc.url").toString();
            flyway.setDataSource(dbUrl, "", "");
            flyway.setInitOnMigrate( true );
            try {
                flyway.migrate();
            } catch (Exception ex) {
                log.error( "Error migrating the database:", ex );
            }
        }
    }

    /**
     * @see de.ingrid.utils.IPlug#configure(de.ingrid.utils.PlugDescription)
     */
    @Override
    public void configure(PlugDescription plugDescription) {
        super.configure(plugDescription);
        log.info("(Re-)Configuring iPlug-SE ...");

        this.fPlugDesc = plugDescription;
        this.fPlugId = fPlugDesc.getPlugId();

        // TODO: add plug id as index information
        // ...

        log.info("Done.");
    }

    /**
     * @see de.ingrid.utils.IPlug#close()
     */
    public void close() {}

    /**
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery,
     *      int, int)
     */
    public IngridHits search(IngridQuery query, int start, int length) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("incoming query : " + query.toString());
        }

        // check if query is rejected and return 0 hits instead of search within
        // the iplug
        if (query.isRejected()) {
            return new IngridHits(fPlugId, 0, new IngridHit[] {}, true);
        }

        // remove "meta" field from query so search works !
        QueryUtil.removeFieldFromQuery(query, QueryUtil.FIELDNAME_METAINFO);
        QueryUtil.removeFieldFromQuery(query, QueryUtil.FIELDNAME_INCL_META);

        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {

            ClauseQuery cq = new ClauseQuery(true, false);
            cq.addField(new FieldQuery(true, false, "iPlugId", elasticConfig.communicationProxyUrl));
            query.addClause(cq);
            return this.iBusIndexManager.search(query, start, length);
        }


        preProcess(query);

        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setToAlias(baseConfig.index + "_*");
        indexInfo.setToIndex(baseConfig.index + "_*");
        elasticConfig.activeIndices = new IndexInfo[] {indexInfo};

        return index.search(query, start, length);

    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetail(de.ingrid.utils.IngridHit,
     *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
     */
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] requestedFields) throws Exception {

        preProcess(query);

        // request iBus directly to get search results from within this iPlug
        // adapt query to only get results coming from this iPlug and activated in iBus
        // But when not connected to an iBus then use direct connection to Elasticsearch
        if (elasticConfig.esCommunicationThroughIBus) {
            return this.iBusIndexManager.getDetail(hit, query, requestedFields);
        }

        return index.getDetail(hit, query, requestedFields);
    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit[],
     *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
     */
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] requestedFields) throws Exception {

        preProcess(query);

        IngridHitDetail[] detailHits = new IngridHitDetail[hits.length];

        for (int i = 0; i < hits.length; i++) {
            detailHits[i] = getDetail(hits[i], query, requestedFields);

        }
        return detailHits;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SEIPlug.class, args);

        // normally shutdown the elastic search node and stop all running
        // nutch processes
/*        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    esBean.destroy();
                    File[] instancesDirs = FileUtils.getInstancesDirs();
                    for (File subDir : instancesDirs) {
                        Instance instance = InstanceController.getInstanceData( subDir.getName() );
                        nutchController.stop( instance );
                        em.close();
                    }

                } catch (Exception e) {
                    log.error("Error shutting down", e);
                }
            }
        })*/;
    }

    private static void setupTestData(EntityManager em) {
        em.getTransaction().begin();

        // check first if test data already has been added
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Url> criteria = cb.createQuery( Url.class );
        Root<Url> urlTable = criteria.from(Url.class);
        Predicate instanceCriteria = cb.equal( urlTable.get("instance"), "catalog" );

        criteria.from( Url.class );
        criteria.where( instanceCriteria );

        int size = em.createQuery( criteria ).getResultList().size();

        if (size == 0) {

            Url url = new Url("catalog");
            url.setStatus("200");
            url.setUrl("http://www.wemove.com/");
            List<Metadata> metadata = new ArrayList<Metadata>();
            Metadata m1 = new Metadata();
            m1.setMetaKey("lang");
            m1.setMetaValue("en");
            Metadata m2 = new Metadata();
            m2.setMetaKey("topic");
            m2.setMetaValue("t2");
            Metadata m3 = new Metadata();
            m3.setMetaKey("topic");
            m3.setMetaValue("t3");
            Metadata m4 = new Metadata();
            m4.setMetaKey("unknown");
            m4.setMetaValue("xxx");
            Metadata m5 = new Metadata();
            m5.setMetaKey("topic");
            m5.setMetaValue("angularjs");
            metadata.add(m1);
            metadata.add(m2);
            metadata.add(m3);
            metadata.add(m4);
            metadata.add(m5);
            url.setMetadata(metadata);
            List<String> limitUrls = new ArrayList<String>();
            limitUrls.add("http://www.wemove.com/");
            url.setLimitUrls(limitUrls);
            List<String> excludeUrls = new ArrayList<String>();
            excludeUrls.add("http://www.wemove.com/about");
            url.setExcludeUrls(excludeUrls);

            em.persist(url);

            String[] urls = new String[] { "http://www.spiegel.de", "http://www.heise.de", "http://www.apple.com", "http://www.engadget.com", "http://www.tagesschau.de", "http://www.home-mag.com/", "http://www.ultramusicfestival.com/",
                    "http://www.ebook.de/de/", "http://www.audible.de", "http://www.amazon.com", "http://www.powerint.com/", "http://www.tanzkongress.de/", "http://www.thesourcecode.de/", "http://werk-x.at/", "http://keinundapel.com/",
                    "http://www.ta-trung.com/", "http://www.attac.de/", "http://www.altana-kulturstiftung.de/", "http://www.lemagazinedouble.com/", "http://www.montessori-muehlheim.de/", "http://missy-magazine.de/",
                    "http://www.eh-darmstadt.de/", "http://herbert.de/", "http://www.mousonturm.de/", "http://www.zeit.de/", "https://read2burn.com/" };

            metadata = new ArrayList<Metadata>();
            Metadata md = new Metadata();
            md.setMetaKey("lang");
            md.setMetaValue("de");
            metadata.add(md);

            md = new Metadata();
            md.setMetaKey("partner");
            md.setMetaValue("bund");
            metadata.add(md);

            md = new Metadata();
            md.setMetaKey("provider");
            md.setMetaValue("bu_bmu");
            metadata.add(md);

            md = new Metadata();
            md.setMetaKey("datatype");
            md.setMetaValue("www");
            metadata.add(md);

            md = new Metadata();
            md.setMetaKey("datatype");
            md.setMetaValue("default");
            metadata.add(md);

            for (String uri : urls) {
                url = new Url("catalog");
                url.setStatus("400");
                url.setUrl(uri);
                List<String> limit = new ArrayList<String>();
                limit.add(uri);
                url.setLimitUrls(limit);
                url.setMetadata(metadata);
                em.persist(url);
            }

            url = new Url("other");
            url.setStatus("200");
            url.setUrl("http://de.wikipedia.org/");
            List<String> limit = new ArrayList<String>();
            limit.add("http://de.wikipedia.org");
            url.setLimitUrls(limit);
            em.persist(url);
        }

        em.getTransaction().commit();
    }

    @Override
    public IngridDocument call(IngridCall targetInfo) throws Exception {
        throw new RuntimeException( "call-function not implemented in SE-iPlug" );
    }

}
