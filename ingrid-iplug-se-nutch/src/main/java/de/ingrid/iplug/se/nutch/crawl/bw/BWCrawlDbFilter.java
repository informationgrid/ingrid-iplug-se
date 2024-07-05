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

import de.ingrid.iplug.se.nutch.net.InGridURLNormalizers;
import de.ingrid.iplug.se.nutch.tools.IngridElasticSearchClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

/**
 * CrawlDb filter tool that filters urls that pass a white-black list
 *
 * @see BWInjector
 */
public class BWCrawlDbFilter extends Configured implements Tool {

    public static final Logger LOG = LoggerFactory.getLogger(BWCrawlDbFilter.class);

    public static final String CURRENT_NAME = "current";

    public IngridElasticSearchClient esClient = null;

    public static class ObjectWritableMapper extends Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, Writable value, Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable>.Context context) throws IOException, InterruptedException {
            ObjectWritable objectWritable = new ObjectWritable(value);
            context.write(key, objectWritable);
        }
    }

    /**
     * url-crawlDatum tuple
     */
    public static class Entry implements Writable {

        private Text _url;

        private CrawlDatum _crawlDatum;

        public Entry() {
        }

        public Entry(Text url, CrawlDatum crawlDatum) {
            _url = url;
            _crawlDatum = crawlDatum;
        }

        public void write(DataOutput out) throws IOException {
            _url.write(out);
            _crawlDatum.write(out);

        }

        public void readFields(DataInput in) throws IOException {
            _url = new Text();
            _url.readFields(in);
            _crawlDatum = new CrawlDatum();
            _crawlDatum.readFields(in);
        }

        public String toString() {
            return _url.toString() + "\n " + _crawlDatum.toString();
        }

    }

    /**
     * Wraps url and crawlDatum into Entry wrapper and assiociate it with a
     * {@link HostTypeKey}
     */
    public static class CrawlDbMapper extends Mapper<Text, CrawlDatum, HostTypeKey, Entry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        protected void setup(Mapper<Text, CrawlDatum, HostTypeKey, Entry>.Context context) throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = context.getConfiguration();
            urlFiltering = conf.getBoolean(URL_FILTERING, false);
            urlNormalizers = conf.getBoolean(URL_NORMALIZING, false);
            if (urlFiltering) {
                filters = new URLFilters(conf);
            }
            if (urlNormalizers) {
                scope = conf.get(URL_NORMALIZING_SCOPE, InGridURLNormalizers.SCOPE_BWDB);
                normalizers = new URLNormalizers(conf, scope);
            }
        }

        @Override
        public void map(Text key, CrawlDatum value, Mapper<Text, CrawlDatum, HostTypeKey, Entry>.Context context) throws IOException, InterruptedException {

            String url = key.toString();
            if (urlNormalizers) {
                try {
                    url = normalizers.normalize(url, scope); // normalize the
                    // url
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e);
                    url = null;
                }
            }
            if (url != null && urlFiltering) {
                try {
                    url = filters.filter(url); // filter the url
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e);
                    url = null;
                }
            }
            if (url != null) { // if it passes
                String host = new URL(url).getHost();
                Entry entry = new Entry(key, value);
                context.write(new HostTypeKey(host, HostTypeKey.CRAWL_DATUM_TYPE), entry);
            }
        }

    }

    /**
     * Collects only entries that match a white list entry and no black list
     * entry
     */
    public static class BwReducer extends Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable> {

        private BWPatterns _patterns;

        private IngridElasticSearchClient esClient = null;

        @Override
        public void reduce(HostTypeKey key, Iterable<ObjectWritable> values, Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable>.Context context) throws IOException, InterruptedException {

            for (ObjectWritable objectWritable : values) {
                Object value = objectWritable.get(); // unwrap

                if (value instanceof BWPatterns) {
                    // TODO: flush bulk elastic operation
                    _patterns = (BWPatterns) value;
                    // next values should be a list of entries
                    continue;
                }

                if (_patterns == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Key url or linked url does not have a valid BW patterns, remove it: " + value + " for HostTypeKey: " + key.toString());
                    }
                    // return, because no bw pattern has been set for this url
                    // this results in NOT crawling this url

                    // TODO add to be deleted by elastic search, check for
                    // indexed state, flush immediately
                    if (esClient != null) {
                        CrawlDatum datum = ((Entry) value)._crawlDatum;
                        if (datum.getStatus() != CrawlDatum.STATUS_DB_UNFETCHED) {
                            String url = (((Entry) value)._url).toString();
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Create delete request for elastic search: " + url);
                            }
                            esClient.addRequest(esClient.prepareDeleteRequest(url));
                        }
                    }

                    continue;
                }

                String url = (((Entry) value)._url).toString();
                if (_patterns.willPassBWLists(url)) {
                    // url is outside the black list and matches the white list
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("BW patterns passed for url: " + (((Entry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                    }
                    context.write(key, objectWritable);
                } else {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Crawldatum does not pass BW patterns, remove it: " + (((Entry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                    }

                    // deleted by elastic search
                    if (esClient != null) {
                        CrawlDatum datum = ((Entry) value)._crawlDatum;
                        if (datum.getStatus() != CrawlDatum.STATUS_DB_UNFETCHED) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("Create delete request for elastic search: " + url);
                            }
                            try {
                                esClient.addRequest(esClient.prepareDeleteRequest(url));
                            } catch (Exception e) {
                                LOG.error("Error adding request to elasticsearch client.", e);
                            }
                        }
                    }
                }


            }
        }

        public void configure(JobConf job) {
            if (esClient == null) {
                try {
                    esClient = new IngridElasticSearchClient(job);
                } catch (IOException e) {
                    LOG.error("Unable to create elasticsearch client.");
                }
            }
        }

        public void close() throws IOException {
            if (esClient != null) {
                try {
                    esClient.close();
                } catch (Exception e) {
                    LOG.error("Error closing elasticsearch client.", e);
                }
            }
        }

    }

    /**
     * mapper that converts between the different formats, since hadoop by today
     * does not support, different key:values between mapper and reducer in the
     * same job
     */
    public static class FormatConverter extends Mapper<HostTypeKey, ObjectWritable, Text, CrawlDatum> {

        @Override
        public void map(HostTypeKey key, ObjectWritable value, Mapper<HostTypeKey, ObjectWritable, Text, CrawlDatum>.Context context) throws IOException, InterruptedException {
            Entry entry = (Entry) value.get();
            context.write(entry._url,  entry._crawlDatum);
        }

    }

    public BWCrawlDbFilter() {
    }

    // TODO use normalize and filter inside the bw-job? and not only in the
    // crawldb-job.
    public void update(Path crawlDb, Path bwdb, boolean normalize, boolean filter, boolean replaceCrawlDb) throws IOException, InterruptedException, ClassNotFoundException {

        String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path outputCrawlDb = new Path(crawlDb, name);

        LOG.info("filter crawldb against bwdb: starting");
        LOG.info("filter crawldb against bwdb: output crawldb " + outputCrawlDb);
        LOG.info("filter crawldb against bwdb: input crawldb " + crawlDb);
        LOG.info("filter crawldb against bwdb: bwdb: " + bwdb);
        LOG.info("filter crawldb against bwdb: normalize: " + normalize);
        LOG.info("filter crawldb against bwdb: filter: " + filter);
        LOG.info("filter crawldb against bwdb: replaceCrawlDb: " + replaceCrawlDb);

        Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);

        // return if crawldb does not exist
        if (!fs.exists(crawlDb)) {
            LOG.info("filter crawldb against bwdb: crawldb does not exist, nothing todo.");
            return;
        }

        // wrapping
        LOG.info("filter crawldb against bwdb: wrapping started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedSegOutput = new Path(crawlDb, name);

        Job job = NutchJob.getInstance(getConf());
        Configuration jobConf = job.getConfiguration();

        job.setJobName("filter crawldb against bwdb: wrap crawldb: " + crawlDb);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        Path current = new Path(crawlDb, CURRENT_NAME);
        if (FileSystem.get(jobConf).exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }

        job.setMapperClass(CrawlDbMapper.class);

        FileOutputFormat.setOutputPath(job, wrappedSegOutput);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(Entry.class);
        jobConf.setBoolean(CrawlDbMapper.URL_FILTERING, filter);
        jobConf.setBoolean(CrawlDbMapper.URL_NORMALIZING, normalize);

        Path lock = CrawlDb.lock(getConf(), current, false);
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
            LockUtil.removeLockFile(fs, lock);
        }

        // filtering
        LOG.info("filter crawldb against bwdb: filtering started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path tmpMergedDb = new Path(crawlDb, name);

        Job filterJob = NutchJob.getInstance(getConf());

        filterJob.setJobName("filtering: " + wrappedSegOutput + bwdb);
        filterJob.setInputFormatClass(SequenceFileInputFormat.class);

        FileInputFormat.addInputPath(filterJob, wrappedSegOutput);
        FileInputFormat.addInputPath(filterJob, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(filterJob, tmpMergedDb);
        filterJob.setMapperClass(ObjectWritableMapper.class);
        filterJob.setReducerClass(BwReducer.class);
        filterJob.setOutputFormatClass(MapFileOutputFormat.class);
        filterJob.setOutputKeyClass(HostTypeKey.class);
        filterJob.setOutputValueClass(ObjectWritable.class);
        lock = CrawlDb.lock(getConf(), bwdb, false);
        try {
            boolean success = filterJob.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + filterJob.getStatus().getState() + ", reason: "
                        + filterJob.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            throw e;
        } finally {
            NutchJob.cleanupAfterFailure(wrappedSegOutput, lock, fs);
        }


        // convert formats
        LOG.info("filter crawldb against bwdb: converting started.");
        Job convertJob = NutchJob.getInstance(getConf());
        convertJob.setJobName("format converting: " + tmpMergedDb);
        FileInputFormat.addInputPath(convertJob, tmpMergedDb);
        convertJob.setInputFormatClass(SequenceFileInputFormat.class);
        convertJob.setMapperClass(FormatConverter.class);
        FileOutputFormat.setOutputPath(convertJob, outputCrawlDb);
        convertJob.setOutputFormatClass(MapFileOutputFormat.class);
        convertJob.setOutputKeyClass(Text.class);
        convertJob.setOutputValueClass(CrawlDatum.class);

        lock = CrawlDb.lock(getConf(), tmpMergedDb, false);
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
            throw e;
        } finally {
            // remove wrappedSegOutput
            NutchJob.cleanupAfterFailure(tmpMergedDb, lock, fs);
        }


        if (replaceCrawlDb) {
            LOG.info("filter crawldb against bwdb: replace current crawldb");
            CrawlDb.install(convertJob, crawlDb);
        }
        LOG.info("filter crawldb against bwdb: finished.");

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new BWCrawlDbFilter(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: BWCrawlDbFilter <crawldb> <bwdb> <normalize> <filter> <replace current crawldb>");
            return -1;
        }
        try {

            update(new Path(args[0]), new Path(args[1]), Boolean.parseBoolean(args[2]), Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]));

            return 0;
        } catch (Exception e) {
            LOG.error("BWCrawlDbFilter: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
