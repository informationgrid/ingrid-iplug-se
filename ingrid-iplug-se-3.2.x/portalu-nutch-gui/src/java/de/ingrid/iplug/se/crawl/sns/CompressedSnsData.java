package de.ingrid.iplug.se.crawl.sns;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.hadoop.io.CompressedWritable;
import org.apache.hadoop.io.Text;

public class CompressedSnsData extends CompressedWritable {

  private Set<Text> _buzzwords = new HashSet<Text>();

  private Set<Text> _communityCodes = new HashSet<Text>();

  private Set<Text> _locations = new HashSet<Text>();

  private Set<Text> _topicIds = new HashSet<Text>();

  private Set<Text> _t0s = new HashSet<Text>();

  private Set<Text> _t1t2s = new HashSet<Text>();

  private Text _x1 = new Text("");

  private Text _x2 = new Text("");

  private Text _y1 = new Text("");

  private Text _y2 = new Text("");

  private transient Text _text = new Text("");

  public static final String DIR_NAME = "sns_data";

  private boolean _coordinatesFound;

  // READ FIELDS
  public void readFieldsCompressed(DataInput datainput) throws IOException {
    readUtf8IntoList(_buzzwords, datainput);
    readUtf8IntoList(_communityCodes, datainput);
    readUtf8IntoList(_locations, datainput);
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

  private void readUtf8IntoList(Set<Text> set, DataInput dataInput)
      throws IOException {
    int length = dataInput.readInt();
    set.clear();
    for (int i = 0; i < length; i++) {
      Text utf8 = readUtf8(dataInput);
      set.add(utf8);
    }
  }

  private Text readUtf8(DataInput dataInput) throws IOException {
    Text utf8 = new Text();
    utf8.readFields(dataInput);
    return utf8;
  }

  // WRITE FIELDS
  public void writeCompressed(DataOutput dataoutput) throws IOException {
    writeUtf8OutOfList(_buzzwords, dataoutput);
    writeUtf8OutOfList(_communityCodes, dataoutput);
    writeUtf8OutOfList(_locations, dataoutput);
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

  private void writeUtf8OutOfList(Set<Text> set, DataOutput dataOutput)
      throws IOException {
    dataOutput.writeInt(set.size());
    Iterator<Text> iterator = set.iterator();
    while (iterator.hasNext()) {
      Text utf8 = (Text) iterator.next();
      utf8.write(dataOutput);
    }
  }

  public void setBuzzwords(Set<Text> buzzwords) {
    ensureInflated();
    _buzzwords = buzzwords;
  }

  public void setText(Text text) {
    _text = text;
  }

  public Text getText() {
    return _text;
  }

  public void setCommunityCodes(Set<Text> communityCodes) {
    ensureInflated();
    _communityCodes = communityCodes;
  }

  public void setLocations(Set<Text> locations) {
      ensureInflated();
      _locations = locations;
  }

  public void setX1(Text x1) {
    ensureInflated();
    _x1 = x1;
  }

  public void setX2(Text x2) {
    ensureInflated();
    _x2 = x2;
  }

  public void setY1(Text y1) {
    ensureInflated();
    _y1 = y1;
  }

  public void setY2(Text y2) {
    ensureInflated();
    _y2 = y2;
  }

  public void setT0(Set<Text> t0s) {
    ensureInflated();
    _t0s = t0s;
  }

  public void setT1T2(Set<Text> t) {
    ensureInflated();
    _t1t2s = t;
  }

  public Set<Text> getBuzzwords() {
    ensureInflated();
    return _buzzwords;
  }

  public Set<Text> getT0s() {
    ensureInflated();
    return _t0s;
  }

  public Set<Text> getT1T2s() {
    ensureInflated();
    return _t1t2s;
  }

  public Set<Text> getCommunityCodes() {
    ensureInflated();
    return _communityCodes;
  }

  public Set<Text> getLocations() {
      ensureInflated();
      return _locations;
  }

  public void setTopicIds(Set<Text> topids) {
    ensureInflated();
    _topicIds = topids;
  }

  public Set<Text> getTopicIds() {
    ensureInflated();
    return _topicIds;
  }

  public Text getX1() {
    ensureInflated();
    return _x1;
  }

  public Text getX2() {
    ensureInflated();
    return _x2;
  }

  public Text getY1() {
    ensureInflated();
    return _y1;
  }

  public Text getY2() {
    ensureInflated();
    return _y2;
  }

  public void resetValues() {
    _buzzwords.clear();
    _communityCodes.clear();
    _locations.clear();
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
    ensureInflated();
    _coordinatesFound = coordinatesFound;
  }

  public boolean isCoordinatesFound() {
    ensureInflated();
    return _coordinatesFound;
  }

  public static CompressedSnsData read(DataInput dataInput) throws IOException {
    CompressedSnsData ret = new CompressedSnsData();
    ret.readFields(dataInput);
    return ret;
  }

}
