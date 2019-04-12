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

import de.ingrid.iplug.se.nutch.tools.IBusElasticsearchClient;
import de.ingrid.iplug.se.nutch.tools.IngridElasticSearchClient;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ElasticIndexWriter implements IndexWriter {
    public static Logger LOG = LoggerFactory.getLogger( ElasticIndexWriter.class );

    private Configuration config;

    private IngridElasticSearchClient client = null;
    private IBusElasticsearchClient clientiBus = null;

    private Map<String, Object> dependingFieldsMap = new HashMap<>();
    private boolean useIBusCommunication;

    @Override
    public void open(JobConf job, String name) throws IOException {
        if (useIBusCommunication) {
            try {
                clientiBus = new IBusElasticsearchClient(job);
            } catch (Exception e) {
                LOG.error("Error initializing IBusElasticsearchClient", e);
                throw new RuntimeException(e);
            }
        }
        client = new IngridElasticSearchClient( job );
    }

    @Override
    public void write(NutchDocument doc) throws IOException {
        String id = (String) doc.getFieldValue("id");

        Map<String, Object> source = new HashMap<>();
        int requestLength = 0;

        // Loop through all fields of this doc
        for (String fieldName : doc.getFieldNames()) {
            if (doc.getField(fieldName).getValues().size() > 1) {
                if (fieldName.equalsIgnoreCase("boost")) {
                    source.put("doc_boost", doc.getField(fieldName).getValues());
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Adding field: " + fieldName + " with value: " + doc.getField(fieldName).getValues());
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

        // add basic information to dataset to identify source for central index
        source.put("dataSourceName", config.get("iplug.datasource.name", "unknown iPlug"));
        source.put("iPlugId", config.get("iplug.id", "unknown"));

        // dynamically add fields depending on other fields
        requestLength += addDependentFields( source );

        // facet search requires looks for other fields, which we need to map
        mapTopicFieldValues(source, doc);

        if (useIBusCommunication) {
            clientiBus.update(source);

        } else {

            IndexRequestBuilder request = client.prepareIndexRequest(id);
            request.setSource(source);

            // Add this indexing request to a bulk request
            client.addRequest(request, requestLength);

        }
    }

    // also add mapped index field for topics (used for facets in central index)
    private void mapTopicFieldValues(Map<String, Object> source, NutchDocument doc) {

        List<Object> values = new ArrayList<>();

        String[] types = new String[] {"topic", "measure", "service"};
        for (String type : types) {
            if (doc.getField(type) != null) {
                if (doc.getField(type).getValues().size() > 1) {
                    values.addAll(doc.getField(type).getValues());
                } else {
                    values.add(doc.getFieldValue(type));
                }
            }
        }

        if (values.size() > 0) {
            if (values.size() == 1) {
                source.put("topic", values.get(0));
            } else {
                source.put("topic", values);
            }
        }
    }

    /**
     * Add a new field to the index depending on another given field. This is configured
     * in the config file.
     * key:value->otherKey:otherValue, where key and value can be "*" to be ignored
     * @param source is the new index document which is going to be written 
     * @return the number of additionally written bytes
     */
    @SuppressWarnings("unchecked")
    private int addDependentFields(Map<String, Object> source) {
        int requestLength = 0;
        for (String key : dependingFieldsMap.keySet()) {
            String[] split = key.split( ":" );
            String val = (String) dependingFieldsMap.get( key );
            String[] targetSplitted = val.split( ":" );
            // if the key doesn't matter we only check for the value
            if ("*".equals( split[0] )) {
                // if the value also does not matter, we can add the depending value to all documents
                if ("*".equals( split[1] )) {
                    addToSource( source, targetSplitted[0], targetSplitted[1] );
                    requestLength += targetSplitted[1].length();
                } else {
                    // otherwise we check if the value matches before we add the depending value
                    if (source.containsValue( split[1] )) {
                        addToSource( source, targetSplitted[0], targetSplitted[1] );
                        requestLength += targetSplitted[1].length();
                    }
                }
            } else {
                // check if source contains the wanted key
                if (source.containsKey( split[0] )) {
                    // if we don't need to check for a given value, we just can write the depending value
                    if ("*".equals( split[1] )) {
                        addToSource( source, targetSplitted[0], targetSplitted[1] );
                        requestLength += targetSplitted[1].length();
                    } else {
                        Object origValue = source.get( split[0] );
                        boolean add = false;
                        // otherwise we check if key AND value match, before we add the depending value
                        // check firt if the field is a list
                        if (origValue instanceof ArrayList) {
                            if (((ArrayList<String>) origValue).contains( split[1] )) {
                                add = true;
                            }
                        // or if it's a simple string
                        } else if (split[1].equals( origValue ) ) {
                            add = true;
                        }
                        
                        if (add) {
                            addToSource( source, targetSplitted[0], targetSplitted[1] );
                            requestLength += targetSplitted[1].length();
                        }
                    }
                }
                
            }
        }
        return requestLength;
    }

    @SuppressWarnings("unchecked")
    private void addToSource(Map<String, Object> source, String key, String value) {
        Object sourceValue = source.get( key );
        if (sourceValue == null) {
            source.put( key, value );
        } else if (sourceValue instanceof ArrayList) {
            ((ArrayList<String>) sourceValue).add( value );
        } else {
            ArrayList<String> newValues = new ArrayList<>();
            newValues.add( (String) sourceValue );
            newValues.add( value );
            source.put( key, newValues );
        }
        
    }

    @Override
    public void delete(String key) throws IOException {

        if (useIBusCommunication) {
            clientiBus.deleteDoc(key);
        } else {
            try {
                DeleteRequestBuilder request = client.prepareDeleteRequest(key);
                client.addRequest(request, key.length());
            } catch (ElasticsearchException e) {
                throw makeIOException(e);
            }
        }

    }

    public static IOException makeIOException(ElasticsearchException e) {
        final IOException ioe = new IOException();
        ioe.initCause( e );
        return ioe;
    }

    @Override
    public void update(NutchDocument doc) throws IOException {
        write( doc );
    }

    @Override
    public void commit() throws IOException {

        if (!useIBusCommunication) {
            client.commit();
        }
    }

    @Override
    public void close() throws IOException {
        if (useIBusCommunication) {
            try {
                clientiBus.close();
            } catch (Exception e) {
                LOG.error("Error closing iBus connection", e);
            }
        } else {
            client.close();
        }
    }

    @Override
    public String describe() {

        if (useIBusCommunication) {
            return "Using iBus communication for Elasticsearch operations";
        } else {
            if (client != null) {
                return client.describe();
            } else {
                return "not initialized yet.";
            }
        }

    }

    @Override
    public void setConf(Configuration conf) {
        config = conf;
        String cluster = conf.get( ElasticConstants.CLUSTER );
        String host = conf.get( ElasticConstants.HOST );

        if (StringUtils.isBlank( cluster ) && StringUtils.isBlank( host )) {
            String message = "Missing elastic.cluster and elastic.host. At least one of them should be set in nutch-site.xml ";
            message += "\n" + describe();
            LOG.error( message );
            throw new RuntimeException( message );
        }

        String staticFields = conf.get( ElasticConstants.DEPENDING_FIELDS );

        if (staticFields != null) {
            // separate entries
            String[] entries = staticFields.split( "," );
            for (String entry : entries) {
                // separate keys from values
                String[] keyValue = entry.split( "->" );
                dependingFieldsMap.put( keyValue[0], keyValue[1] );
            }
        }

        useIBusCommunication = "true".equals(config.get("use.elastic.with.ibus"));
    }

    @Override
    public Configuration getConf() {
        return config;
    }
}
