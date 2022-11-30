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

package de.ingrid.iplug.se.nutch.crawl.metadata;

import de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector.MetadataContainer;
import de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataWrapper.UrlParseDataContainer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetadataMerger extends Configured {

  public static final Logger LOG = LoggerFactory.getLogger(MetadataMerger.class);

  public static class ObjectWritableMapper extends Mapper<HostType, Writable, HostType, ObjectWritable> {

    @Override
    public void map(HostType key, Writable value, Mapper<HostType, Writable, HostType, ObjectWritable>.Context context)
            throws IOException, InterruptedException {
      ObjectWritable objectWritable = new ObjectWritable(value);
      context.write(key, objectWritable);
    }
  }

  public static class MetadataReducer extends Reducer<HostType, ObjectWritable, HostType, ObjectWritable> {

    private MetadataContainer _metadataContainer;
    private final Map<String, Pattern> patternCache = new HashMap<>();

    public void reduce(HostType key, Iterable<ObjectWritable> values, Reducer<HostType, ObjectWritable, HostType, ObjectWritable>.Context context) throws IOException, InterruptedException {

      for (ObjectWritable obj : values) {
        Object value = obj.get(); // unwrap

        if (value instanceof MetadataContainer) {
          _metadataContainer = (MetadataContainer) value;
          patternCache.clear();
          for (Metadata metadata : _metadataContainer.getMetadatas()) {
            String patternString = metadata.get("pattern");
            patternCache.put(patternString, Pattern.compile(patternString));
          }
          continue;
        }

        if (_metadataContainer != null) {
          // for this HostType a MetadataContainer exists
          UrlParseDataContainer urlParseDataContainer = (UrlParseDataContainer) value;
          Text url = urlParseDataContainer.getUrl();
          ParseData parseData = urlParseDataContainer.getParseData();
          Metadata metadataFromSegment = parseData.getParseMeta();

          for (Metadata metadata : _metadataContainer.getMetadatas()) {
            String patternString = metadata.get("pattern");
            Pattern p = patternCache.get(patternString);
            Matcher m = p.matcher(url.toString());
            if (m.find()) {
              String[] names = metadata.names();
              for (String name : names) {
                String[] metadataValues = metadata.getValues(name);
                for (String metadataValue : metadataValues) {
                  // check if metadata already exists
                  String[] mdValues = metadataFromSegment.getValues(name);
                  boolean mdExists = false;
                  if (mdValues.length > 0) {
                    for (String v : mdValues) {
                      if (v.equals(metadataValue)) {
                        mdExists = true;
                      }
                    }
                  }
                  if (!mdExists) {
                    metadataFromSegment.add(name, metadataValue);
                  }
                }
              }
            }
          }
        } else {
          // for this HostType no MetadataContainer exist
          LOG.info("No MetadataContainer found for: " + ((UrlParseDataContainer) value).getUrl());
        }

        context.write(key, obj);

      }
    }

  }

  public MetadataMerger(Configuration conf) {
    super(conf);
  }

  public void merge(Path metadataDb, Path wrappedParseData, Path out) throws IOException, InterruptedException, ClassNotFoundException {
    LOG.info("metadata update: merge started.");

    Configuration conf = getConf();
    FileSystem fs = FileSystem.get(conf);

    Job mergeJob = NutchJob.getInstance(conf);
    mergeJob.setJobName("merging: " + metadataDb + " and " + wrappedParseData);
    mergeJob.setInputFormatClass(SequenceFileInputFormat.class);

    Path metadataDbPath = new Path(metadataDb, "current");
    // LOG.info("Merge job uses input pathes: '" + wrappedParseData + "' and '"
    // + metadataDbPath + "'.");
    FileInputFormat.addInputPath(mergeJob, wrappedParseData);
    FileInputFormat.addInputPath(mergeJob, metadataDbPath);
    FileOutputFormat.setOutputPath(mergeJob, out);
    mergeJob.setMapperClass(ObjectWritableMapper.class);
    mergeJob.setReducerClass(MetadataReducer.class);
    mergeJob.setOutputFormatClass(MapFileOutputFormat.class);
    mergeJob.setOutputKeyClass(HostType.class);
    mergeJob.setOutputValueClass(ObjectWritable.class);
    Path lock = CrawlDb.lock(getConf(), wrappedParseData, false);
    try {
      boolean success = mergeJob.waitForCompletion(true);
      if (!success) {
        String message = "Job did not succeed, job status:"
                + mergeJob.getStatus().getState() + ", reason: "
                + mergeJob.getStatus().getFailureInfo();
        LOG.error(message);
        throw new RuntimeException(message);
      }
    } catch (IOException | InterruptedException | ClassNotFoundException e) {
      LOG.error("Job failed: {}", e);
      fs.delete(out, true);
      throw e;
    } finally {
      LockUtil.removeLockFile(fs, lock);
    }
  }

}
