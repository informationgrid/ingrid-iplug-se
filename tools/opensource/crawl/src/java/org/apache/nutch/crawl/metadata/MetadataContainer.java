package org.apache.nutch.crawl.metadata;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.io.Writable;
import org.apache.nutch.metadata.Metadata;

public class MetadataContainer implements Writable {

  private List<Metadata> _metadatas = new ArrayList<Metadata>();

  public MetadataContainer() {
  }

  public MetadataContainer(Metadata... metadatas) {
    for (Metadata metadata : metadatas) {
      _metadatas.add(metadata);
    }
  }

  public void addMetadata(Metadata metadata) {
    _metadatas.add(metadata);
  }

  public List<Metadata> getMetadatas() {
    return _metadatas;
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    int size = in.readInt();
    for (int i = 0; i < size; i++) {
      Metadata metadata = new Metadata();
      metadata.readFields(in);
      _metadatas.add(metadata);
    }

  }

  @Override
  public void write(DataOutput out) throws IOException {
    out.writeInt(_metadatas.size());
    for (Metadata metadata : _metadatas) {
      metadata.write(out);
    }
  }

}
