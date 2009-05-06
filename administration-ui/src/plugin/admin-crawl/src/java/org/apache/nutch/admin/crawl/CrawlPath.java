package org.apache.nutch.admin.crawl;

import org.apache.hadoop.fs.Path;

public class CrawlPath {

  private Path _path;

  private long _size;

  public Path getPath() {
    return _path;
  }

  public void setPath(Path path) {
    _path = path;
  }

  public long getSize() {
    return _size;
  }

  public void setSize(long len) {
    _size = len;
  }

}
