package org.apache.nutch.crawl.crawldb;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.IPreCrawl;
import org.apache.nutch.crawl.Injector;

public class CrawldbInject implements IPreCrawl {

  private Configuration _configuration;

  @Override
  public void preCrawl(Path crawlDir) throws IOException {
    System.out.println("CrawldbInject.preCrawl()********");

    Injector injector = new Injector(_configuration);
    injector.inject(crawlDir, new Path("/tmp/urls/urls.csv"));
    
  }

  @Override
  public Configuration getConf() {
    return _configuration;
  }

  @Override
  public void setConf(Configuration configuration) {
    _configuration = configuration;
  }

}
