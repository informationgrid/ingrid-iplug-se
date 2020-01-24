/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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

import org.springframework.beans.factory.annotation.Value;

@org.springframework.context.annotation.Configuration
public class MigratorConfig {

    @Value("${username:}")
    public String username;

    @Value("${password:}")
    public String password;

    @Value("${dbPath:}")
    public String dbPath;

    @Value("${webInstance:}")
    public String webInstance;

    @Value("${catalogInstance:}")
    public String catalogInstance;

    @Value("${partner:}")
    public String partner;

    @Value("${db.dir:database}")
    public String databaseDir;

    @Value("${db.id:iplug-se}")
    public String databaseID;

    @Value("${dir.instances:instances}")
    private String dirInstances;


    public void validate() {
        if ("".equals(username) || "".equals(dbPath) || "".equals(webInstance) || "".equals(catalogInstance)) {
            System.out.println( "==================================================================================" );
            System.out.println( "Usage: migrate.sh -DdbPath=<db-path> -DwebInstance=<web-instance> -DcatalogInstance=<catalog-instance> -Dusername=<username> [-Dpassword=<password>] [-Dpartner=<partner>]" );
            System.out.println( "----------------------------------" );
            System.out.println( "e.g.:  migrate.sh -DdbPath=jdbc:mysql://localhost:3306/iplugse -Dweb=my_websites -Dcatalog=my_catalog -Dusername=root -Dpassword=1234 -Dpartner=mv" );
            System.out.println( "==================================================================================" );
            System.out.println( "The web and catalog name can be the same if the two different URL types shall be\n"
                    + "merged together into one instance. The handling of these URLs will not be changed,\n"
                    + "so that catalog-URLs will be fetched as single pages only.\n"
                    + "When migrating from Oracle-Database, you have to activate the Oracle driver. Look at migrate.sh script." );
            System.exit( -1 );
        }
    }

    public String getInstancesDir() {
        return dirInstances;
    }
}
