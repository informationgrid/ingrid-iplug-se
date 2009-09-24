/**
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

package org.apache.nutch.indexer.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.lucene.LuceneWriter;
import org.apache.nutch.indexer.lucene.LuceneWriter.INDEX;
import org.apache.nutch.indexer.lucene.LuceneWriter.STORE;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRepository;

public class MetadataIndexingFilter implements IndexingFilter {

  private Configuration _conf;

  private List<String> _fields = new ArrayList<String>();

  @Override
  public void addIndexBackendOptions(Configuration conf) {
    PluginRepository pluginRepository = PluginRepository.get(conf);
    PluginDescriptor pluginDescriptor = pluginRepository
            .getPluginDescriptor("index-metadata");

    Extension[] extensions = pluginDescriptor.getExtensions();
    for (Extension extension : extensions) {
      if (MetadataQueryFilter.class.getSimpleName().equals(extension.getId())) {
        // add raw fields query filter
        String rawFields = extension.getAttribute("raw-fields");
        if (rawFields != null) {
          String[] splits = rawFields.split(",");
          for (String split : splits) {
            _fields.add(split.trim());
            LuceneWriter.addFieldOptions(split.trim(), STORE.YES,
                    INDEX.UNTOKENIZED, conf);
          }
        }
        // add fields query filter
        String fields = extension.getAttribute("fields");
        if (fields != null) {
          String[] splits = fields.split(",");
          for (String split : splits) {
            _fields.add(split.trim());
            LuceneWriter.addFieldOptions(split.trim(), STORE.YES,
                    INDEX.TOKENIZED, conf);
          }
        }
      }
    }
  }

  @Override
  public NutchDocument filter(NutchDocument doc, Parse parse, Text url,
          CrawlDatum datum, Inlinks inlinks) throws IndexingException {
    ParseData data = parse.getData();
    // process content metadata
    Metadata contentMeta = data.getContentMeta();
    extractMetadata(doc, contentMeta);
    // process parse metadata
    Metadata parseMeta = data.getParseMeta();
    extractMetadata(doc, parseMeta);
    return doc;
  }

  @Override
  public Configuration getConf() {
    return _conf;
  }

  @Override
  public void setConf(Configuration conf) {
    _conf = conf;
  }

  private void extractMetadata(NutchDocument doc, Metadata metadata) {
    for (String field : _fields) {
      addToDoc(doc, field, metadata);
    }
  }

  private void addToDoc(NutchDocument doc, String keyName, Metadata metadata) {
    String[] values = metadata.getValues(keyName);
    for (String value : values) {
      doc.add(keyName, value);
    }
  }

}
