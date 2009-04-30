package org.apache.nutch.admin.scheduling;

public class CrawlCommand {

  private Integer _depth;

  private Integer _topn;

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

}
