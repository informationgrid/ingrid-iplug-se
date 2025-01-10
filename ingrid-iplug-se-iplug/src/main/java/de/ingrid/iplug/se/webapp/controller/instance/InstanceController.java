/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.webapp.controller.instance;

import de.ingrid.admin.controller.AbstractController;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.DBUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridCall;
import de.ingrid.utils.IngridDocument;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class InstanceController extends AbstractController {

    private static Logger log = LogManager.getLogger(InstanceController.class);

    private static CommunicationService communicationInterface;

    public static Instance getInstanceData(String name) {
        // first check if the path really exists, in case we called an URL with a wrong parameter
        Path workPath = Paths.get(SEIPlug.conf.getInstancesDir(), name );
        if (!workPath.toFile().exists()) return null;

        Instance instance = new Instance();
        instance.setName( name );
        instance.setWorkingDirectory( workPath.toString() );
        instance.setClusterName( SEIPlug.conf.clusterName );
        instance.setIndexName( SEIPlug.baseConfig.index );

        HashSet<String> activeIndices = getActiveIndices();

        if (activeIndices != null) {
            String iBusIndexId = SEIPlug.baseConfig.uuid + "=>" + instance.getInstanceIndexName();
            instance.setIsActive(activeIndices.contains(iBusIndexId));
        }

        instance.setEsTransportTcpPort(SEIPlug.conf.esTransportTcpPort);
        instance.setEsHttpHost(SEIPlug.conf.esHttpHost);


        return instance;
    }

    private static HashSet<String> getActiveIndices() {

        // always get iBus from communicationInterface in case it has been changed
        IBus iBus = communicationInterface.getIBus();

        IngridCall call = new IngridCall();
        call.setTarget("iBus");
        call.setMethod("getActiveIndices");
        IngridDocument result;
        try {
            result = iBus.call(call);
        } catch (Exception e) {
            log.error("Could not get indices from iBus", e);
            return null;
        }

        return (HashSet<String>) result.get("result");
    }

    /**
     * Returns true if the user has role 'instanceAdmin' and has NO ACCESS to the given instance.
     */
    public boolean hasNoAccessToInstance(String instanceName, HttpServletRequest request, HttpServletResponse response) {
        String user = request.getUserPrincipal().getName();
        return request.isUserInRole("instanceAdmin") && !DBUtils.isAdminForInstance(user, instanceName);

    }

    public static void setCommunicationInterface(CommunicationService communicationInterface) {
        InstanceController.communicationInterface = communicationInterface;
    }
}
