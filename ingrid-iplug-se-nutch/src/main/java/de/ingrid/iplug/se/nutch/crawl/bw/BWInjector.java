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

package de.ingrid.iplug.se.nutch.crawl.bw;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Injects black- or white list urls into a database. Urls will be normalized
 * until injection.
 * 
 */
public class BWInjector extends Configured implements Tool {

    private static final Logger LOG = LoggerFactory.getLogger(BWInjector.class);

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
    public static class BWInjectMapper extends Mapper<WritableComparable<Object>, Text, HostTypeKey, BWPatterns> {

        private URLNormalizers _urlNormalizers;

        private boolean _prohibited;
        private boolean _normalize;
        private boolean _acceptHttpAndHttps;

        @Override
        protected void setup(Mapper<WritableComparable<Object>, Text, HostTypeKey, BWPatterns>.Context context) throws IOException, InterruptedException {
            super.setup(context);

            Configuration conf = context.getConfiguration();
            _urlNormalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_INJECT);
            _prohibited = conf.getBoolean(PROHIBITED, true);
            _normalize = conf.getBoolean(NORMALIZE, true);
            _normalize = conf.getBoolean(NORMALIZE, true);
            _acceptHttpAndHttps = conf.getBoolean(ACCEPT_HTTP_AND_HTTPS, false);
        }

        @Override
        public void map(WritableComparable<Object> key, Text val, Mapper<WritableComparable<Object>, Text, HostTypeKey, BWPatterns>.Context context) throws IOException, InterruptedException {

            String url = val.toString();
            if (_normalize) {
                try {
                    url = _urlNormalizers.normalize(url, "bw"); // normalize
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e);
                    url = null;
                }
            }
            if (url != null) {
                String host;
                try {
                    host = new URL(url).getHost();
                } catch (Exception e) {
                    LOG.warn("unable to get host from: " + url + " : " + e);
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
                context.write(new HostTypeKey(host, HostTypeKey.PATTERN_TYPE), patterns);
            }
        }

    }

    /**
     * Reduces {@link HostTypeKey} - {@link BWPatterns} tuples
     */
    public static class BWInjectReducer extends Reducer<HostTypeKey, BWPatterns, HostTypeKey, BWPatterns> {

        @Override
        public void reduce(HostTypeKey key, Iterable<BWPatterns> values, Reducer<HostTypeKey, BWPatterns, HostTypeKey, BWPatterns>.Context context) throws IOException, InterruptedException {
            Set<Text> pos = new HashSet<>();
            Set<Text> neg = new HashSet<>();

            for (BWPatterns value : values) {
                pos.addAll(value.getPositive());
                neg.addAll(value.getNegative());
            }

            Text[] negative = neg.toArray(new Text[0]);
            Text[] positive = pos.toArray(new Text[0]);

            context.write(key, new BWPatterns(positive, negative));
        }
    }

    public void inject(Path bwDb, Path urlDir, boolean prohibited) throws IOException, InterruptedException, ClassNotFoundException {

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

        Job sortJob = NutchJob.getInstance(getConf());
        Configuration sortJobConf = sortJob.getConfiguration();
        FileSystem fs = FileSystem.get(sortJobConf);

        sortJob.setJobName("bw-inject " + urlDir);
        FileInputFormat.addInputPath(sortJob, urlDir);
        sortJob.setMapperClass(BWInjectMapper.class);

        sortJobConf.setBoolean(PROHIBITED, prohibited);

        FileOutputFormat.setOutputPath(sortJob, tempDir);
        sortJob.setOutputFormatClass(SequenceFileOutputFormat.class);
        sortJob.setOutputKeyClass(HostTypeKey.class);
        sortJob.setOutputValueClass(BWPatterns.class);
        Path lock;
        lock = CrawlDb.lock(getConf(), urlDir, false);
        try {
            boolean success = sortJob.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + sortJob.getStatus().getState() + ", reason: "
                        + sortJob.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            throw e;
        } finally {
            LockUtil.removeLockFile(fs, lock);
        }

        // merge with existing bw db
        LOG.info("BWInjector: Merging injected urls into bwDb.");
        String newDbName = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path newDb = new Path(bwDb, newDbName);

        Job job = NutchJob.getInstance(getConf());
        Configuration jobConf = job.getConfiguration();
        fs = FileSystem.get(jobConf);
        job.setJobName("merge bwDb " + bwDb);
        Path current = new Path(bwDb, "current");
        if (FileSystem.get(jobConf).exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }
        FileInputFormat.addInputPath(job, tempDir);
        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setReducerClass(BWInjectReducer.class);
        FileOutputFormat.setOutputPath(job, newDb);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(BWPatterns.class);
        lock = CrawlDb.lock(getConf(), current, false);
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
            throw e;
        } finally {
            NutchJob.cleanupAfterFailure(tempDir, lock, fs);
        }

        LOG.info("rename bwdb");
        Path old = new Path(bwDb, "old");
        fs.delete(old, true);
        if (fs.exists(current)) {
            fs.rename(current, old);
        }
        fs.rename(newDb, current);
        fs.delete(old, true);

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
