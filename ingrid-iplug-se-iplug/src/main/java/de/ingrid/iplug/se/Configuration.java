package de.ingrid.iplug.se;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;

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
    
    private UrlMaintenanceSettings urlMaintenanceSettings;

	@Override
	public void initialize() {
	    try(Reader reader = new InputStreamReader(Configuration.class.getResourceAsStream("/urlMaintenance.json"), "UTF-8")) {
//            Gson gson = new GsonBuilder().create();
//            UrlMaintenanceSettings settings = new UrlMaintenanceSettings();
//            
//            List<IngridPartner> partner = new ArrayList<IngridPartner>();
//            IngridPartner p = settings.new IngridPartner("by", "Bayern");
//            List<Provider> provider = new ArrayList<Provider>();
//            Provider prov1 = new Provider( "prov1", "Anbieter 1" );
//            provider.add( prov1 );
//            Provider prov2 = new Provider( "prov1", "Anbieter 1" );
//            provider.add( prov2 );
//            p.provider = provider;
//            partner.add( p  );
//            settings.setPartner( partner );
//            List<UrlTypes> types = new ArrayList<UrlMaintenanceSettings.UrlTypes>();
//            UrlTypes urlType = settings.new UrlTypes();
//            urlType.name = "Webseiten";
//            List<Options> options = new ArrayList<UrlMaintenanceSettings.Options>();
//            
//            Options o = settings.new Options();
//            o.id = "o1";
//            o.value = "Umwelt";
//            options.add( o );
//            o = settings.new Options();
//            o.id = "o2";
//            o.value = "Recht";
//            options.add( o );
//            o = settings.new Options();
//            o.id = "o3";
//            o.value = "Forschung";
//            options.add( o );
//            urlType.options = options ;
//            types.add( urlType  );
//            settings.setTypes( types  );
//            String json = gson.toJson( settings );
//            //Person p = gson.fromJson(reader, Person.class);
//            System.out.println(json);
	        
	        // see: http://www.javacreed.com/simple-gson-example/
	        Gson gson = new GsonBuilder().create();
            UrlMaintenanceSettings settings = gson.fromJson(reader, UrlMaintenanceSettings.class);
            setUrlMaintenanceSettings( settings );
	    
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
	
	@PropertyValue("partner.name")
	public String partnerName;
	
	@PropertyValue("partner.provider.name")
	public List<String> providerNames;
	

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
    
    public UrlMaintenanceSettings getUrlMaintenanceSettings() {
        return urlMaintenanceSettings;
    }
    
    public void setUrlMaintenanceSettings(UrlMaintenanceSettings urlMaintenanceSettings) {
        this.urlMaintenanceSettings = urlMaintenanceSettings;
    }


}
