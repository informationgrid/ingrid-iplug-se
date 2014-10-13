package de.ingrid.iplug.se;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.tngtech.configbuilder.annotation.configuration.Separator;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

@PropertiesFiles( {"config", "elasticsearch"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Configuration implements IConfig {
    
    private static Logger log = Logger.getLogger(Configuration.class);
    
    public class StringToSearchType extends TypeTransformer<String, SearchType> {
        
        @Override
        public SearchType transform( String input ) {
            SearchType type;
            switch (input) {
            case "COUNT":
                type = SearchType.COUNT;
                break;
            case "DEFAULT":
                type = SearchType.DEFAULT;
                break;
            case "DFS_QUERY_AND_FETCH":
                type = SearchType.DFS_QUERY_AND_FETCH;
                break;
            case "DFS_QUERY_THEN_FETCH":
                type = SearchType.DFS_QUERY_THEN_FETCH;
                break;
            case "QUERY_AND_FETCH":
                type = SearchType.QUERY_AND_FETCH;
                break;
            case "QUERY_THEN_FETCH":
                type = SearchType.QUERY_THEN_FETCH;
                break;
            case "SCAN":
                type = SearchType.SCAN;
                break;
            default:
                log.error( "Unknown SearchType (" + input + "), using default one: DFS_QUERY_THEN_FETCH" );
                type = SearchType.DFS_QUERY_THEN_FETCH;
            }
            return type;
        }
        
    }
    
    public class StringToModifier extends TypeTransformer<String, Modifier> {
        
        @Override
        public Modifier transform( String input ) {
            Modifier modifier = null;
            switch (input) {
            case "none":
                modifier = Modifier.NONE;
                break;
            case "log":
                modifier = Modifier.LOG;
                break;
            case "log1p":
                modifier = Modifier.LOG1P;
                break;
            case "log2p":
                modifier = Modifier.LOG2P;
                break;
            case "ln":
                modifier = Modifier.LN;
                break;
            case "ln1p":
                modifier = Modifier.LN1P;
                break;
            case "ln2p":
                modifier = Modifier.LN2P;
                break;
            case "square":
                modifier = Modifier.SQUARE;
                break;
            case "sqrt":
                modifier = Modifier.SQRT;
                break;
            case "reciprocal":
                modifier = Modifier.RECIPROCAL;
                break;
            }
            return modifier;
        }
    }
    
	@Override
	public void initialize() {
	}

    @TypeTransformers(Configuration.StringToSearchType.class)
    @PropertyValue("search.type")
    @DefaultValue("DEFAULT")
    public SearchType searchType;
	
	@PropertyValue("dir.instances")
	@DefaultValue("instances")
    private String dirInstances;

	@PropertyValue("db.id")
    @DefaultValue("iplug-se")
    public String databaseID;
	
	@PropertyValue("db.dir")
	@DefaultValue("database")
	public String databaseDir;
	
	@PropertyValue("http.port")
    @DefaultValue("9200")
    public String esHttpPort;
    
	@PropertyValue("transport.tcp.port")
    @DefaultValue("9300")
    public String esTransportTcpPort;

    @PropertyValue("http.host")
    @DefaultValue("localhost")
    public String esHttpHost;
    
    @PropertyValue("elastic.boost.field")
    @DefaultValue("boost")
    public String esBoostField;
    
    @TypeTransformers(Configuration.StringToModifier.class)
    @PropertyValue("elastic.boost.modifier")
    @DefaultValue("log1p")
    public Modifier esBoostModifier;
    
    @PropertyValue("elastic.boost.factor")
    @DefaultValue("0.1")
    public float esBoostFactor;
    
    @PropertyValue("elastic.boost.mode")
    @DefaultValue("sum")
    public String esBoostMode;
	
	@PropertyValue("index.name")
	@DefaultValue("iplugse")
	public String index;
	
	@PropertyValue("instance.active")
	@DefaultValue("")
	public List<String> activeInstances;
	
    @PropertyValue("nutch.call.java.options")
    @DefaultValue("-Dhadoop.log.file=hadoop.log -Dfile.encoding=UTF-8")
    @Separator(" ")
    public List<String> nutchCallJavaOptions;

	
	@Override
    public void addPlugdescriptionValues( PlugdescriptionCommandObject pdObject ) {
        pdObject.put( "iPlugClass", "de.ingrid.iplug.se.SEIPlug" );

        // make sure only partner=all is communicated to iBus
        @SuppressWarnings("unchecked")
        List<String> partners = pdObject.getArrayList(PlugDescription.PARTNER);
        //
        if (partners == null) {
            pdObject.addPartner("all");
        } else {
            /*for (String partner : partners) {
                if (!partner.equalsIgnoreCase("all")) {
                    pdObject.removeFromList(PlugDescription.PARTNER, partner);
                }
            }*/
            
            partners.clear();
            partners.add("all");
        }

        // make sure only provider=all is communicated to iBus
        @SuppressWarnings("unchecked")
        List<String> providers = pdObject.getArrayList(PlugDescription.PROVIDER);
        if (providers == null) {
        pdObject.addProvider("all");
        } else {
            for (String provider : providers) {
                if (!provider.equalsIgnoreCase("all")) {
                    pdObject.removeFromList(PlugDescription.PROVIDER, provider);
                }
            }
            if (providers.isEmpty()) {
                providers.add("all");
            }
        }
        
        if (!pdObject.containsRankingType(IngridQuery.SCORE_RANKED)) {
            pdObject.addToList(IngridQuery.RANKED, IngridQuery.SCORE_RANKED);
        }
        
//        pdObject.addField("incl_meta");
//        pdObject.addField("t01_object.obj_class");
//        pdObject.addField("metaclass");
//        
        // DatabaseConnection dbc = new DatabaseConnection( databaseDriver,
        // databaseUrl, databaseUsername, databasePassword, databaseSchema );
//        pdObject.setConnection( dbc );
    }

//
    @Override
    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd ) {
        props.setProperty( "db.dir", this.databaseDir );
        props.setProperty( "dir.instances", this.dirInstances );
        props.setProperty( "instance.active", getActiveInstancesAsString() );        
        
        // write elastic search properties to separate configuration
        // TODO: refactor this code to make an easy function, by putting it into
        // the base-webapp!
        Properties p = new Properties();
        try {
            // check for elastic search settings in classpath, which works
            // during development
            // and production
            Resource resource = new ClassPathResource( "/elasticsearch.properties" );
            if (resource.exists()) {
                p.load( resource.getInputStream() );
            } else {
                // create file if it does not exist yet
                // use the location of the production environment!
                resource = new FileSystemResource( "conf/elasticsearch.properties" );
            }
            p.put( "http.port", esHttpPort );
            p.put( "script.disable_dynamic", false );
            OutputStream os = new FileOutputStream( resource.getFile() ); 
            p.store( os, "Override configuration written by the application" );
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getInstancesDir() {
        return dirInstances;
    }
    
    public void setInstancesDir(String dir) {
        this.dirInstances = dir;
    }
    
    public Map<String, String> getElasticSearchSettings() {
        Map<String,String> map = new HashMap<String, String>();
        //map.put( "", "" )
        return map;
    }
    
    public String getActiveInstancesAsString() {
        return StringUtils.join( this.activeInstances, ',' );
    }

}
