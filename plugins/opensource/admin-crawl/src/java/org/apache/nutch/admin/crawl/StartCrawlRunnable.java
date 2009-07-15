package org.apache.nutch.admin.crawl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.CrawlTool;

public class StartCrawlRunnable implements Runnable {

  private final CrawlTool _crawlTool;
  private final Integer _topN;
  private final Integer _depth;
  private static final Log LOG = LogFactory.getLog(PreCrawlRunnable.class);

  public StartCrawlRunnable(final CrawlTool crawlTool, Integer topN,
      Integer depth) {
    _crawlTool = crawlTool;
    _topN = topN;
    _depth = depth;
  }

  @Override
  public void run() {
    FileSystem fileSystem = _crawlTool.getFileSystem();
    Path crawlDir = _crawlTool.getCrawlDir();
    Path lockPath = new Path(crawlDir, "crawl.running");
    try {
      fileSystem.createNewFile(lockPath);
      _crawlTool.crawl(_topN, _depth);
    } catch (IOException e) {
      LOG.warn("can not start crawl.", e);
    } finally {
      try {
        fileSystem.delete(lockPath, false);
      } catch (IOException e) {
        LOG.warn("can not delete lock file.", e);
      }
    }
  }

}
