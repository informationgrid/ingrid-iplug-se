package de.ingrid.iplug.se.crawl.sns;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.util.Progressable;

public class SnsOutputFormat implements OutputFormat<Text, CompressedSnsData> {

  private static final Log LOG = LogFactory.getLog(SnsOutputFormat.class);

  public static final String SNS_DIR_NAME = "sns_data";

  public void checkOutputSpecs(FileSystem filesystem, JobConf jobconf) throws IOException {
    File file = new File(jobconf.get("mapred.output.dir"), SNS_DIR_NAME);
    if (!file.exists()) {
      file.mkdir();
    }
  }

  @Override
  public RecordWriter<Text, CompressedSnsData> getRecordWriter(FileSystem filesystem, JobConf jobConf, String name,
      Progressable progressable) throws IOException {
    Path snsDataPath = new Path(jobConf.get("mapred.output.dir"), SNS_DIR_NAME);
    Path snsData = new Path(snsDataPath, name);
    LOG.info("Using path '" + snsData + "' for SequenceFile.Writer.");
    SequenceFile.Writer writer = new SequenceFile.Writer(filesystem, jobConf, snsData, Text.class,
        CompressedSnsData.class);
    SnsRecordWriter recordWriter = null;
    try {
      recordWriter = new SnsRecordWriter(writer);
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
    return recordWriter;
  }

}
