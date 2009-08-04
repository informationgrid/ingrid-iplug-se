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

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.lucene.LuceneWriter;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.plugin.PluginRepository;

public class MetadataIndexingFilter implements IndexingFilter {

  private Configuration _conf;
  private Properties _properties;

  @Override
  public void addIndexBackendOptions(Configuration conf) {
    Set<Object> keySet = _properties.keySet();
    for (Object key : keySet) {
      Object value = _properties.get(key);
      String[] splits = value.toString().split(",");
      LuceneWriter.STORE store = "STORE".equalsIgnoreCase(splits[0]) ? LuceneWriter.STORE.YES
          : LuceneWriter.STORE.NO;
      LuceneWriter.INDEX index = "TOKENIZED".equalsIgnoreCase(splits[1]) ? LuceneWriter.INDEX.TOKENIZED
          : LuceneWriter.INDEX.UNTOKENIZED;
      LuceneWriter.addFieldOptions(key.toString(), store, index, conf);
    }
  }

  @Override
  public NutchDocument filter(NutchDocument doc, Parse parse, Text url,
      CrawlDatum datum, Inlinks inlinks) throws IndexingException {
    ParseData data = parse.getData();
    Enumeration<Object> enumeration = _properties.keys();
    while (enumeration.hasMoreElements()) {
      Object key = (Object) enumeration.nextElement();
      String keyName = key.toString();
      Metadata contentMeta = data.getContentMeta();
      addToDoc(doc, keyName, contentMeta);
      Metadata parseMeta = data.getParseMeta();
      addToDoc(doc, keyName, parseMeta);
    }
    return doc;
  }

  @Override
  public Configuration getConf() {
    return _conf;
  }

  @Override
  public void setConf(Configuration conf) {
    PluginRepository pluginRepository = PluginRepository.get(conf);
    String pluginPath = pluginRepository.getPluginDescriptor("index-metadata")
        .getPluginPath();
    File file = new File(pluginPath, "plugin.properties");
    _properties = new Properties();
    try {
      _properties.load(new FileInputStream(file));
    } catch (Exception e) {
      throw new RuntimeException("plugin.properties not found under ["
          + pluginPath + "]", e);
    }
    _conf = conf;
  }

  private void addToDoc(NutchDocument doc, String keyName, Metadata contentMeta) {
    String[] values = contentMeta.getValues(keyName);
    for (String value : values) {
      doc.add(keyName, value);
    }
  }

}
