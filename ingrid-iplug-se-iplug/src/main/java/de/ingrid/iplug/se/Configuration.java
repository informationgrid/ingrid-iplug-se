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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@PropertiesFiles( {"config", "elasticsearch"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Configuration implements IConfig {
    
    private static Logger log = Logger.getLogger(Configuration.class);
    
    public class StringToSearchType extends TypeTransformer<String, SearchType>{
        
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
	
	@PropertyValue("http.port")
    @DefaultValue("9299")
    public String esHttpPort;
	
	@PropertyValue("index.name")
	@DefaultValue("iplugse")
	public String index;
	
	@PropertyValue("instance.active")
	public List<String> activeInstances;
	

	@Override
    public void addPlugdescriptionValues( PlugdescriptionCommandObject pdObject ) {
        pdObject.put( "iPlugClass", "de.ingrid.iplug.se.SEIPlug" );
        
//        pdObject.addField("incl_meta");
//        pdObject.addField("t01_object.obj_class");
//        pdObject.addField("metaclass");
//        
//        DatabaseConnection dbc = new DatabaseConnection( databaseDriver, databaseUrl, databaseUsername, databasePassword, databaseSchema );
//        pdObject.setConnection( dbc );
    }
//
    @Override
    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd ) {
        props.setProperty( "dir.instances", this.dirInstances);
        props.setProperty( "instance.active", getActiveInstancesAsString() );        
        
        // write elastic search properties to separate configuration
        // TODO: refactor this code to make an easy function, by putting it into the base-webapp!
        Properties p = new Properties();
        try {
            // check for elastic search settings in classpath, which works during development
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
