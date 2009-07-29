package org.apache.nutch.admin.crawl.statistic;

public class Statistic {

  private String _host;
  private Long _crawldbCount;
  private Long _segmentCount;

  public Statistic(String host, Long crawldbCount, Long segmentCount) {
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

  public Long getCrawldbCount() {
    return _crawldbCount;
  }

  public void setCrawldbCount(Long crawldbCount) {
    _crawldbCount = crawldbCount;
  }

  public Long getSegmentCount() {
    return _segmentCount;
  }

  public void setSegmentCount(Long segmentCount) {
    _segmentCount = segmentCount;
  }

}
