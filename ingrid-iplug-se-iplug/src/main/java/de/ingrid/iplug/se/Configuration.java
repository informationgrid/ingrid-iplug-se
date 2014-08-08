package de.ingrid.iplug.se;

import java.util.Properties;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;

import de.ingrid.admin.IConfig;
import de.ingrid.admin.command.PlugdescriptionCommandObject;

@PropertiesFiles( {"config"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class Configuration implements IConfig {
    
    //private static Log log = LogFactory.getLog(Configuration.class);

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPropertiesFromPlugdescription(Properties arg0,
			PlugdescriptionCommandObject arg1) {
		// TODO Auto-generated method stub
		
	}
    
//    @PropertyValue("iplug.database.driver")
//    @DefaultValue("com.mysql.jdbc.Driver")
//    public String databaseDriver;
//    
//    @PropertyValue("iplug.database.url")
//    @DefaultValue("jdbc:mysql://localhost:3306/igc")
//    public String databaseUrl;
//    
//    @PropertyValue("iplug.database.username")
//    public String databaseUsername;
//    
//    @PropertyValue("iplug.database.password")
//    public String databasePassword;
//    
//    @PropertyValue("iplug.database.schema")
//    public String databaseSchema;
//    
//    
//    @PropertyValue("spring.profile")
//    public String springProfile;
//
//    @Override
//    public void initialize() {
//        if ( springProfile != null ) {
//            System.setProperty( "spring.profiles.active", springProfile );
//        } else {
//            log.error( "Spring profile not set! In configuration set 'spring.profile' to one of 'object_internet', 'object_intranet', 'address_internet' or 'address_intranet'" );
//            System.exit( 1 );
//        }
//    }
//
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
