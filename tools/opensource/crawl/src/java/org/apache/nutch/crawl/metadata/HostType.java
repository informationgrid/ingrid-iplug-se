package org.apache.nutch.crawl.metadata;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class HostType implements WritableComparable<HostType> {

  public static final IntWritable METADATA_CONTAINER = new IntWritable(0);

  public static final IntWritable URL_PARSEDATA_CONTAINER = new IntWritable(1);

  private Text _host = new Text();

  private IntWritable _type = METADATA_CONTAINER;

  public HostType() {
  }

  public HostType(Text host, IntWritable type) {
    _host = host;
    _type = type;
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

}
