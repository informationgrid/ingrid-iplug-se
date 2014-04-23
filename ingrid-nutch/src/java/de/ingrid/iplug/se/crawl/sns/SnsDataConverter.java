package de.ingrid.iplug.se.crawl.sns;

import java.util.HashSet;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;

public class SnsDataConverter {

  public static void main(String[] args) throws Exception {

    SnsData uncompressedSnsData = new SnsData();
    CompressedSnsData compressedSnsData = new CompressedSnsData();

    Configuration configuration = new Configuration();
    // LocalFileSystem fileSystem = new LocalFileSystem(new Configuration());
    LocalFileSystem fileSystem = new LocalFileSystem();
    SequenceFile.Reader reader = new SequenceFile.Reader(fileSystem, new Path(args[0]), configuration);
    SequenceFile.Writer writer = new SequenceFile.Writer(fileSystem, configuration, new Path(args[1]), Text.class,
        CompressedSnsData.class);

    Text key = new Text();
    int counter = 0;
    while (reader.next(key, uncompressedSnsData)) {

      compressedSnsData.resetValues();
      compressedSnsData.setBuzzwords(new HashSet<Text>(uncompressedSnsData.getBuzzwords()));
      compressedSnsData.setCommunityCodes(new HashSet<Text>(uncompressedSnsData.getCommunityCodes()));
      compressedSnsData.setCoordinatesFound(uncompressedSnsData.isCoordinatesFound());
      compressedSnsData.setT0(new HashSet<Text>(uncompressedSnsData.getT0s()));
      compressedSnsData.setT1T2(new HashSet<Text>(uncompressedSnsData.getT1T2s()));
      compressedSnsData.setTopicIds(new HashSet<Text>(uncompressedSnsData.getTopicIds()));
      compressedSnsData.setX1(uncompressedSnsData.getX1());
      compressedSnsData.setX2(uncompressedSnsData.getX2());
      compressedSnsData.setY1(uncompressedSnsData.getY1());
      compressedSnsData.setY2(uncompressedSnsData.getY2());

      writer.append(key, compressedSnsData);

      counter++;
    }
    writer.close();
  }
}
