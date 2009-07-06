package org.apache.nutch.admin.crawl;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.ContentSummary;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.apache.nutch.crawl.CrawlTool;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class CrawlController extends NavigationSelector {

  private DateFormat _format = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

  @ModelAttribute("crawlPaths")
  public CrawlPath[] referenceDataCrawlFolders(HttpSession session)
      throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
        .getAttribute("nutchInstance");

    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();
    // look in the same path in hdfs
    Path path = new Path(instanceFolder.getAbsolutePath(), "crawls");
    Configuration configuration = nutchInstance.getConfiguration();
    FileSystem fileSystem = FileSystem.get(configuration);
    FileStatus[] fileStatusArray = fileSystem.listStatus(path);
    CrawlPath[] crawlPathArray = new CrawlPath[fileStatusArray.length];
    int counter = 0;
    for (FileStatus fileStatus : fileStatusArray) {
      Path fileStatusPath = fileStatus.getPath();
      ContentSummary contentSummary = fileSystem
          .getContentSummary(fileStatusPath);
      long len = contentSummary.getLength();
      len = (len / 1024) / 1024;
      crawlPathArray[counter] = new CrawlPath();
      crawlPathArray[counter].setPath(fileStatusPath);
      crawlPathArray[counter].setSize(len);
      counter++;
    }
    return crawlPathArray;
  }

  @ModelAttribute("depths")
  public Integer[] referenceDataDepths() {
    return new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  }

  @ModelAttribute("crawlCommand")
  public CrawlCommand referenceDataCrawlCommand() {
    return new CrawlCommand();
  }

  @RequestMapping(value = "/index.html", method = RequestMethod.GET)
  public String crawl() {
    return "createCrawl";
  }

  @RequestMapping(value = "/createCrawl.html", method = RequestMethod.POST)
  public String createCrawl(HttpSession session) throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
        .getAttribute("nutchInstance");

    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();
    // look in the same path in hdfs
    Path path = new Path(instanceFolder.getAbsolutePath(), "crawls");
    Configuration configuration = nutchInstance.getConfiguration();
    FileSystem fileSystem = FileSystem.get(configuration);
    String folderName = "Crawl-" + _format.format(new Date());
    Path crawlDir = new Path(path, folderName);
    fileSystem.mkdirs(crawlDir);
    CrawlTool crawlTool = new CrawlTool(configuration, crawlDir);
    crawlTool.preCrawl();
    // TODO use correct params
    crawlTool.crawl(10, 10);
    return "redirect:/index.html";
  }

}
