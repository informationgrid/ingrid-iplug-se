/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.indexwriter.elastic;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.nutch.indexer.IndexWriter;
import org.apache.nutch.indexer.NutchDocument;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ingrid.iplug.se.nutch.tools.IngridElasticSearchClient;

/**
 */
public class ElasticIndexWriter implements IndexWriter {
    public static Logger LOG = LoggerFactory.getLogger(ElasticIndexWriter.class);

    private Configuration config;

    private IngridElasticSearchClient client = null;

    @Override
    public void open(JobConf job, String name) throws IOException {

        client = new IngridElasticSearchClient(job);
    }

    @Override
    public void write(NutchDocument doc) throws IOException {
        String id = (String) doc.getFieldValue("id");
        IndexRequestBuilder request = client.prepareIndexRequest(id);

        Map<String, Object> source = new HashMap<String, Object>();
        int requestLength = 0;

        // Loop through all fields of this doc
        for (String fieldName : doc.getFieldNames()) {
            if (doc.getField(fieldName).getValues().size() > 1) {
                if (fieldName.equalsIgnoreCase("boost")) {
                    source.put("doc_boost", doc.getField(fieldName).getValues());
                }
                source.put(fieldName, doc.getField(fieldName).getValues());
                // Loop through the values to keep track of the size of this
                // document
                for (Object value : doc.getField(fieldName).getValues()) {
                    requestLength += value.toString().length();
                }
            } else {
                if (fieldName.equalsIgnoreCase("boost")) {
                    source.put("doc_boost", doc.getField(fieldName).getValues());
                }
                source.put(fieldName, doc.getFieldValue(fieldName));
                requestLength += doc.getFieldValue(fieldName).toString().length();
            }
        }
        request.setSource(source);

        // Add this indexing request to a bulk request
        client.addRequest(request, requestLength);
    }

    @Override
    public void delete(String key) throws IOException {
        try {
            DeleteRequestBuilder request = client.prepareDeleteRequest(key);
            client.addRequest(request, key.length());
        } catch (ElasticsearchException e) {
            throw makeIOException(e);
        }
    }

    public static IOException makeIOException(ElasticsearchException e) {
        final IOException ioe = new IOException();
        ioe.initCause(e);
        return ioe;
    }

    @Override
    public void update(NutchDocument doc) throws IOException {
        write(doc);
    }

    @Override
    public void commit() throws IOException {
        client.commit();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public String describe() {
        if (client != null) {
            return client.describe();
        } else {
            return "not initialized yet.";
        }
    }

    @Override
    public void setConf(Configuration conf) {
        config = conf;
        String cluster = conf.get(ElasticConstants.CLUSTER);
        String host = conf.get(ElasticConstants.HOST);

        if (StringUtils.isBlank(cluster) && StringUtils.isBlank(host)) {
            String message = "Missing elastic.cluster and elastic.host. At least one of them should be set in nutch-site.xml ";
            message += "\n" + describe();
            LOG.error(message);
            throw new RuntimeException(message);
        }
    }

    @Override
    public Configuration getConf() {
        return config;
    }
}
