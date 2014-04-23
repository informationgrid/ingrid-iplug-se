package de.ingrid.iplug.se.crawl.sns;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputFormat;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.RecordWriter;

public class SnsAnalyzer {

  private static final Log LOG = LogFactory.getLog(SnsAnalyzer.class);

  private String _outputDirName;

  private RecordWriter<Text, CompressedSnsData> _recordWriter;

  private RecordReader<Text, CompressedSnsData> _recordReader;

  public SnsAnalyzer(JobConf jobConf) throws IOException {
    _outputDirName = jobConf.get("mapred.output.dir");
    FileSystem fileSystem = FileSystem.get(jobConf);
    InputFormat<Text, CompressedSnsData> inputFormat = new SnsInputFormat();
    OutputFormat<Text, CompressedSnsData> outputFormat = new SnsOutputFormat();
    _recordReader = inputFormat.getRecordReader(null, jobConf, null);
    _recordWriter = outputFormat.getRecordWriter(fileSystem, jobConf, "part-00000", null);

    Runnable runnable = new Runnable() {
      public void run() {
        try {
          Text url = new Text();
          CompressedSnsData snsData = new CompressedSnsData();
          int i = 0;
          while (_recordReader.next(url, snsData)) {
            _recordWriter.write(url, snsData);
            i++;
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        LOG.info("Sns Analyzing finished");
      }
    };
    new Thread(runnable).start();
  }

  public void analyze(Text url, String text) {
    ((SnsInMemoryRecordReader) _recordReader).add(url, text);
  }

  public void close() throws IOException {
    _recordReader.close();
    _recordWriter.close(null);
  }

  public String toString() {
    return _outputDirName;
  }
}
