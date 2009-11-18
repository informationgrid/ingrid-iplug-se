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

package org.apache.nutch.crawl.bw;

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
import org.apache.nutch.crawl.CrawlDbFilter;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

/**
 * CrawlDb update tool that only updates urls that passing a white-black list
 * 
 * @see BWInjector
 */
public class BWUpdateDb extends Configured {

  public static final Log LOG = LogFactory.getLog(BWUpdateDb.class);

  public static class ObjectWritableMapper implements
          Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

    @Override
    public void map(HostTypeKey key, Writable value,
            OutputCollector<HostTypeKey, ObjectWritable> collector,
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
  public static class BWMapper implements
          Mapper<Text, CrawlDatum, HostTypeKey, Entry> {

    @Override
    public void map(Text key, CrawlDatum value,
            OutputCollector<HostTypeKey, Entry> out, Reporter rep)
            throws IOException {

      String host = new URL((key).toString()).getHost();
      Entry entry = new Entry(key, value);
      out.collect(new HostTypeKey(host, HostTypeKey.CRAWL_DATUM_TYPE), entry);
    }

    public void configure(JobConf conf) {
    }

    public void close() throws IOException {
    }

  }

  /**
   * Collects only entries that match a white list entry and no black list entry
   */
  public static class BwReducer implements
          Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable> {

    private BWPatterns _patterns;

    public void reduce(HostTypeKey key, Iterator<ObjectWritable> values,
            OutputCollector<HostTypeKey, ObjectWritable> out, Reporter report)
            throws IOException {

      while (values.hasNext()) {
        ObjectWritable objectWritable = (ObjectWritable) values.next();
        Object value = objectWritable.get(); // unwrap
        
        if (value instanceof BWPatterns) {
          _patterns = (BWPatterns) value;
          // next values should be a list of entries
          return;
        }

        if (_patterns == null) {
          return;
        }

        String url = (((Entry) value)._url).toString();
        if (_patterns.willPassBWLists(url)) {
          // url is outside the black list and matches the white list
          out.collect(key, objectWritable);
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
  public static class FormatConverter implements
          Mapper<HostTypeKey, ObjectWritable, Text, CrawlDatum> {
    public void map(HostTypeKey key, ObjectWritable value,
            OutputCollector<Text, CrawlDatum> out, Reporter rep)
            throws IOException {
      Entry entry = (Entry) value.get();
      out.collect(entry._url, entry._crawlDatum);
    }

    public void configure(JobConf arg0) {
    }

    public void close() throws IOException {
    }
  }

  public BWUpdateDb(Configuration conf) {
    super(conf);
  }

  // TODO use normalize and filter inside the bw-job? and not only in the
  // crawldb-job.
  public void update(Path crawlDb, Path bwdb, Path[] segments,
          boolean normalize, boolean filter) throws IOException {
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
      FileInputFormat.addInputPath(job, new Path(segment,
              CrawlDatum.FETCH_DIR_NAME)); // ??
      FileInputFormat.addInputPath(job, new Path(segment,
              CrawlDatum.PARSE_DIR_NAME));
    }

    job.setMapperClass(BWMapper.class);

    FileOutputFormat.setOutputPath(job, wrappedSegOutput);
    job.setOutputFormat(MapFileOutputFormat.class);
    job.setOutputKeyClass(HostTypeKey.class);
    job.setOutputValueClass(Entry.class);
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
    boolean additionsAllowed = getConf().getBoolean(
            CrawlDb.CRAWLDB_ADDITIONS_ALLOWED, true);
    updateJob.setBoolean(CrawlDb.CRAWLDB_ADDITIONS_ALLOWED, additionsAllowed);
    updateJob.setBoolean(CrawlDbFilter.URL_FILTERING, filter);
    updateJob.setBoolean(CrawlDbFilter.URL_NORMALIZING, normalize);

    FileInputFormat.addInputPath(updateJob, tmpFormatOut);
    LOG.info("bw update: Merging bw filtered segment data into db.");
    JobClient.runJob(updateJob);
    FileSystem.get(job).delete(tmpFormatOut, true);

    LOG.info("install crawldb");
    CrawlDb.install(updateJob, crawlDb);
    LOG.info("bw update: done");

  }

  public static void main(String[] args) throws Exception {
    BWUpdateDb bwDb = new BWUpdateDb(NutchConfiguration.create());
    if (args.length != 5) {
      System.err
              .println("Usage: <crawldb> <bwdb> <segment> <normalize> <filter>");
      return;
    }
    bwDb.update(new Path(args[0]), new Path(args[1]), new Path[] { new Path(
            args[2]) }, Boolean.valueOf(args[3]), Boolean.valueOf(args[4]));
  }

}
