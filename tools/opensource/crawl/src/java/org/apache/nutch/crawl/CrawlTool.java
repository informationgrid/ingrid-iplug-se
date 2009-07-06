package org.apache.nutch.crawl;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

public class CrawlTool {

  private final Configuration _configuration;
  private final Path _crawlDir;
  private PreCrawls _preCrawls;

  public CrawlTool(Configuration configuration, Path crawlDir) {
    _configuration = configuration;
    _crawlDir = crawlDir;
    _preCrawls = new PreCrawls(configuration);
  }

  public void preCrawl() throws IOException {
    _preCrawls.preCrawl(_crawlDir);
  }
  
  public void crawl(Integer topn, Integer depth) throws IOException {
    System.out.println("start crawl");

    System.out.println("end crawl");
  }

}
