package org.apache.nutch.admin.scheduling;

import org.apache.nutch.admin.CrawlTool;

public class SchedulingRunnable implements Runnable {

  private final CrawlTool _crawlTool;

  public SchedulingRunnable(CrawlTool crawlTool) {
    _crawlTool = crawlTool;
  }

  @Override
  public void run() {
    _crawlTool.crawl();
  }

}
