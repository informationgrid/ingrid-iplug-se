package de.ingrid.iplug.se.migrate;

import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertiesFiles;
import com.tngtech.configbuilder.annotation.propertyloaderconfiguration.PropertyLocations;
import com.tngtech.configbuilder.annotation.validation.Validation;
import com.tngtech.configbuilder.annotation.valueextractor.CommandLineValue;

import de.ingrid.iplug.se.Configuration;

@PropertiesFiles( {"config"} )
@PropertyLocations(directories = {"conf"}, fromClassLoader = true)
public class MigratorConfig extends Configuration {

    @CommandLineValue(shortOpt="u", longOpt="username", hasArg=true)
    public String username;

    @CommandLineValue(shortOpt="p", longOpt="password", hasArg=true)
    public String password;
    
    @CommandLineValue(shortOpt="db", longOpt="dbPath", hasArg=true)
    public String dbPath;

    @CommandLineValue(shortOpt="web", longOpt="webInstance", hasArg=true)
    public String webInstance;
    
    @CommandLineValue(shortOpt="catalog", longOpt="catalogInstance", hasArg=true)
    public String catalogInstance;

    @Validation
    private void validate() {
        if (username == null || dbPath == null || webInstance == null || catalogInstance == null) {
            System.out.println( "==================================================================================" );
            System.out.println( "Usage: migrate.sh -db <db-path> -web <web-instance> -catalog <catalog-instance> -u <username> [-p <password>]" );
            System.out.println( "or:    migrate.sh -dbPath <db-path> -webInstance <web-instance> -catalogInstance <catalog-instance> -username <username> [-password <password>]" );
            System.out.println( "----------------------------------" );
            System.out.println( "e.g.:  migrate.sh -dbPath jdbc:mysql://localhost:3306/iplugse -web my_websites -catalog my_catalog -u root -p 1234" );
            System.out.println( "==================================================================================" );
            System.out.println( "The web and catalog name can be the same if the two different URL types shall be\n"
                    + "merged together into one instance. The handling of these URLs will not be changed,\n"
                    + "so that catalog-URLs will be fetched as single pages only." );
            System.exit( -1 );
        }
    }
}
