package org.apache.nutch.admin.crawl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.CrawlTool;

public class PreCrawlRunnable implements Runnable {

  private final CrawlTool _crawlTool;

  private static final Log LOG = LogFactory.getLog(PreCrawlRunnable.class);

  public PreCrawlRunnable(final CrawlTool crawlTool) {
    _crawlTool = crawlTool;
  }

  @Override
  public void run() {
    FileSystem fileSystem = _crawlTool.getFileSystem();
    Path crawlDir = _crawlTool.getCrawlDir();
    Path lockPath = new Path(crawlDir, "crawl.running");
    try {
      fileSystem.createNewFile(lockPath);
      _crawlTool.preCrawl();
    } catch (IOException e) {
      LOG.warn("can not prepare crawl.", e);
    } finally {
      try {
        fileSystem.delete(lockPath, false);
      } catch (IOException e) {
        LOG.warn("can not delete lock file.", e);
      }
    }
  }

}
