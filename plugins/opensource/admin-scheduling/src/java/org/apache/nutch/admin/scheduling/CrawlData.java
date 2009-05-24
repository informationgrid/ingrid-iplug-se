package org.apache.nutch.admin.scheduling;

import java.io.File;
import java.io.Serializable;

public class CrawlData implements Serializable {

  private static final long serialVersionUID = 2982502019868199903L;

  private Integer _depth;

  private Integer _topn;

  private File _workingDirectory;

  public Integer getDepth() {
    return _depth;
  }

  public void setDepth(Integer depth) {
    _depth = depth;
  }

  public Integer getTopn() {
    return _topn;
  }

  public void setTopn(Integer topn) {
    _topn = topn;
  }

  public File getWorkingDirectory() {
    return _workingDirectory;
  }

  public void setWorkingDirectory(File workingDirectory) {
    _workingDirectory = workingDirectory;
  }

  @Override
  public String toString() {
    return "depth: " + _depth + " topN: " + _topn + " directory:"
        + _workingDirectory.getAbsolutePath();
  }

}
