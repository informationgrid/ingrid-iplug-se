package de.ingrid.iplug.se;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchType;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformer;
import com.tngtech.configbuilder.annotation.typetransformer.TypeTransformers;
import com.tngtech.configbuilder.annotation.valueextractor.DefaultValue;
import com.tngtech.configbuilder.annotation.valueextractor.PropertyValue;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@PropertiesFiles( {"config"} )
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPropertiesFromPlugdescription(Properties arg0,
			PlugdescriptionCommandObject arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@TypeTransformers(Configuration.StringToSearchType.class)
    @PropertyValue("search.type")
    @DefaultValue("DEFAULT")
    public SearchType searchType;	

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
//    @Override
//    public void setPropertiesFromPlugdescription( Properties props, PlugdescriptionCommandObject pd ) {
//        DatabaseConnection connection = (DatabaseConnection) pd.getConnection();
//        databaseDriver = connection.getDataBaseDriver();
//        databaseUrl = connection.getConnectionURL();
//        databaseUsername = connection.getUser();
//        databasePassword = connection.getPassword();
//        databaseSchema = connection.getSchema();
//        
//        props.setProperty( "iplug.database.driver", databaseDriver);
//        props.setProperty( "iplug.database.url", databaseUrl);
//        props.setProperty( "iplug.database.username", databaseUsername);
//        props.setProperty( "iplug.database.password", databasePassword);
//        props.setProperty( "iplug.database.schema", databaseSchema);
//    }


}
