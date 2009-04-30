package org.apache.nutch.admin.scheduling;

import java.io.File;

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

    LOG.info("try to get lock...");
    if (!LOCK) {
      LOG.info("success...");
      LOG.info("lock the scheduled crawl");
      LOCK = true;
      try {
        CrawlData crawlData = _crawlDataPersistence.loadCrawlData();
        Integer depth = crawlData.getDepth();
        Integer topn = crawlData.getTopn();
        File workingDirectory = crawlData.getWorkingDirectory();
        _crawlTool.crawl(workingDirectory, topn, depth);
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
