/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
/*
 * Copyright (c) 1997-2006 by wemove GmbH
 */
package de.ingrid.iplug.se;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.flywaydb.core.Flyway;
import org.h2.tools.Recover;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.IndexImpl;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.instance.InstanceController;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.tool.QueryUtil;

/**
 * TODO Describe your created type (class, etc.) here.
 * 
 * @author joachim@wemove.com
 */
@Service
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

    private static ElasticsearchNodeFactoryBean esBean;

    private static NutchController nutchController;

    private static EntityManager em;

    public SEIPlug() {
        super(30000, null, null, null, null);
    };

    @Autowired
    public SEIPlug(IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors, 
            ElasticsearchNodeFactoryBean esBean, NutchController nutchController) {
        super(30000, new PlugDescriptionFieldFilters(), injector, preProcessors, postProcessors);
        SEIPlug.esBean = esBean;
        SEIPlug.nutchController = nutchController;
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
    public void close() throws Exception {
        // TODO Auto-generated method stub

    }

    /**
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery,
     *      int, int)
     */
    public IngridHits search(IngridQuery query, int start, int length) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("incomming query : " + query.toString());
        }

        // check if query is rejected and return 0 hits instead of search within
        // the iplug
        if (query.isRejected()) {
            return new IngridHits(fPlugId, 0, new IngridHit[] {}, true);
        }

        // remove "meta" field from query so search works !
        QueryUtil.removeFieldFromQuery(query, QueryUtil.FIELDNAME_METAINFO);
        QueryUtil.removeFieldFromQuery(query, QueryUtil.FIELDNAME_INCL_META);
        
        preProcess(query);

        return index.search(query, start, length);

    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetail(de.ingrid.utils.IngridHit,
     *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
     */
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] requestedFields) throws Exception {
        
        preProcess(query);
        
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
        conf = new ConfigBuilder<Configuration>(Configuration.class).withCommandLineArgs(args).build();
        new JettyStarter(conf);

        // set the directory of the database to the configured one
        Map<String, String> properties = new HashMap<String, String>();
        Path dbDir = Paths.get(conf.databaseDir);
        properties.put("javax.persistence.jdbc.url", "jdbc:h2:" + dbDir.toFile().getAbsolutePath() + "/urls;MVCC=true");

        // get an entity manager instance (initializes properties in the
        // DBManager)
        EntityManagerFactory emf = null;
        // for development use the settings from the persistence.xml
        if ("iplug-se-dev".equals(conf.databaseID)) {
            emf = Persistence.createEntityManagerFactory(conf.databaseID);
        } else {
            emf = Persistence.createEntityManagerFactory(conf.databaseID, properties);
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
        if ("iplug-se-dev".equals(conf.databaseID)) {
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

        // normally shutdown the elastic search node and stop all running
        // nutch processes
        Runtime.getRuntime().addShutdownHook(new Thread() {
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
                    e.printStackTrace();
                }
            }
        });
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

}
