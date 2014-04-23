package de.ingrid.iplug.se.crawl.sns;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

public class SnsData implements Writable {

  private List<Text> _buzzwords = new ArrayList<Text>();

  private List<Text> _communityCodes = new ArrayList<Text>();

  private List<Text> _topicIds = new ArrayList<Text>();

  private List<Text> _t0s = new ArrayList<Text>();

  private List<Text> _t1t2s = new ArrayList<Text>();

  private Text _x1 = new Text("");

  private Text _x2 = new Text("");

  private Text _y1 = new Text("");

  private Text _y2 = new Text("");

  private transient Text _text = new Text("");

  public static final String DIR_NAME = "sns_data";

  private boolean _coordinatesFound;

  // READ FIELDS
  public void readFields(DataInput datainput) throws IOException {
    readUtf8IntoList(_buzzwords, datainput);
    readUtf8IntoList(_communityCodes, datainput);
    readUtf8IntoList(_topicIds, datainput);
    readUtf8IntoList(_t0s, datainput);
    readUtf8IntoList(_t1t2s, datainput);
    _coordinatesFound = datainput.readBoolean();
    if (_coordinatesFound) {
      _x1 = readUtf8(datainput);
      _x2 = readUtf8(datainput);
      _y1 = readUtf8(datainput);
      _y2 = readUtf8(datainput);
    }
  }

  private void readUtf8IntoList(List<Text> list, DataInput dataInput) throws IOException {
    int length = dataInput.readInt();
    list.clear();
    for (int i = 0; i < length; i++) {
      Text utf8 = readUtf8(dataInput);
      list.add(utf8);
    }
  }

  private Text readUtf8(DataInput dataInput) throws IOException {
    Text utf8 = new Text();
    utf8.readFields(dataInput);
    return utf8;
  }

  // WRITE FIELDS
  public void write(DataOutput dataoutput) throws IOException {
    writeUtf8OutOfList(_buzzwords, dataoutput);
    writeUtf8OutOfList(_communityCodes, dataoutput);
    writeUtf8OutOfList(_topicIds, dataoutput);
    writeUtf8OutOfList(_t0s, dataoutput);
    writeUtf8OutOfList(_t1t2s, dataoutput);
    dataoutput.writeBoolean(_coordinatesFound);
    if (_coordinatesFound) {
      _x1.write(dataoutput);
      _x2.write(dataoutput);
      _y1.write(dataoutput);
      _y2.write(dataoutput);
    }
  }

  private void writeUtf8OutOfList(List<Text> list, DataOutput dataOutput) throws IOException {
    dataOutput.writeInt(list.size());
    for (int i = 0; i < list.size(); i++) {
      Text utf8 = list.get(i);
      utf8.write(dataOutput);
    }
  }

  public void setBuzzwords(List<Text> buzzwords) {
    _buzzwords = buzzwords;
  }

  public void setText(String text) {
    _text = new Text(text);
  }

  public String getText() {
    return _text.toString();
  }

  public void setCommunityCodes(List<Text> communityCodes) {
    _communityCodes = communityCodes;
  }

  public void setX1(String x1) {
    _x1 = new Text(x1);
  }

  public void setX2(String x2) {
    _x2 = new Text(x2);
  }

  public void setY1(String y1) {
    _y1 = new Text(y1);
  }

  public void setY2(String y2) {
    _y2 = new Text(y2);
  }

  public void setT0(List<Text> t0s) {
    _t0s = t0s;
  }

  public void setT1T2(List<Text> t) {
    _t1t2s = t;
  }

  public List<Text> getBuzzwords() {
    return _buzzwords;
  }

  public List<Text> getT0s() {
    return _t0s;
  }

  public List<Text> getT1T2s() {
    return _t1t2s;
  }

  public List<Text> getCommunityCodes() {
    return _communityCodes;
  }

  public void setTopicIds(List<Text> topids) {
    _topicIds = topids;
  }

  public List<Text> getTopicIds() {
    return _topicIds;
  }

  public Text getX1() {
    return _x1;
  }

  public Text getX2() {
    return _x2;
  }

  public Text getY1() {
    return _y1;
  }

  public Text getY2() {
    return _y2;
  }

  public void resetValues() {
    _buzzwords.clear();
    _communityCodes.clear();
    _t0s.clear();
    _t1t2s.clear();
    _topicIds.clear();
    _x1 = new Text("");
    _x2 = new Text("");
    _y1 = new Text("");
    _y2 = new Text("");
    _coordinatesFound = false;
  }

  public void setCoordinatesFound(boolean coordinatesFound) {
    _coordinatesFound = coordinatesFound;
  }

  public boolean isCoordinatesFound() {
    return _coordinatesFound;
  }
}
