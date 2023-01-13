/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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

import de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataWrapper.UrlParseDataContainer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ParseDataUnwrapper extends Configured {

  private static final Logger LOG = LoggerFactory.getLogger(ParseDataUnwrapper.class);

  public static class ParseDataUnwrapperMapper extends Mapper<HostType, ObjectWritable, Text, ParseData> {

    @Override
    public void map(HostType key, ObjectWritable value, Mapper<HostType, ObjectWritable, Text, ParseData>.Context context) throws IOException, InterruptedException {
      UrlParseDataContainer container = (UrlParseDataContainer) value.get();
      Text url = container.getUrl();
      ParseData parseData = container.getParseData();
      context.write(url, parseData);
    }
  }

  public ParseDataUnwrapper(Configuration configuration) {
    super(configuration);
  }

  public void unwrap(Path wrappedParseData, Path out) throws IOException, InterruptedException, ClassNotFoundException {

    Configuration conf = getConf();
    FileSystem fs = FileSystem.get(conf);

    Job convertJob = NutchJob.getInstance(conf);
    convertJob.setJobName("format converting: " + wrappedParseData);
    FileInputFormat.addInputPath(convertJob, wrappedParseData);
    convertJob.setInputFormatClass(SequenceFileInputFormat.class);
    convertJob.setMapperClass(ParseDataUnwrapperMapper.class);
    FileOutputFormat.setOutputPath(convertJob, out);
    FileOutputFormat.setCompressOutput(convertJob, true);
    convertJob.setOutputFormatClass(MapFileOutputFormat.class);
    convertJob.setOutputKeyClass(Text.class);
    convertJob.setOutputValueClass(ParseData.class);
    try {
      boolean success = convertJob.waitForCompletion(true);
      if (!success) {
        String message = "Job did not succeed, job status:"
                + convertJob.getStatus().getState() + ", reason: "
                + convertJob.getStatus().getFailureInfo();
        LOG.error(message);
        throw new RuntimeException(message);
      }
    } catch (IOException | InterruptedException | ClassNotFoundException e) {
      LOG.error("Job failed: {}", e);
      fs.delete(out, true);
      throw e;
    }
  }
}
