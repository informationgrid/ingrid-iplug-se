/*
 * Copyright (c) 1997-2006 by wemove GmbH
 */
package de.ingrid.iplug.se;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
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
    private static Logger log = Logger.getLogger( SEIPlug.class );

	public static Configuration conf;

    /**
     * The <code>PlugDescription</code> object passed at startup
     */
    private PlugDescription fPlugDesc = null;

    /**
     * Workingdirectory of the iPlug instance as absolute path
     */
    //private String fWorkingDir = ".";

    /**
     * Unique Plug-iD
     */
    private String fPlugId = null;

    //private static final long serialVersionUID = SEIPlug.class.getName().hashCode();

    @Autowired
	private Index index;


    public SEIPlug() {
        super(30000, null, null, null, null);
    };
    
    @Autowired
    public SEIPlug(IMetadataInjector[] injector, IPreProcessor[] preProcessors, IPostProcessor[] postProcessors) {
        super(30000, new PlugDescriptionFieldFilters(), injector, preProcessors, postProcessors);
        
    }
    
    /**
     * @see de.ingrid.utils.IPlug#configure(de.ingrid.utils.PlugDescription)
     */
    @Override
    public void configure(PlugDescription plugDescription) {
        super.configure(plugDescription);
        log.info("Configuring iPlug-SE ...");

        this.fPlugDesc = plugDescription;
        this.fPlugId = fPlugDesc.getPlugId();
//        try {
//            this.fWorkingDir = fPlugDesc.getWorkinDirectory().getCanonicalPath();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        // TODO: add plug id as index information
        // ...
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
        
        return index.search( query, 0, 10 );
        
    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetail(de.ingrid.utils.IngridHit,
     *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
     */
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] requestedFields) throws Exception {
        return index.getDetail( hit, requestedFields );
    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit[],
     *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
     */
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] requestedFields) throws Exception {
        IngridHitDetail[] detailHits = new IngridHitDetail[ hits.length ];
        
        for (int i = 0; i < hits.length; i++) {
            detailHits[i] = getDetail( hits[i], query, requestedFields );
            
        }
        return detailHits;
    }
    
    public static void main(String[] args) throws Exception {
        conf = new ConfigBuilder<Configuration>(Configuration.class).withCommandLineArgs(args).build();
        log.debug( "DEBUG test" );
        new JettyStarter( conf );
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(conf.databaseID);//, properties);
        DBManager.INSTANCE.intialize(emf);
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        
        // do database migrations
//        Flyway flyway = new Flyway();
//        flyway.setDataSource(dbUrl, "", "");
//        flyway.migrate();
        
        // get an entity manager instance (initializes properties in the DBManager)
        em.getTransaction().begin();
        Url url = new Url( "catalog" );
        url.setStatus( 200 );
        url.setUrl( "http://www.wemove.com" );
        List<Metadata> metadata = new ArrayList<Metadata>();
        Metadata m1 = new Metadata();
        m1.setMetaKey( "lang" );
        m1.setMetaValue( "en" );
        Metadata m2 = new Metadata();
        m2.setMetaKey( "topic" );
        m2.setMetaValue( "t2" );
        Metadata m3 = new Metadata();
        m3.setMetaKey( "topic" );
        m3.setMetaValue( "t3" );
        Metadata m4 = new Metadata();
        m4.setMetaKey( "unknown" );
        m4.setMetaValue( "xxx" );
        Metadata m5 = new Metadata();
        m5.setMetaKey( "topic" );
        m5.setMetaValue( "angularjs" );
        metadata.add( m1 );
        metadata.add( m2 );
        metadata.add( m3 );
        metadata.add( m4 );
        metadata.add( m5 );
        url.setMetadata( metadata );
        List<String> limitUrls = new ArrayList<String>();
        limitUrls.add( "http://www.wemove.com/about" );
        limitUrls.add( "http://www.wemove.com/jobs" );
        url.setLimitUrls( limitUrls );
        
        em.persist(url);
        em.getTransaction().commit();
        
    }
    
}
