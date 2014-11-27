/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

    @CommandLineValue(shortOpt="partner", longOpt="partner", hasArg=true)
    public String partner;
    
    @Validation
    private void validate() {
        if (username == null || dbPath == null || webInstance == null || catalogInstance == null) {
            System.out.println( "==================================================================================" );
            System.out.println( "Usage: migrate.sh -db <db-path> -web <web-instance> -catalog <catalog-instance> -u <username> [-p <password>] [-partner <partner>}" );
            System.out.println( "or:    migrate.sh -dbPath <db-path> -webInstance <web-instance> -catalogInstance <catalog-instance> -username <username> [-password <password>] [-partner <partner>]" );
            System.out.println( "----------------------------------" );
            System.out.println( "e.g.:  migrate.sh -dbPath jdbc:mysql://localhost:3306/iplugse -web my_websites -catalog my_catalog -u root -p 1234 -partner mv" );
            System.out.println( "==================================================================================" );
            System.out.println( "The web and catalog name can be the same if the two different URL types shall be\n"
                    + "merged together into one instance. The handling of these URLs will not be changed,\n"
                    + "so that catalog-URLs will be fetched as single pages only." );
            System.exit( -1 );
        }
    }
}
