package org.apache.nutch.admin.crawl.statistic;

public class Statistic {

  private String _host;
  private Integer _crawldbCount;
  private Integer _segmentCount;

  public Statistic(String host, Integer crawldbCount, Integer segmentCount) {
    _host = host;
    _crawldbCount = crawldbCount;
    _segmentCount = segmentCount;
  }

  public String getHost() {
    return _host;
  }

  public void setHost(String host) {
    _host = host;
  }

  public Integer getCrawldbCount() {
    return _crawldbCount;
  }

  public void setCrawldbCount(Integer crawldbCount) {
    _crawldbCount = crawldbCount;
  }

  public Integer getSegmentCount() {
    return _segmentCount;
  }

  public void setSegmentCount(Integer segmentCount) {
    _segmentCount = segmentCount;
  }

}
