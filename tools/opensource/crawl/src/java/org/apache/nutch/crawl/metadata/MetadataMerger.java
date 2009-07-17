package org.apache.nutch.crawl.metadata;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
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
import org.apache.nutch.crawl.metadata.MetadataInjector.MetadataContainer;
import org.apache.nutch.util.NutchJob;

public class MetadataMerger extends Configured {

  public static final Log LOG = LogFactory.getLog(MetadataMerger.class);

  public static class ObjectWritableMapper implements
      Mapper<HostType, Writable, HostType, ObjectWritable> {

    @Override
    public void map(HostType key, Writable value,
        OutputCollector<HostType, ObjectWritable> collector, Reporter reporter)
        throws IOException {
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

  public static class BwReducer implements
      Reducer<HostType, ObjectWritable, HostType, ObjectWritable> {

    private MetadataContainer _patterns;

    public void reduce(HostType key, Iterator<ObjectWritable> values,
        OutputCollector<HostType, ObjectWritable> out, Reporter report)
        throws IOException {

      System.out.println("MetadataMerger.BwReducer.reduce() NEW");
      while (values.hasNext()) {
        ObjectWritable obj = (ObjectWritable) values.next();
        Object value = obj.get(); // unwrap

        System.out.println("MetadataMerger.BwReducer.reduce(): "
            + value.getClass());
        // if (value instanceof MetadataContainer) {
        // _patterns = (MetadataContainer) value;
        // // next values should be a list of entries
        // return;
        // }
        //
        // if (_patterns == null) {
        // return;
        // }



      }
    }

    public void configure(JobConf arg0) {
    }

    public void close() throws IOException {
    }

  }

  public void merge(Path metadataDb, Path wrappedParseData, Path out)
      throws IOException {
    LOG.info("metadata update: merge started.");
    JobConf mergeJob = new NutchJob(getConf());
    mergeJob.setJobName("merging: " + metadataDb + " and " + wrappedParseData);
    mergeJob.setInputFormat(SequenceFileInputFormat.class);

    FileInputFormat.addInputPath(mergeJob, new Path(metadataDb, "current"));
    FileInputFormat.addInputPath(mergeJob, wrappedParseData);
    FileOutputFormat.setOutputPath(mergeJob, out);
    mergeJob.setMapperClass(ObjectWritableMapper.class);
    mergeJob.setReducerClass(BwReducer.class);
    mergeJob.setOutputFormat(MapFileOutputFormat.class);
    mergeJob.setOutputKeyClass(HostType.class);
    mergeJob.setOutputValueClass(ObjectWritable.class);
    JobClient.runJob(mergeJob);

  }
}
