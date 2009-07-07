package org.apache.nutch.crawl.prepare;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.IPreCrawl;
import org.apache.nutch.crawl.bw.BWInjector;

public class BwdbPrepare implements IPreCrawl {

  private static final Log LOG = LogFactory.getLog(BwdbPrepare.class);
  private Configuration _conf;

  @Override
  public void preCrawl(Path crawlDir) throws IOException {

    LOG.info("bwdb injecting starts...");
    FileSystem fs = FileSystem.get(_conf);
    Path limitUrls = new Path(crawlDir, "urls/limit");
    Path excludeUrls = new Path(crawlDir, "urls/exclude");
    Path bwDb = new Path(crawlDir, "bwdb");

    BWInjector injector = new BWInjector(_conf);
    if (!fs.exists(limitUrls)) {
      LOG.warn("limit urlDir [" + limitUrls
          + "] does not exist. Skip injecting.");
      return;
    }
    LOG.info("inject limit urls [" + limitUrls + "] into bwdb [" + bwDb + "]");
    injector.inject(bwDb, limitUrls, false);

    if (!fs.exists(excludeUrls)) {
      LOG.warn("exclude urlDir [" + excludeUrls
          + "] does not exist. Skip injecting.");
      return;
    }
    LOG.info("inject exclude urls [" + excludeUrls + "] into bwdb [" + bwDb
        + "]");
    injector.inject(bwDb, excludeUrls, true);

    LOG.info("bwdb injecting ends...");
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
