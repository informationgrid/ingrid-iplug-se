package org.apache.nutch.admin.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.CrawlTool;

public class SchedulingRunnable implements Runnable {

  private final CrawlTool _crawlTool;

  private boolean LOCK = false;

  private static final Log LOG = LogFactory.getLog(SchedulingRunnable.class);

  public SchedulingRunnable() {
    _crawlTool = new CrawlTool();
  }

  @Override
  public void run() {

    LOG.info("try to get lock...");
    if (!LOCK) {
      LOG.info("success...");
      LOG.info("lock the scheduled crawl");
      LOCK = true;
      try {
        _crawlTool.crawl();
      } catch (Throwable e) {
      } finally {
        LOG.info("unlock the scheduled crawl");
        LOCK = false;
      }
    } else {
      LOG.info("fails...");
      LOG.info("crawl is locked");
    }

  }

}
