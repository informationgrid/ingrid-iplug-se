package org.apache.nutch.tools;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.SequenceFile.Sorter;
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
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;

public class HostStatistic extends Configured {

  public static final String INPUT_OPTION = "INPUT_OPTION";

  public static final String INPUT_OPTION_CRAWLDB_DIR = "-crawldb";

  public static final String INPUT_OPTION_FETCH_DIR = "-fetch";

  public static final String COUNT_OPTION = "COUNT_OPTION";

  public static final String COUNT_OPTION_HOST = "-host";

  private static final Log LOG = LogFactory.getLog(HostStatistic.class
      .getName());

  public static class StatisticWritable implements
      WritableComparable<StatisticWritable> {

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

  public static class StatisticWritableCounter implements
      Mapper<Text, Writable, Text, StatisticWritable>,
      Reducer<Text, StatisticWritable, Text, StatisticWritable> {

    private StatisticWritable _one = new StatisticWritable();

    private StatisticWritable _sum = new StatisticWritable();

    @Override
    public void map(Text key, Writable value,
        OutputCollector<Text, StatisticWritable> collector, Reporter reporter)
        throws IOException {
      CrawlDatum crawlDatum = (CrawlDatum) value;
      Text utf8 = (Text) key;
      String urlString = utf8.toString();
      URL url = new URL(urlString);
      String host = url.getHost();
      _one._isFetched.set(false);

      collector.collect(new Text(host), _one);
      collector.collect(new Text("Overall"), _one);
      if ((crawlDatum.getStatus() == CrawlDatum.STATUS_DB_FETCHED)
          || crawlDatum.getStatus() == CrawlDatum.STATUS_FETCH_SUCCESS) {
        _one._isFetched.set(true);
        collector.collect(new Text(host), _one);
        collector.collect(new Text("Overall"), _one);
      }
    }

    @Override
    public void reduce(Text key, Iterator<StatisticWritable> values,
        OutputCollector<Text, StatisticWritable> collector, Reporter reporter)
        throws IOException {
      long overallCounter = 0;
      long fetchSuccesCounter = 0;

      while (values.hasNext()) {
        StatisticWritable statisticWritable = (StatisticWritable) values.next();
        if (statisticWritable._isFetched.get()) {
          fetchSuccesCounter = fetchSuccesCounter
              + statisticWritable._fetchSuccessCount.get();
        } else {
          overallCounter = overallCounter
              + statisticWritable._overallCount.get();
        }
      }
      _sum._fetchSuccessCount.set(fetchSuccesCounter);
      _sum._overallCount.set(overallCounter);
      System.out.println(key + ": " + _sum);
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

  public void statistic(Path crawldb, Path segment) throws IOException {

    Path out = new Path(segment, "statistic/host");

    FileSystem fileSystem = FileSystem.get(getConf());
    fileSystem.delete(out, true);

    LOG.info("START CRAWLDB STATISTIC");
    String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));

    String name = "crawldb-statistic-temp-" + id;
    Path tempCrawldb = new Path(getConf().get("mapred.temp.dir", "."), name);
    JobConf countJob = createCountJob(INPUT_OPTION_CRAWLDB_DIR, crawldb,
        tempCrawldb);
    JobClient.runJob(countJob);

    LOG.info("START FETCH STATISTIC");
    name = "shard-statistic-temp-" + id;
    Path tempFetch = new Path(getConf().get("mapred.temp.dir", "."), name);
    countJob = createCountJob(INPUT_OPTION_FETCH_DIR, segment, tempFetch);
    JobClient.runJob(countJob);

    name = "crawldb-sequence-temp-" + id;
    Path tempSequenceCrawldb = new Path(getConf().get("mapred.temp.dir", "."),
        name);
    JobConf sequenceCrawldbJob = createSequenceFileJob(tempCrawldb,
        tempSequenceCrawldb);
    JobClient.runJob(sequenceCrawldbJob);

    name = "shard-sequence-temp-" + id;
    Path tempSequenceShard = new Path(getConf().get("mapred.temp.dir", "."),
        name);
    JobConf sequenceShardJob = createSequenceFileJob(tempFetch,
        tempSequenceShard);
    JobClient.runJob(sequenceShardJob);

    // sort the output files into one file
    Sorter sorter = new SequenceFile.Sorter(fileSystem,
        StatisticWritable.class, Text.class, getConf());

    Path[] paths = getPaths(fileSystem, tempSequenceCrawldb);
    LOG.info("sort path's: " + Arrays.asList(paths));
    sorter.sort(paths, new Path(out, "crawldb"), false);

    paths = getPaths(fileSystem, tempSequenceShard);
    LOG.info("sort path's: " + Arrays.asList(paths));
    sorter.sort(paths, new Path(out, "shard"), false);

    fileSystem.delete(tempCrawldb, true);
    fileSystem.delete(tempFetch, true);
    // fileSystem.delete(tempMerge, true);
  }

  private Path[] getPaths(FileSystem fileSystem, Path tempSequenceCrawldb)
      throws IOException {
    FileStatus[] listStatus = fileSystem.listStatus(tempSequenceCrawldb);
    Path[] paths = new Path[listStatus.length];
    int c = 0;
    for (FileStatus status : listStatus) {
      paths[c++] = status.getPath();
    }
    return paths;
  }

  private static JobConf createCountJob(String inputOption, Path in, Path out) {
    Path inputDir = null;
    if (inputOption.equals(INPUT_OPTION_CRAWLDB_DIR)) {
      inputDir = new Path(in, CrawlDb.CURRENT_NAME);
    } else if (inputOption.equals(INPUT_OPTION_FETCH_DIR)) {
      inputDir = new Path(in, CrawlDatum.FETCH_DIR_NAME);
    }
    JobConf job = new JobConf();
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

  private static JobConf createSequenceFileJob(Path in, Path out) {
    JobConf sortJob = new JobConf();
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
    Path crawldb = new Path(args[0]);
    Path segment = new Path(args[1]);
    HostStatistic hostStatistic = new HostStatistic(new Configuration());
    hostStatistic.statistic(crawldb, segment);

  }

}
