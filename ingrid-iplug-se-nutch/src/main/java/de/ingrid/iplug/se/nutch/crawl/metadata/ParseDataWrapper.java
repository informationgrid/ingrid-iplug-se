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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;

public class ParseDataWrapper extends Configured {

    public static final Logger LOG = LoggerFactory.getLogger(ParseDataWrapper.class);

    public static class UrlParseDataContainer implements Writable {

        private Text _url = new Text();

        private ParseData _parseData = new ParseData();

        public UrlParseDataContainer() {
        }

        public UrlParseDataContainer(Text url, ParseData parseData) {
            _url = url;
            _parseData = parseData;
        }

        public Text getUrl() {
            return _url;
        }

        public void setUrl(Text url) {
            _url = url;
        }

        public ParseData getParseData() {
            return _parseData;
        }

        public void setParseData(ParseData parseData) {
            _parseData = parseData;
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            _url.readFields(in);
            _parseData = ParseData.read(in);
        }

        @Override
        public void write(DataOutput out) throws IOException {
            _url.write(out);
            _parseData.write(out);
        }

    }

    public static class ParseDataWrapperMapper extends Mapper<Text, ParseData, HostType, UrlParseDataContainer> {

        @Override
        public void map(Text key, ParseData value, Mapper<Text, ParseData, HostType, UrlParseDataContainer>.Context context) throws IOException, InterruptedException {
            String url = key.toString();
            String host = new URL(url).getHost();
            UrlParseDataContainer container = new UrlParseDataContainer(new Text(url), value);
            HostType hostType = new HostType(new Text(host), HostType.URL_PARSEDATA_CONTAINER);
            context.write(hostType, container);
        }
    }

    public ParseDataWrapper(Configuration conf) {
        super(conf);
    }

    public void wrap(Path segment, Path out) throws IOException, InterruptedException, ClassNotFoundException {

        Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);

        Job job = NutchJob.getInstance(conf);
        job.setJobName("wrap parse data from segment: " + segment);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        FileInputFormat.addInputPath(job, new Path(segment, ParseData.DIR_NAME));

        job.setMapperClass(ParseDataWrapperMapper.class);

        FileOutputFormat.setOutputPath(job, out);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostType.class);
        job.setOutputValueClass(UrlParseDataContainer.class);
        try {
            boolean success = job.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + job.getStatus().getState() + ", reason: "
                        + job.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            fs.delete(out, true);
            throw e;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Configuration configuration = NutchConfiguration.create();
        ParseDataWrapper wrapper = new ParseDataWrapper(configuration);
        wrapper.wrap(new Path(args[0]), new Path(args[1]));
    }
}
