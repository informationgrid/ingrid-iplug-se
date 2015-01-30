/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

package de.ingrid.iplug.se.nutch.crawl.bw;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
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
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

/**
 * Injects black- or white list urls into a database. Urls will be normalized
 * until injection.
 * 
 */
public class BWInjector extends Configured implements Tool {

    private static final Log LOG = LogFactory.getLog(BWInjector.class);

    private static final String PROHIBITED = "prohibited";
    private static final String NORMALIZE = "normalize";
    private static final String ACCEPT_HTTP_AND_HTTPS = "bw.accept.http.and.https";

    public BWInjector(Configuration conf) {
        super(conf);
    }

    public BWInjector() {

    }

    /**
     * Creates {@link HostTypeKey} - {@link BWPatterns} tuples from each text
     * line.
     */
    public static class BWInjectMapper implements Mapper<WritableComparable<Object>, Text, HostTypeKey, BWPatterns> {

        private URLNormalizers _urlNormalizers;

        private JobConf jobConf;

        private boolean _prohibited;
        private boolean _normalize;
        private boolean _acceptHttpAndHttps;

        public void configure(JobConf job) {
            this.jobConf = job;
            _urlNormalizers = new URLNormalizers(jobConf, URLNormalizers.SCOPE_INJECT);
            _prohibited = jobConf.getBoolean(PROHIBITED, true);
            _normalize = jobConf.getBoolean(NORMALIZE, true);
            _normalize = jobConf.getBoolean(NORMALIZE, true);
            _acceptHttpAndHttps = jobConf.getBoolean(ACCEPT_HTTP_AND_HTTPS, false);
        }

        public void close() {
        }

        @Override
        public void map(WritableComparable<Object> key, Text val, OutputCollector<HostTypeKey, BWPatterns> output, Reporter reporter) throws IOException {

            String url = val.toString();
            if (_normalize) {
                try {
                    url = _urlNormalizers.normalize(url, "bw"); // normalize
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e.toString());
                    url = null;
                }
            }
            if (url != null) {
                String host;
                try {
                    host = new URL(url).getHost();
                } catch (Exception e) {
                    LOG.warn("unable to get host from: " + url + " : " + e.toString());
                    return;
                }
                if (_acceptHttpAndHttps) {
                    url = url.replace("http:", "htt(p|ps):");
                    url = url.replace("https:", "htt(p|ps):");
                }
                BWPatterns patterns;
                if (_prohibited) {
                    patterns = new BWPatterns(new Text[0], new Text[] { new Text(url) });
                } else {
                    patterns = new BWPatterns(new Text[] { new Text(url) }, new Text[0]);
                }
                output.collect(new HostTypeKey(host, HostTypeKey.PATTERN_TYPE), patterns);
            }
        }

    }

    /**
     * Reduces {@link HostTypeKey} - {@link BWPatterns} tuples
     */
    public static class BWInjectReducer implements Reducer<HostTypeKey, BWPatterns, HostTypeKey, BWPatterns> {
        public void configure(JobConf job) {
        }

        public void close() {
        }

        @Override
        public void reduce(HostTypeKey key, Iterator<BWPatterns> values, OutputCollector<HostTypeKey, BWPatterns> output, Reporter reporter) throws IOException {
            Set<Text> pos = new HashSet<Text>();
            Set<Text> neg = new HashSet<Text>();

            while (values.hasNext()) {
                BWPatterns value = values.next();
                pos.addAll(value.getPositive());
                neg.addAll(value.getNegative());
            }

            Text[] negative = neg.toArray(new Text[neg.size()]);
            Text[] positive = pos.toArray(new Text[pos.size()]);

            output.collect(key, new BWPatterns(positive, negative));
        }
    }

    public void inject(Path bwDb, Path urlDir, boolean prohibited) throws IOException {

        LOG.info("BWInjector: starting");
        if (prohibited) {
            LOG.info("BWInjector: injecting black list urls");
        } else {
            LOG.info("BWInjector: injecting white list urls");
        }

        LOG.info("BWInjector: bwDb: " + bwDb);
        LOG.info("BWInjector: urlDir: " + urlDir);

        String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        name = "/bw-inject-temp-" + name;
        Path tempDir = new Path(getConf().get("mapred.temp.dir", ".") + name);

        JobConf sortJob = new NutchJob(getConf());
        sortJob.setJobName("bw-inject " + urlDir);
        FileInputFormat.addInputPath(sortJob, urlDir);
        sortJob.setMapperClass(BWInjectMapper.class);

        sortJob.setBoolean(PROHIBITED, prohibited);
        sortJob.setBoolean(NORMALIZE, false);

        FileOutputFormat.setOutputPath(sortJob, tempDir);
        sortJob.setOutputFormat(SequenceFileOutputFormat.class);
        sortJob.setOutputKeyClass(HostTypeKey.class);
        sortJob.setOutputValueClass(BWPatterns.class);
        JobClient.runJob(sortJob);

        // merge with existing bw db
        LOG.info("BWInjector: Merging injected urls into bwDb.");
        String newDbName = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path newDb = new Path(bwDb, newDbName);

        JobConf job = new NutchJob(getConf());
        job.setJobName("merge bwDb " + bwDb);

        Path current = new Path(bwDb, "current");
        if (FileSystem.get(job).exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }
        FileInputFormat.addInputPath(job, tempDir);
        job.setInputFormat(SequenceFileInputFormat.class);

        job.setReducerClass(BWInjectReducer.class);
        FileOutputFormat.setOutputPath(job, newDb);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(BWPatterns.class);
        JobClient.runJob(job);

        LOG.info("rename bwdb");
        FileSystem fs = new JobClient(job).getFs();
        Path old = new Path(bwDb, "old");
        fs.delete(old, true);
        if (fs.exists(current)) {
            fs.rename(current, old);
        }
        fs.rename(newDb, current);
        fs.delete(old, true);

        // clean up
        fs.delete(tempDir, true);
        LOG.info("BWInjector: done");
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new BWInjector(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: BWInjector <bwdb> <urls-white> <urls-black>");
            return -1;
        }
        try {
            Path bwdb = new Path(args[0]);
            LocalFileSystem localFS = FileSystem.getLocal(getConf());
            if (localFS.exists(bwdb)) {
                localFS.delete(bwdb, true);
            }

            // inject white urls
            inject(new Path(args[0]), new Path(args[1]), false);
            // inject black urls
            inject(new Path(args[0]), new Path(args[2]), true);
            return 0;
        } catch (Exception e) {
            LOG.error("BWInjector: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
