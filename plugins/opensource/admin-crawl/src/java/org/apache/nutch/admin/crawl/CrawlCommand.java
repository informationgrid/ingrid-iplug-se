package org.apache.nutch.admin.crawl;

public class CrawlCommand {

  private Integer _depth;

  private Integer _topn;

  private String _crawlFolder;

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

  public String getCrawlFolder() {
    return _crawlFolder;
  }

  public void setCrawlFolder(String crawlFolder) {
    _crawlFolder = crawlFolder;
  }

}
