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

package de.ingrid.nutch.crawl.bw;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
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
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

import de.ingrid.iplug.se.net.InGridURLNormalizers;

/**
 * CrawlDb filter tool that filters urls that pass a white-black list
 * 
 * @see BWInjector
 */
public class BWCrawlDbFilter extends Configured {

    public static final Log LOG = LogFactory.getLog(BWCrawlDbFilter.class);

    public static final String CURRENT_NAME = "current";

    public static class ObjectWritableMapper implements Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, Writable value, OutputCollector<HostTypeKey, ObjectWritable> collector,
                Reporter reporter) throws IOException {
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
    public static class CrawlDbMapper implements Mapper<Text, CrawlDatum, HostTypeKey, Entry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, CrawlDatum value, OutputCollector<HostTypeKey, Entry> out, Reporter rep)
                throws IOException {

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
                out.collect(new HostTypeKey(host, HostTypeKey.CRAWL_DATUM_TYPE), entry);
            }
        }

        public void configure(JobConf job) {
            urlFiltering = job.getBoolean(URL_FILTERING, false);
            urlNormalizers = job.getBoolean(URL_NORMALIZING, false);
            if (urlFiltering) {
                filters = new URLFilters(job);
            }
            if (urlNormalizers) {
                scope = job.get(URL_NORMALIZING_SCOPE, InGridURLNormalizers.SCOPE_BWDB);
                normalizers = new URLNormalizers(job, scope);
            }
        }

        public void close() throws IOException {
        }

    }

    /**
     * Collects only entries that match a white list entry and no black list
     * entry
     */
    public static class BwReducer implements Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable> {

        private BWPatterns _patterns;

        public void reduce(HostTypeKey key, Iterator<ObjectWritable> values,
                OutputCollector<HostTypeKey, ObjectWritable> out, Reporter report) throws IOException {

            while (values.hasNext()) {
                ObjectWritable objectWritable = (ObjectWritable) values.next();
                Object value = objectWritable.get(); // unwrap

                if (value instanceof BWPatterns) {
                    _patterns = (BWPatterns) value;
                    // next values should be a list of entries
                    return;
                }

                if (_patterns == null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Key url or linked url does not have a valid BW patterns, remove it: " + value
                                + " for HostTypeKey: " + key.toString());
                    }
                    // return, because no bw pattern has been set for this url
                    // this results in NOT crawling this url
                    return;
                }

                String url = (((Entry) value)._url).toString();
                if (_patterns.willPassBWLists(url)) {
                    // url is outside the black list and matches the white list
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("BW patterns passed for url: " + (((Entry) value)._url).toString()
                                + " for HostTypeKey: " + key.toString());
                    }
                    out.collect(key, objectWritable);
                } else {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Crawldatum does not pass BW patterns, remove it: " + (((Entry) value)._url).toString()
                                + " for HostTypeKey: " + key.toString());
                    }

                }
            }
        }

        public void configure(JobConf arg0) {
        }

        public void close() throws IOException {
        }

    }

    /**
     * mapper that converts between the different formats, since hadoop by today
     * does not support, different key:values between mapper and reducer in the
     * same job
     */
    public static class FormatConverter implements Mapper<HostTypeKey, ObjectWritable, Text, CrawlDatum> {

        public void map(HostTypeKey key, ObjectWritable value, OutputCollector<Text, CrawlDatum> out, Reporter rep)
                throws IOException {
            Entry entry = (Entry) value.get();
            out.collect(entry._url, entry._crawlDatum);
        }

        public void configure(JobConf job) {
        }

        public void close() throws IOException {
        }
    }

    public BWCrawlDbFilter(Configuration conf) {
        super(conf);
    }

    // TODO use normalize and filter inside the bw-job? and not only in the
    // crawldb-job.
    public void update(Path crawlDb, Path bwdb, boolean normalize, boolean filter,
            boolean replaceCrawlDb) throws IOException {
        
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

        JobConf job = new NutchJob(getConf());
        job.setJobName("filter crawldb against bwdb: wrap crawldb: " + crawlDb);

        job.setInputFormat(SequenceFileInputFormat.class);

        Path current = new Path(crawlDb, CURRENT_NAME);
        if (FileSystem.get(job).exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }

        job.setMapperClass(CrawlDbMapper.class);

        FileOutputFormat.setOutputPath(job, wrappedSegOutput);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(Entry.class);
        job.setBoolean(CrawlDbMapper.URL_FILTERING, filter);
        job.setBoolean(CrawlDbMapper.URL_NORMALIZING, normalize);
        JobClient.runJob(job);

        // filtering
        LOG.info("filter crawldb against bwdb: filtering started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path tmpMergedDb = new Path(crawlDb, name);
        JobConf filterJob = new NutchJob(getConf());
        filterJob.setJobName("filtering: " + wrappedSegOutput + bwdb);
        filterJob.setInputFormat(SequenceFileInputFormat.class);

        FileInputFormat.addInputPath(filterJob, wrappedSegOutput);
        FileInputFormat.addInputPath(filterJob, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(filterJob, tmpMergedDb);
        filterJob.setMapperClass(ObjectWritableMapper.class);
        filterJob.setReducerClass(BwReducer.class);
        filterJob.setOutputFormat(MapFileOutputFormat.class);
        filterJob.setOutputKeyClass(HostTypeKey.class);
        filterJob.setOutputValueClass(ObjectWritable.class);
        JobClient.runJob(filterJob);

        // remove wrappedSegOutput
        FileSystem.get(job).delete(wrappedSegOutput, true);

        // convert formats
        LOG.info("filter crawldb against bwdb: converting started.");
        JobConf convertJob = new NutchJob(getConf());
        convertJob.setJobName("format converting: " + tmpMergedDb);
        FileInputFormat.addInputPath(convertJob, tmpMergedDb);
        convertJob.setInputFormat(SequenceFileInputFormat.class);
        convertJob.setMapperClass(FormatConverter.class);
        FileOutputFormat.setOutputPath(convertJob, outputCrawlDb);
        convertJob.setOutputFormat(MapFileOutputFormat.class);
        convertJob.setOutputKeyClass(Text.class);
        convertJob.setOutputValueClass(CrawlDatum.class);
        JobClient.runJob(convertJob);

        // 
        FileSystem.get(job).delete(tmpMergedDb, true);

        if (replaceCrawlDb) {
            LOG.info("filter crawldb against bwdb: replace current crawldb");
            CrawlDb.install(convertJob, crawlDb);
        }
        LOG.info("filter crawldb against bwdb: finished.");

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = NutchConfiguration.create();
        BWCrawlDbFilter bwDb = new BWCrawlDbFilter(conf);
        if (args.length != 5) {
            System.err
                    .println("Usage: BWCrawlDbFilter <crawldb> <bwdb> <normalize> <filter> <replace current crawldb>");
            return;
        }
        bwDb.update(new Path(args[1]), new Path(args[2]), Boolean.valueOf(args[3]), Boolean
                .valueOf(args[4]), Boolean.valueOf(args[5]));

    }

}
