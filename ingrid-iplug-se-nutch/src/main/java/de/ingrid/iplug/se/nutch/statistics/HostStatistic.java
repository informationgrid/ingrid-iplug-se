/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.nutch.statistics;

import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BooleanWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.SequenceFile.Sorter;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
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
import org.apache.hadoop.mapred.lib.InverseMapper;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class HostStatistic extends Configured implements Tool {

    private static final Log LOG = LogFactory.getLog(HostStatistic.class.getName());

    public static class StatisticWritable implements WritableComparable<StatisticWritable> {

        private BooleanWritable _isFetched = new BooleanWritable();

        private LongWritable _overallCount = new LongWritable(1);

        private LongWritable _fetchSuccessCount = new LongWritable(1);

        @Override
        public void readFields(DataInput in) throws IOException {
            _isFetched.readFields(in);
            _overallCount.readFields(in);
            _fetchSuccessCount.readFields(in);
        }

        @Override
        public void write(DataOutput out) throws IOException {
            _isFetched.write(out);
            _overallCount.write(out);
            _fetchSuccessCount.write(out);
        }

        @Override
        public String toString() {
            return _fetchSuccessCount + " # " + _overallCount;
        }

        @Override
        public int compareTo(StatisticWritable other) {
            // return _overallCount.compareTo(other._overallCount);
            return other._overallCount.compareTo(_overallCount);
            // return _overallCount.get() < other._overallCount.get() ? 1 : -1;
        }

        public long getFetchSuccessCount() {
            return _fetchSuccessCount.get();
        }

        public long getOverallCount() {
            return _overallCount.get();
        }

    }

    public static class StatisticWritableCounter implements Mapper<Text, Writable, Text, StatisticWritable>, Reducer<Text, StatisticWritable, Text, StatisticWritable> {

        private StatisticWritable _one = new StatisticWritable();

        private StatisticWritable _sum = new StatisticWritable();

        @Override
        public void map(Text key, Writable value, OutputCollector<Text, StatisticWritable> collector, Reporter reporter) throws IOException {
            CrawlDatum crawlDatum = (CrawlDatum) value;
            Text utf8 = (Text) key;
            String urlString = utf8.toString();
            URL url = new URL(urlString);
            String host = url.getHost();
            _one._isFetched.set(false);

            collector.collect(new Text(host), _one);
            collector.collect(new Text("Overall"), _one);
            if ((crawlDatum.getStatus() == CrawlDatum.STATUS_DB_FETCHED) || crawlDatum.getStatus() == CrawlDatum.STATUS_FETCH_SUCCESS || crawlDatum.getStatus() == CrawlDatum.STATUS_DB_NOTMODIFIED) {
                _one._isFetched.set(true);
                collector.collect(new Text(host), _one);
                collector.collect(new Text("Overall"), _one);
            }
        }

        @Override
        public void reduce(Text key, Iterator<StatisticWritable> values, OutputCollector<Text, StatisticWritable> collector, Reporter reporter) throws IOException {
            long overallCounter = 0;
            long fetchSuccesCounter = 0;

            while (values.hasNext()) {
                StatisticWritable statisticWritable = (StatisticWritable) values.next();
                if (statisticWritable._isFetched.get()) {
                    fetchSuccesCounter = fetchSuccesCounter + statisticWritable._fetchSuccessCount.get();
                } else {
                    overallCounter = overallCounter + statisticWritable._overallCount.get();
                }
            }
            _sum._fetchSuccessCount.set(fetchSuccesCounter);
            _sum._overallCount.set(overallCounter);
            collector.collect(key, _sum);
        }

        @Override
        public void configure(JobConf jobConf) {
        }

        @Override
        public void close() throws IOException {
        }

    }

    public HostStatistic(Configuration configuration) {
        super(configuration);
    }

    public HostStatistic() {
    }

    public void statistic(Path crawldb, Path outputDir) throws IOException {

        Path out = new Path(outputDir, "statistic/host");

        FileSystem fs = FileSystem.get(getConf());
        fs.delete(out, true);

        LOG.info("START CRAWLDB STATISTIC");
        String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));

        String name = "crawldb-statistic-temp-" + id;
        Path tempCrawldb = new Path(getConf().get("hadoop.temp.dir", "."), name);
        JobConf countJob = createCountJob(crawldb, tempCrawldb);
        JobClient.runJob(countJob);

        name = "crawldb-sequence-temp-" + id;
        Path tempSequenceCrawldb = new Path(getConf().get("hadoop.temp.dir", "."), name);
        JobConf sequenceCrawldbJob = createSequenceFileJob(tempCrawldb, tempSequenceCrawldb);
        JobClient.runJob(sequenceCrawldbJob);

        fs.delete(tempCrawldb, true);

        // sort the output files into one file
        name = "crawldb-sorted-temp-" + id;
        Path tempSortedCrawldb = new Path(getConf().get("hadoop.temp.dir", "."), name);
        Sorter sorter = new SequenceFile.Sorter(fs, StatisticWritable.class, Text.class, getConf());
        Path[] paths = getPaths(fs, tempSequenceCrawldb);
        sorter.sort(paths, tempSortedCrawldb, false);

        fs.delete(tempSequenceCrawldb, true);

        Reader reader = null;
        BufferedWriter br = null;
        try {

            reader = new SequenceFile.Reader(fs, tempSortedCrawldb, getConf());
            StatisticWritable key = new StatisticWritable();
            Text value = new Text();

            Path file = new Path(out, "crawldb");
            if (fs.exists(file)) {
                fs.delete(file, true);
            }
            OutputStream os = fs.create(file);
            br = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            JSONArray array = new JSONArray();
            while (reader.next(key, value)) {

                JSONObject jsn = new JSONObject();
                jsn.put("host", new String( value.toString() ));
                jsn.put("fetched", key.getFetchSuccessCount());
                jsn.put("known", key.getOverallCount());
                float ratio = 0; 
                if (key.getOverallCount() > 0) {
                    ratio = (float) key.getFetchSuccessCount() / key.getOverallCount();
                }
                jsn.put("ratio", String.format("%.5f", ratio));
                array.put( jsn );
            }
            br.write(array.toString());
        } catch (JSONException e) {
            LOG.error("Error creating JSON from statistics", e);
            e.printStackTrace();
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (br != null) {
                br.close();
            }
        }

        fs.delete(tempSortedCrawldb, true);
    }

    private Path[] getPaths(FileSystem fileSystem, Path tempSequenceCrawldb) throws IOException {
        FileStatus[] listStatus = fileSystem.listStatus(tempSequenceCrawldb);
        Path[] paths = new Path[listStatus.length];
        int c = 0;
        for (FileStatus status : listStatus) {
            if (!status.getPath().getName().contains("_SUCCESS")) {
                paths[c++] = status.getPath();
            } else {
                paths = (Path[]) ArrayUtils.remove(paths, c);
            }
        }
        return paths;
    }

    private JobConf createCountJob(Path in, Path out) {
        Path inputDir = new Path(in, CrawlDb.CURRENT_NAME);

        JobConf job = new NutchJob(getConf());
        job.setJobName("host_count " + inputDir);

        FileInputFormat.addInputPath(job, inputDir);
        job.setInputFormat(SequenceFileInputFormat.class);

        job.setReducerClass(StatisticWritableCounter.class);
        job.setMapperClass(StatisticWritableCounter.class);

        FileOutputFormat.setOutputPath(job, out);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(StatisticWritable.class);
        return job;
    }

    private JobConf createSequenceFileJob(Path in, Path out) {
        JobConf sortJob = new NutchJob(getConf());
        sortJob.setJobName("sort_host_count " + in);
        FileInputFormat.addInputPath(sortJob, in);

        sortJob.setInputFormat(SequenceFileInputFormat.class);

        sortJob.setMapperClass(InverseMapper.class);

        FileOutputFormat.setOutputPath(sortJob, out);
        sortJob.setOutputFormat(SequenceFileOutputFormat.class);
        sortJob.setOutputKeyClass(StatisticWritable.class);
        sortJob.setOutputValueClass(Text.class);
        return sortJob;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new HostStatistic(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: HostStatistic <bwdb> <output_dir>");
            System.err.println("       The statistic will be writte to <output_dir>/statistic/host.");

            return -1;
        }
        try {
            statistic(new Path(args[0]), new Path(args[1]));
            return 0;
        } catch (Exception e) {
            LOG.error("HostStatistic: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
