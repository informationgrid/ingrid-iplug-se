/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;

public class IngridElasticSearchClient {

    public static Logger LOG = LoggerFactory.getLogger(IngridElasticSearchClient.class);

    private static final int DEFAULT_MAX_BULK_DOCS = 250;
    private static final int DEFAULT_MAX_BULK_LENGTH = 2500500;

    private Client client;
    private String instanceIndex;

    private Configuration config;

    private BulkRequestBuilder bulk;
    private ActionFuture<BulkResponse> execute = null;
    private int port = -1;
    private String host = null;
    private String clusterName = null;
    private int maxBulkDocs;
    private int maxBulkLength;
    private long indexedDocs = 0;
    private int bulkDocs = 0;
    private int bulkLength = 0;
    private boolean createNewBulk = false;

    private String type = "default";

    public IngridElasticSearchClient(Configuration conf) throws IOException {
        this.config = conf;

        clusterName = config.get(ElasticConstants.CLUSTER);
        if (clusterName == null) LOG.warn( "No cluster name specified! If node cannot be found, then we might search in the wrong cluster." );

        host = config.get(ElasticConstants.HOST);
        port = config.getInt(ElasticConstants.PORT, 9300);

//        type = config.get(ElasticConstants.TYPE, "default");

        Settings.Builder settingsBuilder = Settings.builder(); // .loadFromPath()classLoader(Settings.class.getClassLoader());

        BufferedReader reader = new BufferedReader(config.getConfResourceAsReader("elasticsearch.conf"));
        String line;
        String parts[];

        while ((line = reader.readLine()) != null) {
            if (StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                line.trim();
                parts = line.split("=");

                if (parts.length == 2) {
                    settingsBuilder.put(parts[0].trim(), parts[1].trim());
                }
            }
        }

        if (StringUtils.isNotBlank(clusterName))
            settingsBuilder.put("cluster.name", clusterName);

        // Set the cluster name and build the settings
        Settings settings = settingsBuilder.build();

        // Prefer TransportClient
        if (host != null && port > 1) {
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new TransportAddress(  InetAddress.getByName( host ), port) );

        } else if (clusterName != null) {
            throw new RuntimeException("TransportClient not created since host and/or port not defined.");
        }

        bulk = client.prepareBulk();
        instanceIndex = config.get(ElasticConstants.INDEX, "nutch");
        maxBulkDocs = config.getInt(ElasticConstants.MAX_BULK_DOCS, DEFAULT_MAX_BULK_DOCS);
        maxBulkLength = config.getInt(ElasticConstants.MAX_BULK_LENGTH, DEFAULT_MAX_BULK_LENGTH);
    }

    public void addRequest(IndexRequestBuilder request, int size) throws IOException {
        // Add this indexing request to a bulk request
        bulkLength += size;
        bulk.add(request);
        checkNewBulk();
    }

    public void addRequest(DeleteRequestBuilder request, int size) throws IOException {
        // Add this indexing request to a bulk request
        bulkLength += size;
        bulk.add(request);
        checkNewBulk();
    }

    public IndexRequestBuilder prepareIndexRequest(@Nullable String id) {
        return client.prepareIndex(instanceIndex, type, id);
    }

    public DeleteRequestBuilder prepareDeleteRequest(@Nullable String id) {
        return client.prepareDelete(instanceIndex, type, id);
    }

    public void commit() {
        if (execute != null) {
            // wait for previous to finish
            long beforeWait = System.currentTimeMillis();
            BulkResponse actionGet = execute.actionGet();
            if (actionGet.hasFailures()) {
                for (BulkItemResponse item : actionGet) {
                    if (item.isFailed()) {
                        throw new RuntimeException("First failure in bulk: " + item.getFailureMessage());
                    }
                }
            }
            long msWaited = System.currentTimeMillis() - beforeWait;
            LOG.info("Previous took in ms " + actionGet.getTook().getMillis() + ", including wait " + msWaited);
            execute = null;
        }
        if (bulk != null) {
            if (bulkDocs > 0) {
                // start a flush, note that this is an asynchronous call
                execute = bulk.execute();
            }
            bulk = null;
        }
        if (createNewBulk) {
            // Prepare a new bulk request
            bulk = client.prepareBulk();
            bulkDocs = 0;
            bulkLength = 0;
        }
    }

    public void close() {
        // Flush pending requests
        LOG.info("Processing remaining requests [docs = " + bulkDocs + ", length = " + bulkLength + ", total docs = " + indexedDocs + "]");
        createNewBulk = false;
        commit();
        // flush one more time to finalize the last bulk
        LOG.info("Processing to finalize last execute");
        createNewBulk = false;
        commit();

        // Close
        client.close();
    }

    private void checkNewBulk() throws IOException {
        indexedDocs++;
        bulkDocs++;

        if (bulkDocs >= maxBulkDocs || bulkLength >= maxBulkLength) {
            // Flush the bulk of indexing requests
            createNewBulk = true;
            commit();
        }
    }

    public String describe() {
        StringBuffer sb = new StringBuffer("ElasticIndexWriter\n");
        sb.append("\t").append(ElasticConstants.CLUSTER).append(" : elastic prefix cluster\n");
        sb.append("\t").append(ElasticConstants.HOST).append(" : " + host + "\n");
        sb.append("\t").append(ElasticConstants.PORT).append(" : " + port + "\n");
        sb.append("\t").append(ElasticConstants.INDEX).append(" : " + instanceIndex + "\n");
        sb.append("\t").append(ElasticConstants.MAX_BULK_DOCS).append(" : " + maxBulkDocs + " (default 250) \n");
        sb.append("\t").append(ElasticConstants.MAX_BULK_LENGTH).append(" : " + maxBulkLength + " (default 2500500 ~2.5MB)\n");
        return sb.toString();
    }

}
