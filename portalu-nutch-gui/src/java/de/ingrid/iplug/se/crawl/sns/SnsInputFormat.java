package de.ingrid.iplug.se.crawl.sns;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;

public class SnsInputFormat implements InputFormat<Text, CompressedSnsData> {

  @Override
  public RecordReader<Text, CompressedSnsData> getRecordReader(InputSplit arg0, JobConf arg1, Reporter arg2)
      throws IOException {
    return new SnsInMemoryRecordReader();
  }

  @Override
  public InputSplit[] getSplits(JobConf arg0, int arg1) throws IOException {
    return null;
  }
}
