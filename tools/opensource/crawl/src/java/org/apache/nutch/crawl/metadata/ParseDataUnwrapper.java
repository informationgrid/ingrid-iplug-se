package org.apache.nutch.crawl.metadata;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.nutch.crawl.metadata.ParseDataWrapper.UrlParseDataContainer;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.NutchJob;

public class ParseDataUnwrapper extends Configured {

  public static class ParseDataUnwrapperMapper implements
      Mapper<HostType, ObjectWritable, Text, ParseData> {

    @Override
    public void map(HostType key, ObjectWritable value,
        OutputCollector<Text, ParseData> out, Reporter reporter)
        throws IOException {
      UrlParseDataContainer container = (UrlParseDataContainer) value.get();
      Text url = container.getUrl();
      ParseData parseData = container.getParseData();
      out.collect(url, parseData);
    }

    @Override
    public void configure(JobConf arg0) {
    }

    @Override
    public void close() throws IOException {
    }

  }
  public ParseDataUnwrapper(Configuration configuration) {
    super(configuration);
  }

  public void unwrap(Path wrappedParseData, Path out) throws IOException {
    JobConf convertJob = new NutchJob(getConf());
    convertJob.setJobName("format converting: " + wrappedParseData);
    FileInputFormat.addInputPath(convertJob, wrappedParseData);
    convertJob.setInputFormat(SequenceFileInputFormat.class);
    convertJob.setMapperClass(ParseDataUnwrapperMapper.class);
    FileOutputFormat.setOutputPath(convertJob, out);
    FileOutputFormat.setCompressOutput(convertJob, true);
    convertJob.setOutputFormat(MapFileOutputFormat.class);
    convertJob.setOutputKeyClass(Text.class);
    convertJob.setOutputValueClass(ParseData.class);
    JobClient.runJob(convertJob);
  }
}
