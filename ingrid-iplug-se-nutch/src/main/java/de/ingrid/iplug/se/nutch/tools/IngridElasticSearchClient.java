/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.nutch.tools;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.DeleteOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.TransportUtils;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.hadoop.conf.Configuration;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.nutch.indexer.IndexWriterParams;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class IngridElasticSearchClient {

    public static Logger LOG = LoggerFactory.getLogger(IngridElasticSearchClient.class);

    private static final int DEFAULT_MAX_BULK_DOCS = 250;
    private static final int DEFAULT_MAX_BULK_LENGTH = 2500500;

    private ElasticsearchClient client;
    private String instanceIndex;

    private BulkIngester<String> bulk;
    private int port = -1;
    private String host = null;
    private String clusterName = null;
    private int maxBulkDocs;
    private int maxBulkLength;
    private long indexedDocs = 0;
    private int bulkDocs = 0;
    private int bulkLength = 0;

    private IndexWriterParams params = null;


    public IngridElasticSearchClient(Configuration conf) throws IOException {

        this.params = new IndexWriterParams(new HashMap<>());

        Iterator<Map.Entry<String, String>> it = conf.iterator();

        while(it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            this.params.put(entry.getKey(), entry.getValue());
        }

        init(this.params);
    }

    public IngridElasticSearchClient(IndexWriterParams parameters ) throws IOException {
        if (this.params == null) {
            init(parameters);
        }
    }


    private void init( IndexWriterParams parameters ) throws IOException {
        this.clusterName = parameters.get(ElasticConstants.CLUSTER);
        if (clusterName == null) LOG.warn( "No cluster name specified! If node cannot be found, then we might search in the wrong cluster." );

        this.host = parameters.get(ElasticConstants.HOST);
        this.port = parameters.getInt(ElasticConstants.PORT, 9200);
        String username = parameters.get(ElasticConstants.USERNAME);
        String password = parameters.get(ElasticConstants.PASSWORD);
        boolean ssl = parameters.getBoolean(ElasticConstants.SSL, false);

        String[] remoteHosts = new String[]{host + ":" + port};

        // Prefer TransportClient
        if (host != null && port > 1) {
            client = createTransportClient(remoteHosts, username, password, ssl);
        } else if (clusterName != null) {
            throw new RuntimeException("TransportClient not created since host and/or port not defined.");
        }

        bulk = BulkIngester.of(bi -> bi
                .client(client)
                .listener(getBulkProcessorListener())
                .flushInterval(5L, TimeUnit.SECONDS));

        instanceIndex = parameters.get(ElasticConstants.INDEX, "nutch");
        maxBulkDocs = parameters.getInt(ElasticConstants.MAX_BULK_DOCS, DEFAULT_MAX_BULK_DOCS);
        maxBulkLength = parameters.getInt(ElasticConstants.MAX_BULK_LENGTH, DEFAULT_MAX_BULK_LENGTH);
    }

    private BulkListener<String> getBulkProcessorListener() {
        return new BulkListener<>() {

            @Override
            public void beforeBulk(long executionId, BulkRequest request, List<String> contexts) {
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<String> contexts, BulkResponse response) {
                // The request was accepted, but may contain failed items.
                // The "context" list gives the file name for each bulk item.
                for (int i = 0; i < contexts.size(); i++) {
                    BulkResponseItem item = response.items().get(i);
                    if (item.error() != null) {
                        // Inspect the failure cause
                        LOG.error("Failed to index file " + contexts.get(i) + " - " + item.error().reason());
                    }
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, List<String> contexts, Throwable failure) {
                // The request could not be sent
                LOG.error("Bulk request " + executionId + " failed", failure);
            }
        };
    }

    public ElasticsearchClient createTransportClient(String[] remoteHosts, String username, String password, Boolean ssl) throws IOException {
        if (this.client != null) {
            client.shutdown();
        }

        List<HttpHost> hosts = new ArrayList<>();
        for (String host : remoteHosts) {
            hosts.add(HttpHost.create(host));
        }

        final CredentialsProvider credentialsProvider = getCredentialsProvider(username, password);

        SSLContext sslContext;
        if (ssl) {
            Path caCertificatePath = Paths.get("elasticsearch-ca.pem");
            sslContext = TransportUtils.sslContextFromHttpCaCrt(caCertificatePath.toFile());
        } else {
            sslContext = null;
        }

        // Create the low-level client
        SSLContext finalSslContext = sslContext;
        RestClient restClient = RestClient
                .builder(hosts.toArray(new HttpHost[0]))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                            httpClientBuilder.setSSLContext(finalSslContext);
                            return httpClientBuilder;
                        }
                )
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }

    private static CredentialsProvider getCredentialsProvider(String username, String password) {
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));
            return credentialsProvider;
        } else return null;
    }

    public void addRequest(BulkOperation request) throws IOException {
        // Add this indexing request to a bulk request
        bulk.add(request);
        checkNewBulk();
    }

    public BulkOperation prepareIndexRequest(String id, Map source) {
        IndexOperation.Builder<Map> operation = new IndexOperation.Builder<Map>()
                .index(instanceIndex)
                .id(id)
                .document(source);

        return BulkOperation.of(b -> b.index(operation.build()));
    }

    public BulkOperation prepareDeleteRequest(String id) {
        return BulkOperation.of(bulk -> bulk
                .delete(DeleteOperation.of(x -> x.index(instanceIndex).id(id)))
        );
    }

    public void commit() {
        bulk.flush();
        /*if (execute != null) {
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
        }*/
    }

    public void close() {
        // Flush pending requests
        LOG.info("Processing remaining requests [docs = " + bulkDocs + ", length = " + bulkLength + ", total docs = " + indexedDocs + "]");
//        commit();
        // flush one more time to finalize the last bulk
        LOG.info("Processing to finalize last execute");
        commit();

        // Close
        client.shutdown();
    }

    private void checkNewBulk() {
        indexedDocs++;
        bulkDocs++;

        if (bulkDocs >= maxBulkDocs || bulkLength >= maxBulkLength) {
            // Flush the bulk of indexing requests
//            createNewBulk = true;
            commit();
        }
    }

    public Map<String, Map.Entry<String, Object>> describe() {
        Map<String, Map.Entry<String, Object>> properties = new LinkedHashMap<>();

        properties.put(ElasticConstants.CLUSTER,
                new AbstractMap.SimpleEntry<>("elastic prefix cluster.", clusterName));
        properties.put(ElasticConstants.HOST,
                new AbstractMap.SimpleEntry<>("Name of ElasticSearch host.",host));
        properties.put(ElasticConstants.PORT,
                new AbstractMap.SimpleEntry<>("The port to connect to elastic server.",port));
        properties.put(ElasticConstants.INDEX,
                new AbstractMap.SimpleEntry<>("Default index to send documents to.",instanceIndex));
        properties.put(ElasticConstants.MAX_BULK_DOCS,
                new AbstractMap.SimpleEntry<>("Maximum size of the bulk in number of documents (default 250).",maxBulkDocs));
        properties.put(ElasticConstants.MAX_BULK_LENGTH,
                new AbstractMap.SimpleEntry<>("Maximum size of the bulk in bytes. (default 2500500 ~2.5MB).",maxBulkLength));

        return properties;
    }

}
