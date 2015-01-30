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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

import de.ingrid.iplug.se.nutch.net.InGridURLNormalizers;

/**
 * CrawlDb update tool that only updates urls that passing a white-black list
 * 
 * @see BWInjector
 */
public class BWUpdateDb extends Configured implements Tool {

    public static final Log LOG = LogFactory.getLog(BWUpdateDb.class);

    public static class ObjectWritableMapper implements Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, Writable value, OutputCollector<HostTypeKey, ObjectWritable> collector, Reporter reporter) throws IOException {
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
    public static class BWMapper implements Mapper<Text, CrawlDatum, HostTypeKey, Entry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, CrawlDatum value, OutputCollector<HostTypeKey, Entry> out, Reporter rep) throws IOException {

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

        public void reduce(HostTypeKey key, Iterator<ObjectWritable> values, OutputCollector<HostTypeKey, ObjectWritable> out, Reporter report) throws IOException {

            while (values.hasNext()) {
                ObjectWritable objectWritable = (ObjectWritable) values.next();
                Object value = objectWritable.get(); // unwrap

                if (value instanceof BWPatterns) {
                    _patterns = (BWPatterns) value;
                    // next values should be a list of entries
                    return;
                }

                if (_patterns == null) {
                    // return, because no bw pattern has been set for this url
                    // this results in NOT crawling this url
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No BW pattern for url: " + (((Entry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                    }
                    return;
                }

                String url = (((Entry) value)._url).toString();
                if (_patterns.willPassBWLists(url)) {
                    // url is outside the black list and matches the white list
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("BW patterns passed for url: " + (((Entry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                    }
                    out.collect(key, objectWritable);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("BW patterns NOT passed for url: " + (((Entry) value)._url).toString() + " for HostTypeKey: " + key.toString());
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

        public void map(HostTypeKey key, ObjectWritable value, OutputCollector<Text, CrawlDatum> out, Reporter rep) throws IOException {
            Entry entry = (Entry) value.get();
            out.collect(entry._url, entry._crawlDatum);
        }

        public void configure(JobConf job) {
        }

        public void close() throws IOException {
        }
    }

    public static class BWDbCsvOutputFormat extends FileOutputFormat<HostTypeKey, BWPatterns> {
        protected static class LineRecordWriter implements RecordWriter<HostTypeKey, BWPatterns> {
            private DataOutputStream out;

            public LineRecordWriter(DataOutputStream out) {
                this.out = out;
                try {
                    out.writeBytes("Black/White list database dump\n");
                } catch (IOException e) {
                }
            }

            public synchronized void write(HostTypeKey key, BWPatterns value) throws IOException {
                out.writeByte('"');
                out.writeBytes(key.toString());
                out.writeByte('"');
                out.writeByte(';');
                out.writeBytes("white: ");
                List<Text> positive = value.getPositive();
                for (Text text : positive) {
                    out.writeBytes(text.toString());
                    out.writeByte(';');
                }
                out.writeBytes("black: ");
                List<Text> negative = value.getNegative();
                for (Text text : negative) {
                    out.writeBytes(text.toString());
                    out.writeByte(';');
                }
                out.writeByte('\n');
            }

            public synchronized void close(Reporter reporter) throws IOException {
                out.close();
            }
        }

        public RecordWriter<HostTypeKey, BWPatterns> getRecordWriter(FileSystem fs, JobConf job, String name, Progressable progress) throws IOException {
            Path dir = FileOutputFormat.getOutputPath(job);
            DataOutputStream fileOut = fs.create(new Path(dir, name), progress);
            return new LineRecordWriter(fileOut);
        }
    }

    public BWUpdateDb(Configuration conf) {
        super(conf);
    }

    public BWUpdateDb() {
    }

    // TODO use normalize and filter inside the bw-job? and not only in the
    // crawldb-job.
    public void update(Path crawlDb, Path bwdb, Path[] segments, boolean normalize, boolean filter) throws IOException {
        LOG.info("bw update: starting");
        LOG.info("bw update: db: " + crawlDb);
        LOG.info("bw update: bwdb: " + bwdb);
        LOG.info("bw update: segments: " + Arrays.asList(segments));

        // wrapping
        LOG.info("bw update: wrapping started.");
        String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedSegOutput = new Path(crawlDb, name);

        JobConf job = new NutchJob(getConf());
        job.setJobName("bw update: wrap segment: " + Arrays.asList(segments));

        job.setInputFormat(SequenceFileInputFormat.class);

        for (Path segment : segments) {
            FileInputFormat.addInputPath(job, new Path(segment, CrawlDatum.FETCH_DIR_NAME));
            FileInputFormat.addInputPath(job, new Path(segment, CrawlDatum.PARSE_DIR_NAME));
        }

        job.setMapperClass(BWMapper.class);

        FileOutputFormat.setOutputPath(job, wrappedSegOutput);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(Entry.class);
        job.setBoolean(BWMapper.URL_FILTERING, filter);
        job.setBoolean(BWMapper.URL_NORMALIZING, normalize);
        JobClient.runJob(job);

        // filtering
        LOG.info("bw update: filtering started.");
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
        LOG.info("bw update: converting started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path tmpFormatOut = new Path(crawlDb, name);
        JobConf convertJob = new NutchJob(getConf());
        convertJob.setJobName("format converting: " + tmpMergedDb);
        FileInputFormat.addInputPath(convertJob, tmpMergedDb);
        convertJob.setInputFormat(SequenceFileInputFormat.class);
        convertJob.setMapperClass(FormatConverter.class);
        FileOutputFormat.setOutputPath(convertJob, tmpFormatOut);
        convertJob.setOutputFormat(MapFileOutputFormat.class);
        convertJob.setOutputKeyClass(Text.class);
        convertJob.setOutputValueClass(CrawlDatum.class);
        JobClient.runJob(convertJob);

        //
        FileSystem.get(job).delete(tmpMergedDb, true);

        JobConf updateJob = CrawlDb.createJob(getConf(), crawlDb);
        boolean additionsAllowed = getConf().getBoolean(CrawlDb.CRAWLDB_ADDITIONS_ALLOWED, true);
        updateJob.setBoolean(CrawlDb.CRAWLDB_ADDITIONS_ALLOWED, additionsAllowed);
        // do not filter/normalize URLs from crawl db again. Presume all
        // urls in crawldb are filtered/normalized
        // joachim@wemove.com at 27.05.2010
        // updateJob.setBoolean(CrawlDbFilter.URL_FILTERING, filter);
        // updateJob.setBoolean(CrawlDbFilter.URL_NORMALIZING, normalize);

        FileInputFormat.addInputPath(updateJob, tmpFormatOut);
        LOG.info("bw update: Merging bw filtered segment data into db.");
        JobClient.runJob(updateJob);
        FileSystem.get(job).delete(tmpFormatOut, true);

        LOG.info("install crawldb");
        CrawlDb.install(updateJob, crawlDb);
        LOG.info("bw update: done");

    }

    public void processDumpJob(String bwDb, String output, Configuration config) throws IOException {

        if (LOG.isInfoEnabled()) {
            LOG.info("BWDb dump: starting");
            LOG.info("BWDb db: " + bwDb);
        }

        Path outFolder = new Path(output);

        JobConf job = new NutchJob(config);
        job.setJobName("dump " + bwDb);

        FileInputFormat.addInputPath(job, new Path(bwDb, CrawlDb.CURRENT_NAME));
        job.setInputFormat(SequenceFileInputFormat.class);

        FileOutputFormat.setOutputPath(job, outFolder);
        job.setOutputFormat(BWDbCsvOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(BWPatterns.class);

        JobClient.runJob(job);
        if (LOG.isInfoEnabled()) {
            LOG.info("BWDb dump: done");
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new BWUpdateDb(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 5 && args.length != 3) {
            System.err.println("Usage: BWUpdateDb <crawldb> <bwdb> <segment> <normalize> <filter>");
            System.err.println("or: BWUpdateDb <bwdb> -dump <out_dir>");
            return -1;
        }
        try {

            if (args[1].equals("-dump")) {
                processDumpJob(args[0], args[2], getConf());
            } else {
                update(new Path(args[0]), new Path(args[1]), new Path[] { new Path(args[2]) }, Boolean.valueOf(args[3]), Boolean.valueOf(args[4]));
            }

            return 0;
        } catch (Exception e) {
            LOG.error("BWUpdateDb: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
