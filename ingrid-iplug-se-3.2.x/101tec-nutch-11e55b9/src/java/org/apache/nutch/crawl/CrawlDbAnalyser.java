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

package org.apache.nutch.crawl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Closeable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

/**
 * Analyse utility for the CrawlDB.
 * 
 * @author Joachim Müller
 * 
 */
public class CrawlDbAnalyser implements Closeable {

    public static final Log LOG = LogFactory.getLog(CrawlDbAnalyser.class);

    private static class FetchIntervalStatsMR implements Mapper<Text, CrawlDatum, LongWritable, LongWritable>,
            Reducer<LongWritable, LongWritable, LongWritable, LongWritable> {

        @Override
        public void map(Text arg0, CrawlDatum datum, OutputCollector<LongWritable, LongWritable> output, Reporter rep)
                throws IOException {
            output.collect(new LongWritable(datum.getFetchInterval()), new LongWritable(1));
        }

        @Override
        public void reduce(LongWritable key, Iterator<LongWritable> values,
                OutputCollector<LongWritable, LongWritable> output, Reporter rep) throws IOException {

            long cnt = 0;

            while (values.hasNext()) {
                values.next();
                cnt++;
            }

            output.collect(key, new LongWritable(cnt));
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void configure(JobConf arg0) {
        }

    }

    private static class SignaturStatsMR implements Mapper<Text, CrawlDatum, Text, Text>,
            Reducer<Text, Text, Text, Text> {

        @Override
        public void map(Text key, CrawlDatum datum, OutputCollector<Text, Text> output, Reporter rep)
                throws IOException {
            if (datum.getSignature() != null) {
                output.collect(new Text(datum.getSignature()), key);
            }
        }

        @Override
        public void reduce(Text key, Iterator<Text> values,
                OutputCollector<Text, Text> output, Reporter rep) throws IOException {

            StringBuilder urls = new StringBuilder();
            int cnt = 0;

            while (values.hasNext()) {
                cnt++;
                Text value = values.next();
                urls.append(value + "::");
            }
            
            if (cnt > 1) {
                output.collect(new Text(String.valueOf(cnt)), new Text(urls.toString()));
            }
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void configure(JobConf arg0) {
        }

    }

    private static class DumpUrlsForFetchIntervalMR implements Mapper<Text, CrawlDatum, Text, CrawlDatum> {

        long interval = 0;

        @Override
        public void map(Text key, CrawlDatum datum, OutputCollector<Text, CrawlDatum> output, Reporter rep)
                throws IOException {
            if (datum.getFetchInterval() == interval) {
                output.collect(key, datum);
            }
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public void configure(JobConf job) {
            interval = job.getLong("db.analyser.fetchInterval", 0);
        }

    }

    public void processFetchIntervalStats(String crawlDb, Configuration config) throws IOException {

        if (LOG.isInfoEnabled()) {
            LOG.info("CrawlDb FetchIntervalStats starting");
            LOG.info("CrawlDb db: " + crawlDb);
        }

        Path tempDir = new Path(config.get("mapred.temp.dir", ".") + "/fetchIntervalStats-temp-"
                + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));

        JobConf job = new NutchJob(config);
        job.setJobName("fetchIntervalStats " + crawlDb);
        FileInputFormat.addInputPath(job, new Path(crawlDb, CrawlDb.CURRENT_NAME));
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(FetchIntervalStatsMR.class);
        job.setReducerClass(FetchIntervalStatsMR.class);

        FileOutputFormat.setOutputPath(job, tempDir);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(LongWritable.class);
        job.setOutputValueClass(LongWritable.class);

        JobClient.runJob(job);

        // reading the result
        FileSystem fileSystem = FileSystem.get(config);
        SequenceFile.Reader[] readers = SequenceFileOutputFormat.getReaders(config, tempDir);

        LongWritable key = new LongWritable();
        LongWritable value = new LongWritable();

        for (int i = 0; i < readers.length; i++) {
            SequenceFile.Reader reader = readers[i];
            while (reader.next(key, value)) {
                System.out.println(String.format("%8d", key.get()) + " : " + String.format("%8d", value.get()));
            }
        }

        fileSystem.delete(tempDir, true);
    }

    public void processSignaturStats(String crawlDb, Configuration config) throws IOException {

        if (LOG.isInfoEnabled()) {
            LOG.info("CrawlDb Signature Stats starting");
            LOG.info("CrawlDb db: " + crawlDb);
        }

        Path tempDir = new Path(config.get("mapred.temp.dir", ".") + "/signatureStats-temp-"
                + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));

        JobConf job = new NutchJob(config);
        job.setJobName("signatureStats " + crawlDb);
        FileInputFormat.addInputPath(job, new Path(crawlDb, CrawlDb.CURRENT_NAME));
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(SignaturStatsMR.class);
        job.setReducerClass(SignaturStatsMR.class);

        FileOutputFormat.setOutputPath(job, tempDir);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        JobClient.runJob(job);

        // reading the result
        FileSystem fileSystem = FileSystem.get(config);
        SequenceFile.Reader[] readers = SequenceFileOutputFormat.getReaders(config, tempDir);

        Text key = new Text();
        Text value = new Text();
        
        long duplicates = 0;

        for (int i = 0; i < readers.length; i++) {
            SequenceFile.Reader reader = readers[i];
            while (reader.next(key, value)) {
                duplicates = duplicates + Integer.parseInt(key.toString()) - 1;
                System.out.println(key.toString() + " :: [" + value.toString() + "]");
            }
        }
        System.out.println("Detecting " + duplicates + " duplicates.");

        fileSystem.delete(tempDir, true);
    }

    public void processDumpUrlsForFetchInterval(String crawlDb, String interval, Configuration config)
            throws IOException {

        if (LOG.isInfoEnabled()) {
            LOG.info("CrawlDb DumpUrlsForFetchInterval starting");
            LOG.info("CrawlDb db: " + crawlDb);
        }

        Path tempDir = new Path(config.get("mapred.temp.dir", ".") + "/dumpUrlsForFetchIntervaltemp-"
                + Integer.toString(new Random().nextInt(Integer.MAX_VALUE)));

        JobConf job = new NutchJob(config);
        job.setJobName("dumpUrlsForFetchInterval " + crawlDb);
        FileInputFormat.addInputPath(job, new Path(crawlDb, CrawlDb.CURRENT_NAME));
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setMapperClass(DumpUrlsForFetchIntervalMR.class);

        FileOutputFormat.setOutputPath(job, tempDir);
        job.setOutputFormat(SequenceFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(CrawlDatum.class);

        job.setLong("db.analyser.fetchInterval", Long.parseLong(interval));

        JobClient.runJob(job);

        // reading the result
        FileSystem fileSystem = FileSystem.get(config);
        SequenceFile.Reader[] readers = SequenceFileOutputFormat.getReaders(config, tempDir);

        Text key = new Text();
        CrawlDatum value = new CrawlDatum();

        for (int i = 0; i < readers.length; i++) {
            SequenceFile.Reader reader = readers[i];
            while (reader.next(key, value)) {
                System.out.println(key.toString() + " (" + value.toString() + ")");
            }
        }

        fileSystem.delete(tempDir, true);
    }

    public static void main(String[] args) throws IOException {
        CrawlDbAnalyser dba = new CrawlDbAnalyser();

        if (args.length < 1) {
            System.err
                    .println("Usage: CrawlDbAnalyser <crawldb> (-fetchIntervalStats | -dumpUrlsForFetchInterval <interval_value>) | -getSignatureStats");
            System.err.println("\t<crawldb>\tdirectory name where crawldb is located");
            System.err
                    .println("\t-fetchIntervalStats \tprint statistic of how many urls are fetched with a fetch interval to System.out");
            System.err
                    .println("\t-dumpUrlsForFetchInterval \tdump all urls for a specifiv fetch interval value to System.out");
            System.err
            .println("\t-getSignatureStats \tdump signatures with all duplicates to System.out");
            return;
        }
        String param = null;
        String crawlDb = args[0];
        Configuration conf = NutchConfiguration.create();
        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-dumpUrlsForFetchInterval")) {
                param = args[++i];
                dba.processDumpUrlsForFetchInterval(crawlDb, param, conf);
            } else if (args[i].equals("-fetchIntervalStats")) {
                dba.processFetchIntervalStats(crawlDb, conf);
            } else if (args[i].equals("-getSignatureStats")) {
                dba.processSignaturStats(crawlDb, conf);
            } else {
                System.err.println("\nError: wrong argument " + args[i]);
            }
        }
        return;
    }

    @Override
    public void close() throws IOException {
    }
}
