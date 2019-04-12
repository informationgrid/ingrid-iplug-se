/*
 * **************************************************-
 * ingrid-iplug-se-nutch
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
package de.ingrid.iplug.se.nutch.tools;

import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IBusIndexManager;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.ElasticDocument;
import net.weta.components.communication.ICommunication;
import net.weta.components.communication.tcp.StartCommunication;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class IBusElasticsearchClient {

    public static Logger LOG = LoggerFactory.getLogger(IBusElasticsearchClient.class);

    private final IBusIndexManager iBusIndexManager;
    private final IndexInfo indexInfo;
    private final BusClient busClient;

    private String instanceIndex;

    private Configuration config;

    public IBusElasticsearchClient(Configuration conf) throws Exception {
        this.config = conf;

        instanceIndex = config.get(ElasticConstants.INDEX, "nutch");

        ElasticConfig elasticConfig = new ElasticConfig();
        elasticConfig.communicationProxyUrl = config.get("iplug.id");
        iBusIndexManager = new IBusIndexManager(elasticConfig);

        indexInfo = new IndexInfo();
        indexInfo.setToIndex(instanceIndex);
        indexInfo.setToAlias(instanceIndex);
        indexInfo.setToType("default");
        indexInfo.setDocIdField("id");

        // get communication file and load it
        File file = new File(conf.get("communication.file"));
        ICommunication communication = StartCommunication.create(new FileInputStream(file));

        // also change peer name for a temporary connection
        communication.setPeerName("iPlug-SE-indexer_" + Math.random());

        LOG.info("Create communication for indexing");
        busClient = BusClientFactory.createBusClient(communication);
        communication.startup();
        // busClient.start();
    }

    public void update(Map<String, Object> document) {
        ElasticDocument doc = new ElasticDocument(document);
        iBusIndexManager.update(indexInfo, doc, false);
    }

    public void deleteDoc(String key) {
        if (key == null) {
            LOG.warn("Document ID for deletion is null");
            return;
        }
         iBusIndexManager.delete(indexInfo, key, false);
    }

    public void close() throws Exception {
        LOG.info("Indexing finished. Flushing and closing connection to iBus.");
        iBusIndexManager.flush();
        busClient.shutdown();
    }
}
