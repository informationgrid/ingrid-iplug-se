package org.apache.nutch.crawl.metadata;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
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
import org.apache.nutch.crawl.bw.BWPatterns;
import org.apache.nutch.crawl.bw.HostTypeKey;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

public class MetadataInjector extends Configured {

  private static final Log LOG = LogFactory.getLog(MetadataInjector.class);

  public static class MetadataContainer implements Writable {

    private List<Metadata> _metadatas = new ArrayList<Metadata>();

    public MetadataContainer() {
    }

    public MetadataContainer(Metadata... metadatas) {
      for (Metadata metadata : metadatas) {
        _metadatas.add(metadata);
      }
    }

    public void addMetadata(Metadata metadata) {
      _metadatas.add(metadata);
    }

    public List<Metadata> getMetadatas() {
      return _metadatas;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
      int size = in.readInt();
      for (int i = 0; i < size; i++) {
        Metadata metadata = new Metadata();
        metadata.readFields(in);
        _metadatas.add(metadata);
      }

    }

    @Override
    public void write(DataOutput out) throws IOException {
      out.writeInt(_metadatas.size());
      for (Metadata metadata : _metadatas) {
        metadata.write(out);
      }
    }

  }

  public MetadataInjector(Configuration conf) {
    super(conf);
  }

  /**
   * Creates {@link HostTypeKey} - {@link BWPatterns} tuples from each text
   * line.
   */
  public static class MetadataInjectMapper implements
      Mapper<WritableComparable<Object>, Text, HostType, MetadataContainer> {

    private URLNormalizers _urlNormalizers;

    public void configure(JobConf job) {
      _urlNormalizers = new URLNormalizers(job, URLNormalizers.SCOPE_INJECT);
    }

    public void close() {
    }

    @Override
    public void map(WritableComparable<Object> key, Text val,
        OutputCollector<HostType, MetadataContainer> output, Reporter reporter)
        throws IOException {

      String line = val.toString();
      String[] splits = line.split("\t");
      String url = splits[0];
      String meteKey = null;
      Metadata metadata = new Metadata();
      metadata.add("pattern", url);
      for (int i = 1; i < splits.length; i++) {
        String split = splits[i];
        if (split.endsWith(":")) {
          meteKey = split.substring(0, split.length() - 1);
          continue;
        }
        metadata.add(meteKey, split);
      }

      try {
        url = _urlNormalizers.normalize(url, "metadata"); // normalize
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

        System.out.println(host);
        System.out.println(metadata);
        System.out.println("++++");
        HostType hostType = new HostType(new Text(host),
            HostType.METADATA_CONTAINER);
        MetadataContainer metadataContainer = new MetadataContainer(metadata);
        output.collect(hostType, metadataContainer);
      }
    }

  }

  /**
   * Reduces {@link HostTypeKey} - {@link BWPatterns} tuples
   */
  public static class MetadataInjectReducer implements
      Reducer<HostType, MetadataContainer, HostType, MetadataContainer> {
    public void configure(JobConf job) {
    }

    public void close() {
    }

    @Override
    public void reduce(HostType key, Iterator<MetadataContainer> values,
        OutputCollector<HostType, MetadataContainer> output, Reporter reporter)
        throws IOException {
      MetadataContainer metadataContainer = new MetadataContainer();
      while (values.hasNext()) {
        MetadataContainer next = values.next();
        List<Metadata> nextMetadatas = next.getMetadatas();
        for (Metadata nextMetadata : nextMetadatas) {
          metadataContainer.addMetadata(nextMetadata);
        }

      }
      output.collect(key, metadataContainer);
    }
  }

  public void inject(Path metadataDb, Path urlDir) throws IOException {

    LOG.info("MetadataInjector: starting");
    LOG.info("MetadataInjector: metadataDb: " + metadataDb);
    LOG.info("MetadataInjector: urlDir: " + urlDir);

    String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
    name = "/metadata-inject-temp-" + name;
    Path tempDir = new Path(getConf().get("mapred.temp.dir", ".") + name);

    JobConf sortJob = new NutchJob(getConf());
    sortJob.setJobName("metadata-inject " + urlDir);
    FileInputFormat.addInputPath(sortJob, urlDir);
    sortJob.setMapperClass(MetadataInjectMapper.class);

    FileOutputFormat.setOutputPath(sortJob, tempDir);
    sortJob.setOutputFormat(SequenceFileOutputFormat.class);
    sortJob.setOutputKeyClass(HostType.class);
    sortJob.setOutputValueClass(MetadataContainer.class);
    JobClient.runJob(sortJob);

    // merge with existing bw db
    LOG.info("MetadataInjector: Merging injected urls into metadataDb.");
    String newDbName = Integer
        .toString(new Random().nextInt(Integer.MAX_VALUE));
    Path newDb = new Path(metadataDb, newDbName);

    JobConf job = new NutchJob(getConf());
    job.setJobName("merge metadata " + metadataDb);

    Path current = new Path(metadataDb, "current");
    if (FileSystem.get(job).exists(current)) {
      FileInputFormat.addInputPath(job, current);
    }
    FileInputFormat.addInputPath(job, tempDir);
    job.setInputFormat(SequenceFileInputFormat.class);

    job.setReducerClass(MetadataInjectReducer.class);
    FileOutputFormat.setOutputPath(job, newDb);
    job.setOutputFormat(MapFileOutputFormat.class);
    job.setOutputKeyClass(HostType.class);
    job.setOutputValueClass(MetadataContainer.class);
    JobClient.runJob(job);

    LOG.info("rename metadataDb");
    FileSystem fs = new JobClient(job).getFs();
    Path old = new Path(metadataDb, "old");
    fs.delete(old, true);
    if (fs.exists(current)) {
      fs.rename(current, old);
    }
    fs.rename(newDb, current);
    fs.delete(old, true);

    // clean up
    fs.delete(tempDir, true);
    LOG.info("MetadataInjector: done");
  }

  public static void main(String[] args) throws Exception {
    MetadataInjector injector = new MetadataInjector(NutchConfiguration
        .create());
    if (args.length < 2) {
      System.err.println("Usage: MetadataInjector <metadataDb> <urls>");
      return;
    }
    injector.inject(new Path(args[0]), new Path(args[1]));
  }
}
