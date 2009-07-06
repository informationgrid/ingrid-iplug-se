package org.apache.nutch.crawl.prepare;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.IPreCrawl;
import org.apache.nutch.crawl.Injector;

public class CrawldbPrepare implements IPreCrawl {

  private Configuration _conf;
  private static final Log LOG = LogFactory.getLog(CrawldbPrepare.class);

  @Override
  public void preCrawl(Path crawlDir) throws IOException {
    Path crawlDb = new Path(crawlDir, "crawldb");
    Path urlDir = new Path(crawlDir, "urls/start");
    LOG.info("prepare crawldb [" + crawlDb + "] from url dir [" + urlDir + "]");
    Injector injector = new Injector(_conf);
    injector.inject(crawlDb, urlDir);
  }

  @Override
  public Configuration getConf() {
    return _conf;
  }

  @Override
  public void setConf(Configuration conf) {
    _conf = conf;
  }

}
