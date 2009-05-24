package org.apache.nutch.admin.scheduling;

import java.io.IOException;

public class CrawlDataPersistence extends Persistence<CrawlData> {

  public void saveCrawlData(Integer depth, Integer topn) throws IOException {
    CrawlData crawlData = new CrawlData();
    crawlData.setDepth(depth);
    crawlData.setTopn(topn);
    crawlData.setWorkingDirectory(_workingDirectory);
    makePersistent(crawlData);
  }

  public CrawlData loadCrawlData() throws IOException, ClassNotFoundException {
    return load(CrawlData.class);
  }

  public boolean existsCrawlData() {
    return exists(CrawlData.class);
  }

  public void deleteCrawlData() throws IOException {
    makeTransient(CrawlData.class);
  }

}
