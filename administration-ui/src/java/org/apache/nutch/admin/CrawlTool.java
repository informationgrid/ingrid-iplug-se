package org.apache.nutch.admin;

import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class CrawlTool {

  public void crawl() {
    System.out.println("CrawlTool.crawl() " + new Date());
  }
}
