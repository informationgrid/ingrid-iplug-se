package org.apache.nutch.admin.crawl.statistic;

public class Statistic {

  private String _host;
  private Long _overallCount;
  private Long _fetchSuccessCount;

  public Statistic(String host, Long overallCount, Long fetchSuccessCount) {
    _host = host;
    _overallCount = overallCount;
    _fetchSuccessCount = fetchSuccessCount;
  }

  public String getHost() {
    return _host;
  }

  public void setHost(String host) {
    _host = host;
  }

  public Long getOverallCount() {
    return _overallCount;
  }

  public void setOverallCount(Long crawldbCount) {
    _overallCount = crawldbCount;
  }

  public Long getFetchSuccessCount() {
    return _fetchSuccessCount;
  }

  public void setFetchSuccessCount(Long segmentCount) {
    _fetchSuccessCount = segmentCount;
  }

}
