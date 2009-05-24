package org.apache.nutch.admin.scheduling;

public class AdvancedCommand extends CrawlCommand {

  private String _pattern;

  public String getPattern() {
    return _pattern;
  }

  public void setPattern(String pattern) {
    _pattern = pattern;
  }

}
