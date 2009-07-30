package org.apache.nutch.tools;

import java.io.DataInput;
import java.io.DataOutput;
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
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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

  private static class MergeReader {
    private org.apache.hadoop.io.SequenceFile.Reader[] _readers;
    private StatisticWritableContainer[] _keys;
    private Text[] _values;

    public MergeReader(org.apache.hadoop.io.SequenceFile.Reader[] readers,
        StatisticWritableContainer[] keys, Text[] values) throws IOException {
      _readers = readers;
      _keys = keys;
      _values = values;
      for (int i = 0; i < readers.length; i++) {
        readNext(i);
      }
    }

    private void readNext(int pos) throws IOException {
      if (!_readers[pos].next(_keys[pos], _values[pos])) {
        _keys[pos] = null;
        _values[pos] = null;
      }
    }

    public boolean next(StatisticWritableContainer key, Text val)
        throws IOException {
      int pos = -1;
      if ((pos = indexOfRecordWithLessestKey()) > -1) {
        key._crawldbStatistic = _keys[pos]._crawldbStatistic;
        key._fetchStatistic = _keys[pos]._fetchStatistic;
        val.set(_values[pos]);
        readNext(pos);
        return true;
      }
      return false;
    }

    private int indexOfRecordWithLessestKey() {
      int index = -1;
      WritableComparable bestKey = null;
      for (int i = 0; i < _keys.length; i++) {
        WritableComparable record = _keys[i];
        if (record != null && bestKey != null) {
          if (record.compareTo(bestKey) < 0) {
            bestKey = record;
            index = i;
          }
        } else if (bestKey == null && record != null) {
          bestKey = record;
          index = i;
        }
      }

      return index;
    }
  }

  public static class StatisticWritable implements
      WritableComparable<StatisticWritable> {

    public static final int CRAWLDB_TYPE = 1;

    public static final int FETCH_TYPE = 2;

    private long _value = -1;

    private int _type = 0;

    @Override
    public void readFields(DataInput in) throws IOException {
      _type = in.readInt();
      _value = in.readLong();
    }

    @Override
    public void write(DataOutput out) throws IOException {
      out.writeInt(_type);
      out.writeLong(_value);
    }

    @Override
    public String toString() {
      return "" + _value;
    }

    @Override
    public int compareTo(StatisticWritable other) {
      // return _value < other._value ? 1 : _value == other._value ? 0 : -1;
      return _value < other._value ? 1 : -1;
    }

    public long getValue() {
      return _value;
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
      Text utf8 = (Text) key;
      String urlString = utf8.toString();
      URL url = new URL(urlString);
      String host = url.getHost();
      collector.collect(new Text("overall"), _one);
      collector.collect(new Text(host), _one);
    }

    @Override
    public void reduce(Text key, Iterator<StatisticWritable> values,
        OutputCollector<Text, StatisticWritable> collector, Reporter reporter)
        throws IOException {
      long counter = 0;
      Set<Integer> set = new HashSet<Integer>();
      while (values.hasNext()) {
        StatisticWritable statisticWritable = (StatisticWritable) values.next();
        counter = counter + statisticWritable._value;
        set.add(statisticWritable._type);
      }
      if (set.size() != 1) {
        throw new IOException("different types in reduce method found: " + set);
      }
      _sum._value = counter;
      _sum._type = set.iterator().next();
      collector.collect(key, _sum);
    }

    @Override
    public void configure(JobConf jobConf) {
      int type = jobConf
          .getInt("statisticType", StatisticWritable.CRAWLDB_TYPE);
      _one._type = type;
      _one._value = 1;
      _sum._value = 0;
    }

    @Override
    public void close() throws IOException {

    }

  }

  public static class StatisticWritableContainer implements
      WritableComparable<StatisticWritableContainer> {

    private StatisticWritable _crawldbStatistic = new StatisticWritable();

    private StatisticWritable _fetchStatistic = new StatisticWritable();

    @Override
    public void readFields(DataInput in) throws IOException {
      _crawldbStatistic.readFields(in);
      _fetchStatistic.readFields(in);
    }

    @Override
    public void write(DataOutput out) throws IOException {
      _crawldbStatistic.write(out);
      _fetchStatistic.write(out);
    }

    @Override
    public String toString() {
      return _crawldbStatistic.toString() + "," + _fetchStatistic;
    }

    @Override
    public int compareTo(StatisticWritableContainer other) {
      return _fetchStatistic.compareTo(other._fetchStatistic);
    }

    public StatisticWritable getCrawldbStatistic() {
      return _crawldbStatistic;
    }

    public StatisticWritable getFetchStatistic() {
      return _fetchStatistic;
    }

  }

  public static class StatisticWritableContainerMerger
      implements
      Mapper<Text, StatisticWritable, Text, StatisticWritableContainer>,
      Reducer<Text, StatisticWritableContainer, Text, StatisticWritableContainer> {

    @Override
    public void map(Text key, StatisticWritable value,
        OutputCollector<Text, StatisticWritableContainer> collector,
        Reporter reporter) throws IOException {

      StatisticWritableContainer container = new StatisticWritableContainer();
      StatisticWritable statisticWritable = (StatisticWritable) value;
      long longValue = statisticWritable._value;
      int type = statisticWritable._type;
      switch (type) {
      case StatisticWritable.CRAWLDB_TYPE:
        container._crawldbStatistic._value = longValue;
        break;
      case StatisticWritable.FETCH_TYPE:
        container._fetchStatistic._value = longValue;
        break;
      default:
        throw new IOException("unknown type: " + type);
      }
      collector.collect(key, container);
    }

    @Override
    public void reduce(Text key, Iterator<StatisticWritableContainer> values,
        OutputCollector<Text, StatisticWritableContainer> collector,
        Reporter reporter) throws IOException {

      StatisticWritableContainer container = new StatisticWritableContainer();
      while (values.hasNext()) {
        StatisticWritableContainer value = (StatisticWritableContainer) values
            .next();
        long longValue = value._crawldbStatistic._value;
        if (longValue != -1L) {
          container._crawldbStatistic._value = longValue;
        }
        longValue = value._fetchStatistic._value;
        if (longValue != -1L) {
          container._fetchStatistic._value = longValue;
        }
      }
      collector.collect(key, container);
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
    countJob.setInt("statisticType", StatisticWritable.CRAWLDB_TYPE);
    JobClient.runJob(countJob);

    LOG.info("START FETCH STATISTIC");
    name = "fetch-statistic-temp-" + id;
    Path tempFetch = new Path(getConf().get("mapred.temp.dir", "."), name);
    countJob = createCountJob(INPUT_OPTION_FETCH_DIR, segment, tempFetch);
    countJob.setInt("statisticType", StatisticWritable.FETCH_TYPE);
    JobClient.runJob(countJob);

    LOG.info("START MERGE");
    name = "merge-statistic-temp-" + id;
    Path tempMerge = new Path(getConf().get("mapred.temp.dir", "."), name);
    JobConf mergeJob = createMergeJob(new Path[] { tempCrawldb, tempFetch },
        tempMerge);
    JobClient.runJob(mergeJob);

    name = "sequence-statistic-temp-" + id;
    Path tempSequence = new Path(getConf().get("mapred.temp.dir", "."), name);
    JobConf sequenceJob = createSequenceFileJob(tempMerge, tempSequence);
    JobClient.runJob(sequenceJob);

    // sort the output files into one file
    Sorter sorter = new SequenceFile.Sorter(fileSystem,
        StatisticWritableContainer.class, Text.class, getConf());
    FileStatus[] listStatus = fileSystem.listStatus(tempSequence);
    Path[] paths = new Path[listStatus.length];
    int c = 0;
    for (FileStatus status : listStatus) {
      paths[c++] = status.getPath();
    }
    sorter.sort(paths, out, false);

    fileSystem.delete(tempCrawldb, true);
    fileSystem.delete(tempFetch, true);
    fileSystem.delete(tempMerge, true);
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

  private JobConf createMergeJob(Path[] files, Path out) {
    JobConf job = new JobConf();
    job.setJobName("merge_host_count ");
    for (Path file : files) {
      FileInputFormat.addInputPath(job, file);
    }
    job.setInputFormat(SequenceFileInputFormat.class);

    job.setMapperClass(StatisticWritableContainerMerger.class);
    job.setReducerClass(StatisticWritableContainerMerger.class);

    FileOutputFormat.setOutputPath(job, out);
    job.setOutputFormat(MapFileOutputFormat.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(StatisticWritableContainer.class);
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
    sortJob.setOutputKeyClass(StatisticWritableContainer.class);
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
