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

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
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
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

/**
 * Injects black- or white list urls into a database. Urls will be normalized
 * until injection.
 * 
 */
public class BWInjector extends Configured {

  private static final Log LOG = LogFactory.getLog(BWInjector.class);

  private static final String PROHIBITED = "prohibited";

  public BWInjector(Configuration conf) {
    super(conf);
  }

  /**
   * Creates {@link HostTypeKey} - {@link BWPatterns} tuples from each text
   * line.
   */
  public static class BWInjectMapper implements
      Mapper<WritableComparable<Object>, Text, HostTypeKey, BWPatterns> {

    private URLNormalizers _urlNormalizers;

    private boolean _prohibited;

    public void configure(JobConf job) {
      _urlNormalizers = new URLNormalizers(job, URLNormalizers.SCOPE_INJECT);
      _prohibited = job.getBoolean(PROHIBITED, true);
    }

    public void close() {
    }

    @Override
    public void map(WritableComparable<Object> key, Text val,
        OutputCollector<HostTypeKey, BWPatterns> output, Reporter reporter)
        throws IOException {

      String url = val.toString();
      try {
        url = _urlNormalizers.normalize(url, "bw"); // normalize
      } catch (Exception e) {
        LOG.warn("Skipping " + url + ":" + e.toString());
        url = null;
      }
      if (url != null) {
        String host;
        try {
          host = new URL(url).getHost();
        } catch (Exception e) {
          LOG.warn("unable to get host from: " + url + " : " + e.toString());
          return;
        }
        BWPatterns patterns;
        if (_prohibited) {
          patterns = new BWPatterns(new Text[0], new Text[] { new Text(url) });
        } else {
          patterns = new BWPatterns(new Text[] { new Text(url) }, new Text[0]);
        }
        output.collect(new HostTypeKey(host, HostTypeKey.PATTERN_TYPE),
            patterns);
      }
    }

  }

  /**
   * Reduces {@link HostTypeKey} - {@link BWPatterns} tuples
   */
  public static class BWInjectReducer implements
      Reducer<HostTypeKey, BWPatterns, HostTypeKey, BWPatterns> {
    public void configure(JobConf job) {
    }

    public void close() {
    }

    @Override
    public void reduce(HostTypeKey key, Iterator<BWPatterns> values,
        OutputCollector<HostTypeKey, BWPatterns> output, Reporter reporter)
        throws IOException {
      Set<Text> pos = new HashSet<Text>();
      Set<Text> neg = new HashSet<Text>();

      while (values.hasNext()) {
        BWPatterns value = values.next();
        pos.addAll(Arrays.asList(value._positive));
        neg.addAll(Arrays.asList(value._negative));
      }

      Text[] negative = neg.toArray(new Text[neg.size()]);
      Text[] positive = pos.toArray(new Text[pos.size()]);

      output.collect(key, new BWPatterns(positive, negative));
    }
  }

  public void inject(Path bwDb, Path urlDir, boolean prohibited)
      throws IOException {

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

    JobConf sortJob = new NutchJob(getConf());
    sortJob.setJobName("bw-inject " + urlDir);
    FileInputFormat.addInputPath(sortJob, urlDir);
    sortJob.setMapperClass(BWInjectMapper.class);

    sortJob.setBoolean(PROHIBITED, prohibited);

    FileOutputFormat.setOutputPath(sortJob, tempDir);
    sortJob.setOutputFormat(SequenceFileOutputFormat.class);
    sortJob.setOutputKeyClass(HostTypeKey.class);
    sortJob.setOutputValueClass(BWPatterns.class);
    JobClient.runJob(sortJob);

    // merge with existing bw db
    LOG.info("BWInjector: Merging injected urls into bwDb.");
    String newDbName = Integer
        .toString(new Random().nextInt(Integer.MAX_VALUE));
    Path newDb = new Path(bwDb, newDbName);

    JobConf job = new NutchJob(getConf());
    job.setJobName("merge bwDb " + bwDb);

    Path current = new Path(bwDb, "current");
    if (FileSystem.get(job).exists(current)) {
      FileInputFormat.addInputPath(job, current);
    }
    FileInputFormat.addInputPath(job, tempDir);
    job.setInputFormat(SequenceFileInputFormat.class);

    job.setReducerClass(BWInjectReducer.class);
    FileOutputFormat.setOutputPath(job, newDb);
    job.setOutputFormat(MapFileOutputFormat.class);
    job.setOutputKeyClass(HostTypeKey.class);
    job.setOutputValueClass(BWPatterns.class);
    JobClient.runJob(job);

    LOG.info("rename bwdb");
    FileSystem fs = new JobClient(job).getFs();
    Path old = new Path(bwDb, "old");
    fs.delete(old, true);
    if (fs.exists(current)) {
      fs.rename(current, old);
    }
    fs.rename(newDb, current);
    fs.delete(old, true);

    // clean up
    fs.delete(tempDir, true);
    LOG.info("BWInjector: done");
  }

  public static void main(String[] args) throws Exception {
    BWInjector injector = new BWInjector(NutchConfiguration.create());
    if (args.length < 2) {
      System.err.println("Usage: BWInjector <bwdb> <urls> (-black|-white)");
      return;
    }
    injector.inject(new Path(args[0]), new Path(args[1]), args[2]
        .equalsIgnoreCase("-black"));
  }

}
