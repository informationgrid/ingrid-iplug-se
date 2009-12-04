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

package org.apache.nutch.crawl.metadata;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.nutch.crawl.metadata.MetadataInjector.MetadataContainer;
import org.apache.nutch.crawl.metadata.ParseDataWrapper.UrlParseDataContainer;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.NutchJob;

public class MetadataMerger extends Configured {

  public static final Log LOG = LogFactory.getLog(MetadataMerger.class);

  public static class ObjectWritableMapper implements Mapper<HostType, Writable, HostType, ObjectWritable> {

    @Override
    public void map(HostType key, Writable value, OutputCollector<HostType, ObjectWritable> collector, Reporter reporter)
        throws IOException {
      ObjectWritable objectWritable = new ObjectWritable(value);
      collector.collect(key, objectWritable);
    }

    @Override
    public void configure(JobConf jobConf) {
    }

    @Override
    public void close() throws IOException {
    }

  }

  public static class MetadataReducer implements Reducer<HostType, ObjectWritable, HostType, ObjectWritable> {

    private MetadataContainer _metadataContainer;

    public void reduce(HostType key, Iterator<ObjectWritable> values, OutputCollector<HostType, ObjectWritable> out,
        Reporter report) throws IOException {

      while (values.hasNext()) {
        ObjectWritable obj = (ObjectWritable) values.next();
        Object value = obj.get(); // unwrap

        if (value instanceof MetadataContainer) {
          _metadataContainer = (MetadataContainer) value;
          return;
        }

        if (_metadataContainer != null) {
          // for this HostType a MetadataContainer exists
          UrlParseDataContainer urlParseDataContainer = (UrlParseDataContainer) value;
          Text url = urlParseDataContainer.getUrl();
          ParseData parseData = urlParseDataContainer.getParseData();
          Metadata metadataFromSegment = parseData.getParseMeta();

          for (Metadata metadata : _metadataContainer.getMetadatas()) {
            String pattern = metadata.get("pattern");
            if (url.toString().startsWith(pattern)) {
              String[] names = metadata.names();
              for (String name : names) {
                String[] metadataValues = metadata.getValues(name);
                for (String metadataValue : metadataValues) {
                  metadataFromSegment.add(name, metadataValue);
                }
              }
            }
          }
        } else {
          // for this HostType no MetadataContainer exist
          LOG.warn("No MetadataContainer found for: " + ((UrlParseDataContainer) value).getUrl());
        }

        out.collect(key, obj);
      }
    }

    public void configure(JobConf arg0) {
    }

    public void close() throws IOException {
    }

  }

  public MetadataMerger(Configuration conf) {
    super(conf);
  }

  public void merge(Path metadataDb, Path wrappedParseData, Path out) throws IOException {
    LOG.info("metadata update: merge started.");
    JobConf mergeJob = new NutchJob(getConf());
    mergeJob.setJobName("merging: " + metadataDb + " and " + wrappedParseData);
    mergeJob.setInputFormat(SequenceFileInputFormat.class);

    Path metadataDbPath = new Path(metadataDb, "current");
    // LOG.info("Merge job uses input pathes: '" + wrappedParseData + "' and '"
    // + metadataDbPath + "'.");
    FileInputFormat.addInputPath(mergeJob, wrappedParseData);
    FileInputFormat.addInputPath(mergeJob, metadataDbPath);
    FileOutputFormat.setOutputPath(mergeJob, out);
    mergeJob.setMapperClass(ObjectWritableMapper.class);
    mergeJob.setReducerClass(MetadataReducer.class);
    mergeJob.setOutputFormat(MapFileOutputFormat.class);
    mergeJob.setOutputKeyClass(HostType.class);
    mergeJob.setOutputValueClass(ObjectWritable.class);
    JobClient.runJob(mergeJob);
  }

}
