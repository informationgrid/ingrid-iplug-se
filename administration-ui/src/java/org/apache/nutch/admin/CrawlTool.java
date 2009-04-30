package org.apache.nutch.admin;

import java.io.File;

public class CrawlTool {

  public CrawlTool() {
    System.out.println("CrawlTool.CrawlTool()");
  }

  public void crawl(File workingDirectory, Integer topn, Integer depth) {
    System.out.println("start crawl");
    try {
      Thread.sleep(1000 * 60 * 3);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("end crawl");
  }
}
