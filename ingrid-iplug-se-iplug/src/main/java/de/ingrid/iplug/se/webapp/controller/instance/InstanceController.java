/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.webapp.controller.instance;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.webapp.container.Instance;

public class InstanceController {

    public static Instance getInstanceData(String name) {
        // first check if the path really exists, in case we called an URL with a wrong parameter
        Path workPath = Paths.get( SEIPlug.conf.getInstancesDir(), name );
        if (!workPath.toFile().exists()) return null;
        
        Instance instance = new Instance();
        instance.setName( name );
        instance.setWorkingDirectory( workPath.toString() );
        instance.setIndexName( SEIPlug.conf.index );
        
        if (SEIPlug.conf.activeInstances.contains( name )) {
            instance.setIsActive( true );
            
        } else {
            instance.setIsActive( false );            
        }
        instance.setEsTransportTcpPort(SEIPlug.conf.esTransportTcpPort);
        instance.setEsHttpHost(SEIPlug.conf.esHttpHost);
        
    
        return instance;
    }
}
