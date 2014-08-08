/*
 * Copyright (c) 1997-2006 by wemove GmbH
 */
package de.ingrid.iplug.se;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.IngridQuery;

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

	private static Configuration conf;

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

        
        index.search( null, 0, 10 );
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
        
        return new IngridHits(this.fPlugId, 0, new IngridHit[0], true);
    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetail(de.ingrid.utils.IngridHit,
     *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
     */
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery query, String[] requestedFields) throws Exception {
        return new IngridHitDetail();
    }

    /**
     * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit[],
     *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
     */
    public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] requestedFields) throws Exception {
        return new IngridHitDetail[0];
    }
    
    public static void main(String[] args) throws Exception {
        conf = new ConfigBuilder<Configuration>(Configuration.class).withCommandLineArgs(args).build();
        new JettyStarter( conf );
    }
    
}
