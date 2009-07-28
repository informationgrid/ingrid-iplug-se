package org.apache.nutch.crawl.metadata;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class HostType implements WritableComparable<HostType> {

  public static final int METADATA_CONTAINER = 1;

  public static final int URL_PARSEDATA_CONTAINER = 2;

  private Text _host = new Text();

  private IntWritable _type = new IntWritable();

  public HostType() {
  }

  public HostType(Text host, int type) {
    _host.set(host);
    _type.set(type);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    _host.readFields(in);
    _type.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    _host.write(out);
    _type.write(out);
  }

  @Override
  public int compareTo(HostType that) {
    int i = _host.compareTo(that._host);
    if (i == 0) {
      // in case host is identically make sure that METADATA_CONTAINER is
      // passing a
      // reducer first
      i = ((_type.get() < that._type.get() ? -1 : (_type.get() == that._type
          .get()) ? 0 : 1));
    }
    return i;
  }

  public Text getHost() {
    return _host;
  }

  @Override
  public int hashCode() {
    return _host.hashCode() + _type.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    HostType other = (HostType) obj;
    return _host.equals(other._host) && _type.equals(other._type);
  }

  @Override
  public String toString() {
    return _host.toString() + " (" + _type + ")";
  }

}
