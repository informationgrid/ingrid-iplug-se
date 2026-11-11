/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2026 wemove digital solutions GmbH
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

import co.elastic.clients.elasticsearch.core.SearchResponse;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.DBUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.utils.ElasticDocument;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridCall;
import de.ingrid.utils.IngridDocument;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class InstanceController extends AbstractController {

    private static Logger log = LogManager.getLogger(InstanceController.class);

    private static CommunicationService communicationInterface;
    private static IndexManager indexManager;
    private static ElasticConfig elasticConfig;

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

        if (elasticConfig != null && !elasticConfig.esCommunicationThroughIBus) {
            return getActiveIndicesFromElasticsearch();
        }

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

    private static HashSet<String> getActiveIndicesFromElasticsearch() {
        try {
            if (!indexManager.indexExists("ingrid_meta")) {
                return new HashSet<>();
            }

            SearchResponse<ElasticDocument> response = indexManager.getClient().search(s -> s
                    .index("ingrid_meta")
                    .size(10000)
                    .query(q -> q.term(t -> t.field("active").value(v -> v.booleanValue(true)))),
                    ElasticDocument.class);

            HashSet<String> activeIndices = new HashSet<>();
            response.hits().hits().forEach(hit -> {
                ElasticDocument source = hit.source();
                if (source != null && source.get("indexId") != null) {
                    activeIndices.add(source.get("indexId").toString());
                }
            });
            return activeIndices;
        } catch (Exception e) {
            log.error("Could not get indices from Elasticsearch", e);
            return null;
        }
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

    @Autowired
    public void setElasticDependencies(IndexManager indexManager, ElasticConfig elasticConfig) {
        InstanceController.indexManager = indexManager;
        InstanceController.elasticConfig = elasticConfig;
    }
}
