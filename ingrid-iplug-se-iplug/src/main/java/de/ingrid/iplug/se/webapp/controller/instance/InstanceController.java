/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.admin.security.IngridPrincipal;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.DBUtils;
import de.ingrid.iplug.se.webapp.container.Instance;

public class InstanceController extends AbstractController {

    public static Instance getInstanceData(String name) {
        // first check if the path really exists, in case we called an URL with a wrong parameter
        Path workPath = Paths.get( SEIPlug.conf.getInstancesDir(), name );
        if (!workPath.toFile().exists()) return null;
        
        Instance instance = new Instance();
        instance.setName( name );
        instance.setWorkingDirectory( workPath.toString() );
        instance.setClusterName( JettyStarter.getInstance().config.cluster );
        instance.setIndexName( JettyStarter.getInstance().config.index );
        
        if (JettyStarter.getInstance().config.indexSearchInTypes.contains( name )) {
            instance.setIsActive( true );
            
        } else {
            instance.setIsActive( false );            
        }
        instance.setEsTransportTcpPort(SEIPlug.conf.esTransportTcpPort);
        //instance.setEsHttpHost(SEIPlug.conf.esHttpHost);
        
    
        return instance;
    }
    
    /**
     * Returns true if the user has role 'instanceAdmin' and has NO ACCESS to the given instance.
     * 
     * @param instanceName
     * @param request
     * @param response
     * @return
     */
    public boolean hasNoAccessToInstance(String instanceName, HttpServletRequest request, HttpServletResponse response) {
        String user = request.getUserPrincipal().getName();
        if (!(request.getUserPrincipal() instanceof IngridPrincipal.SuperAdmin) && request.isUserInRole( "instanceAdmin" ) && !DBUtils.isAdminForInstance( user, instanceName )) {
            return true;
        } else {
            return false;
        }
        
    }

}
