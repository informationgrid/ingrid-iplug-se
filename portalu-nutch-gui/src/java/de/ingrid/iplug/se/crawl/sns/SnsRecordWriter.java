package de.ingrid.iplug.se.crawl.sns;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;

import de.ingrid.iplug.sns.SNSIndexingInterface;
import de.ingrid.iplug.sns.Temporal;
import de.ingrid.iplug.sns.Wgs84Box;

public class SnsRecordWriter implements RecordWriter<Text, CompressedSnsData> {

  private static final String SNS_PROPERTIES = "/sns.properties";

  private static final Log LOG = LogFactory.getLog(SnsRecordWriter.class);

  // private MapFile.Writer _writer;
  private SequenceFile.Writer _writer;

  private final SNSIndexingInterface _snsInterface;

  private long _time;

  private long _counter;

  private long _errorCounter;

  private SimpleDateFormat _dateFormat;

  private int _maxAnalyzing;

  private int _maxBuzzwords;

  private int _maxTopicIds;

  private int _maxCommunityCodes;

  private int _maxLocationCodes;

  private int _maxT0s;

  private int _maxT1T2s;

  public SnsRecordWriter(SequenceFile.Writer writer) throws Exception {
    InputStream snsPropertiesStream = SnsRecordWriter.class.getResourceAsStream(SNS_PROPERTIES);

    if (snsPropertiesStream == null) {
      throw new RuntimeException("Sns properties does not exists in classpath. Check file '" + SNS_PROPERTIES
          + "' is in classpath.");
    }
    Properties properties = new Properties();
    properties.load(snsPropertiesStream);
    _maxAnalyzing = Integer.parseInt((String) properties.get("maxWordForAnalyzing"));
    String userName = (String) properties.get("username");
    String password = (String) properties.get("password");
    String language = (String) properties.get("language");
    String timeout = (String) properties.get("timeout");
    _maxBuzzwords = Integer.parseInt((String) properties.get("maxBuzzwords"));
    _maxTopicIds = Integer.parseInt((String) properties.get("maxTopicIds"));
    _maxCommunityCodes = Integer.parseInt((String) properties.get("maxCommunityCodes"));
    _maxLocationCodes = Integer.parseInt((String) properties.get("maxLocationCodes"));
    _maxT0s = Integer.parseInt((String) properties.get("maxT0s"));
    _maxT1T2s = Integer.parseInt((String) properties.get("maxT1T2s"));

    _snsInterface = new SNSIndexingInterface(userName, password, language);
    _snsInterface.setTimeout(Integer.parseInt(timeout));
    _writer = writer;
    _dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
    _dateFormat.applyPattern("yyyyMMdd");
  }

  public void close(Reporter reporter) throws IOException {
    _writer.close();
  }

  @Override
  public synchronized void write(Text key, CompressedSnsData snsData) throws IOException {
    LOG.info("sns analyzing: " + key);
    snsData.resetValues();
    try {
      long currentTimeMillis = System.currentTimeMillis();
      addBuzzwords(snsData);
      addCoordinates(snsData);
      addDate(snsData);
      addLocations(snsData);

      _time = _time + (System.currentTimeMillis() - currentTimeMillis);
      _counter++;
      if (_counter % 50 == 0) {
        LOG.info(_counter + " urls sns-analyzed in " + (_time / 1000) + " sec. # ~" + ((_time / 1000) / _counter)
            + " sec. per url # with " + _errorCounter + " errors");
      }
      _writer.append(key, snsData);
    } catch (Exception e) {
      _errorCounter++;
      LOG.warn(e.getMessage());
    }
  }

  private void addBuzzwords(CompressedSnsData snsData) throws Exception {
    String text = snsData.getText().toString();
    if (text.length() <= 0) {
      return;
    }
    int wordLength = 10;
    // Call getBuzzwords() first, to get results when calling methods results from
    // getReferencesToTime() and getReferencesToSpace() later.
    String[] buzzwords = _snsInterface.getBuzzwords(text.length() > (_maxAnalyzing * wordLength) ? text.substring(0,
        _maxAnalyzing * wordLength) : text, _maxAnalyzing, false, null);
    Set<Text> set = new HashSet<Text>();
    for (int i = 0; i < buzzwords.length && set.size() < _maxBuzzwords; i++) {
      set.add(new Text(buzzwords[i]));
    }
    snsData.setBuzzwords(set);
  }

  /**
   * @param data
   * @throws Exception
   */
  private void addCoordinates(CompressedSnsData data) throws Exception {
    Wgs84Box[] referencesToSpace = _snsInterface.getReferencesToSpace();
    double x1 = Double.MAX_VALUE;
    double x2 = Double.MIN_VALUE;
    double y1 = Double.MAX_VALUE;
    double y2 = Double.MIN_VALUE;
    boolean coordinatesFound = false;
    Set<Text> codes = new HashSet<Text>();
    for (int i = 0; i < referencesToSpace.length; i++) {
      coordinatesFound = true;
      data.setCoordinatesFound(coordinatesFound);
      Wgs84Box box = referencesToSpace[i];

      String communityCode = box.getGemeindekennziffer();
      if (codes.size() < _maxCommunityCodes && communityCode != null && !communityCode.equals("")) {
        codes.add(new Text(communityCode));
      }
      if (box.getX1() < x1) {
        x1 = box.getX1();
      }
      if (box.getX2() > x2) {
        x2 = box.getX2();
      }
      if (box.getY1() < y1) {
        y1 = box.getY1();
      }
      if (box.getY2() > y2) {
        y2 = box.getY2();
      }
    }
    data.setCommunityCodes(codes);
    String[] topicIdArray = _snsInterface.getTopicIds();
    Set<Text> topicIds = new HashSet<Text>();
    for (int i = 0; i < topicIdArray.length && topicIds.size() < _maxTopicIds; i++) {
      topicIds.add(new Text(topicIdArray[i]));
    }
    data.setTopicIds(topicIds);

    if (coordinatesFound) {
      data.setX1(new Text(DoublePadding.padding(x1)));
      data.setX2(new Text(DoublePadding.padding(x2)));
      data.setY1(new Text(DoublePadding.padding(y1)));
      data.setY2(new Text(DoublePadding.padding(y2)));
    }
  }

  private void addLocations(CompressedSnsData snsData) {
      // just copy up to _maxLocationCodes locations from SNSIndexingInterface
      // into the snsData
      Set<String> locations = _snsInterface.getLocations();
      Set<Text> locationsAsText = new LinkedHashSet<Text>();

      for (String location : locations) {
          locationsAsText.add(new Text(location));
          if (locationsAsText.size() >= _maxLocationCodes) {
              break;
          }
      }
      snsData.setLocations(locationsAsText);
  }

  /**
   * @param data
   * @throws Exception
   */
  private void addDate(CompressedSnsData data) throws Exception {
    Temporal[] referencesToTime = _snsInterface.getReferencesToTime();
    Set<Text> t0 = new HashSet<Text>();
    Set<Text> t1t2 = new HashSet<Text>();
    for (int i = 0; i < referencesToTime.length; i++) {
      Temporal temporal = referencesToTime[i];
      Date at = temporal.getAt();
      Date from = temporal.getFrom();
      Date to = temporal.getTo();

      if (at != null) {
        String atString = _dateFormat.format(at);
        if (t0.size() < _maxT0s) {
          t0.add(new Text(atString));
        }
      }
      if (from != null && to != null) {
        String fromString = _dateFormat.format(from);
        String toString = _dateFormat.format(to);
        if (t1t2.size() < _maxT1T2s) {
          t1t2.add(new Text(fromString + "\t" + toString));
        }
      }
    }
    data.setT0(t0);
    data.setT1T2(t1t2);
  }

}
