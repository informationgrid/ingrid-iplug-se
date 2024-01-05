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
package de.ingrid.iplug.se.nutch.statistics;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.SequenceFile.Sorter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.map.InverseMapper;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class HostStatistic extends Configured implements Tool {

    private static final Logger LOG = LoggerFactory.getLogger(HostStatistic.class.getName());

    public static class StatisticWritable implements WritableComparable<StatisticWritable> {

        private final BooleanWritable _isFetched = new BooleanWritable();

        private final LongWritable _overallCount = new LongWritable(1);

        private final LongWritable _fetchSuccessCount = new LongWritable(1);

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

    public static class StatisticWritableCounterMapper extends Mapper<Text, Writable, Text, StatisticWritable> {

        private final StatisticWritable _one = new StatisticWritable();

        @Override
        public void map(Text key, Writable value, Mapper<Text, Writable, Text, StatisticWritable>.Context context) throws IOException, InterruptedException {
            CrawlDatum crawlDatum = (CrawlDatum) value;
            String urlString = key.toString();
            URL url = new URL(urlString);
            String host = url.getHost();
            _one._isFetched.set(false);

            context.write(new Text(host), _one);
            context.write(new Text("Overall"), _one);
            if ((crawlDatum.getStatus() == CrawlDatum.STATUS_DB_FETCHED) || crawlDatum.getStatus() == CrawlDatum.STATUS_FETCH_SUCCESS || crawlDatum.getStatus() == CrawlDatum.STATUS_DB_NOTMODIFIED) {
                _one._isFetched.set(true);
                context.write(new Text(host), _one);
                context.write(new Text("Overall"), _one);
            }
        }
    }

    public static class StatisticWritableCounterReducer extends Reducer<Text, StatisticWritable, Text, StatisticWritable> {

        private final StatisticWritable _sum = new StatisticWritable();

        @Override
        public void reduce(Text key, Iterable<StatisticWritable> values, Reducer<Text, StatisticWritable, Text, StatisticWritable>.Context context) throws IOException, InterruptedException {
            AtomicLong overallCounter = new AtomicLong();
            AtomicLong fetchSuccesCounter = new AtomicLong();

            values.forEach(statisticWritable -> {
                if (statisticWritable._isFetched.get()) {
                    fetchSuccesCounter.set(fetchSuccesCounter.get() + statisticWritable._fetchSuccessCount.get());
                } else {
                    overallCounter.set(overallCounter.get() + statisticWritable._overallCount.get());
                }
            });

            _sum._fetchSuccessCount.set(fetchSuccesCounter.get());
            _sum._overallCount.set(overallCounter.get());
            context.write(key, _sum);
        }
    }

    public HostStatistic() {
    }

    public void statistic(Path crawldb, Path outputDir) throws IOException, InterruptedException, ClassNotFoundException {

        Path out = new Path(outputDir, "statistic/host");

        Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);
        fs.delete(out, true);

        LOG.info("START CRAWLDB STATISTIC");
        String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));

        String name = "crawldb-statistic-temp-" + id;
        Path tempCrawldb = new Path(getConf().get("hadoop.temp.dir", "."), name);

        Job countJob = createCountJob(crawldb, tempCrawldb, conf);
        Path lock = CrawlDb.lock(conf, crawldb, false);
        try {
            boolean success = countJob.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + countJob.getStatus().getState() + ", reason: "
                        + countJob.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            fs.delete(tempCrawldb, true);
            throw e;
        } finally {
            LockUtil.removeLockFile(fs, lock);
        }

        name = "crawldb-sequence-temp-" + id;
        Path tempSequenceCrawldb = new Path(getConf().get("hadoop.temp.dir", "."), name);
        Job sequenceCrawldbJob = createSequenceFileJob(tempCrawldb, tempSequenceCrawldb, conf);
        try {
            boolean success = sequenceCrawldbJob.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + sequenceCrawldbJob.getStatus().getState() + ", reason: "
                        + sequenceCrawldbJob.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            fs.delete(tempSequenceCrawldb, true);
            throw e;
        } finally {
            fs.delete(tempCrawldb, true);
        }

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
            reader = new SequenceFile.Reader(conf, Reader.file(tempSortedCrawldb));
            StatisticWritable key = new StatisticWritable();
            Text value = new Text();

            Path file = new Path(out, "crawldb");
            if (fs.exists(file)) {
                fs.delete(file, true);
            }
            OutputStream os = fs.create(file);
            br = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            JSONArray array = new JSONArray();
            while (reader.next(key, value)) {

                JSONObject jsn = new JSONObject();
                jsn.put("host", value.toString());
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

    private Job createCountJob(Path in, Path out, Configuration conf) throws IOException {
        Path inputDir = new Path(in, CrawlDb.CURRENT_NAME);

        Job job = NutchJob.getInstance(conf);
        job.setJobName("host_count " + inputDir);

        FileInputFormat.addInputPath(job, inputDir);
        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setReducerClass(StatisticWritableCounterReducer.class);
        job.setMapperClass(StatisticWritableCounterMapper.class);

        FileOutputFormat.setOutputPath(job, out);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(StatisticWritable.class);
        return job;
    }

    private Job createSequenceFileJob(Path in, Path out, Configuration conf) throws IOException {
        Job sortJob = NutchJob.getInstance(conf);
        sortJob.setJobName("sort_host_count " + in);
        FileInputFormat.addInputPath(sortJob, in);

        sortJob.setInputFormatClass(SequenceFileInputFormat.class);

        sortJob.setMapperClass(InverseMapper.class);

        FileOutputFormat.setOutputPath(sortJob, out);
        sortJob.setOutputFormatClass(SequenceFileOutputFormat.class);
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
