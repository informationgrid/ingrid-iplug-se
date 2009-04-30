package org.apache.nutch.admin.scheduling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.CrawlTool;

public class SchedulingRunnable implements Runnable {

  private final CrawlTool _crawlTool;

  private boolean LOCK = false;

  private static final Log LOG = LogFactory.getLog(SchedulingRunnable.class);

  private final CrawlDataPersistence _crawlDataPersistence;

  public SchedulingRunnable(CrawlDataPersistence crawlDataPersistence) {
    _crawlDataPersistence = crawlDataPersistence;
    _crawlTool = new CrawlTool();
  }

  @Override
  public void run() {

    CrawlData crawlData = null;
    try {
      crawlData = _crawlDataPersistence.loadCrawlData();
    } catch (Exception e) {
      LOG.error("can not load crawl data.", e);
      return;
    }
    LOG.info("try to get lock for directory: "
        + crawlData.getWorkingDirectory().getAbsolutePath());
    if (!LOCK) {
      LOG.info("success.");
      LOG.info("lock the scheduled crawl: "
          + crawlData.getWorkingDirectory().getAbsolutePath());
      LOCK = true;
      try {
        _crawlTool.crawl(crawlData.getWorkingDirectory(), crawlData.getTopn(),
            crawlData.getDepth());
      } catch (Throwable e) {
      } finally {
        LOG.info("unlock the scheduled crawl: "
            + crawlData.getWorkingDirectory().getAbsolutePath());
        LOCK = false;
      }
    } else {
      LOG.info("fails...");
      LOG.info("crawl is locked: "
          + crawlData.getWorkingDirectory().getAbsolutePath());
    }

  }

}
