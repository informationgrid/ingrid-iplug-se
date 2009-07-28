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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
    CrawlPath[] crawlPathArray = listPaths(path, fileSystem);
    return crawlPathArray;
  }

  private CrawlPath[] listPaths(Path path, FileSystem fileSystem)
      throws IOException {
    FileStatus[] fileStatusArray = fileSystem.listStatus(path);
    CrawlPath[] crawlPathArray = new CrawlPath[fileStatusArray.length];
    int counter = 0;
    for (FileStatus fileStatus : fileStatusArray) {
      Path fileStatusPath = fileStatus.getPath();
      crawlPathArray[counter] = createCrawlPath(fileStatusPath, fileSystem);
      counter++;
    }
    return crawlPathArray;
  }

  private CrawlPath createCrawlPath(Path path, FileSystem fileSystem)
      throws IOException {
    long len = 0;
    if (fileSystem.exists(path)) {
      ContentSummary contentSummary = fileSystem.getContentSummary(path);
      len = contentSummary.getLength();
      len = (len / 1024) / 1024;
    }
    CrawlPath crawlPath = new CrawlPath();
    crawlPath.setPath(path);
    crawlPath.setSize(len);
    return crawlPath;
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

    PreCrawlRunnable runnable = new PreCrawlRunnable(crawlTool);
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    thread.start();
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/startCrawl.html", method = RequestMethod.GET)
  public String startCrawl(Model model,
      @RequestParam(value = "crawlFolder", required = true) String crawlFolder,
      HttpSession session) throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
        .getAttribute("nutchInstance");
    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();
    // look in the same path in hdfs
    Path crawlsPath = new Path(instanceFolder.getAbsolutePath(), "crawls");
    Path segmentsPath = new Path(crawlsPath, new Path(crawlFolder, "segments"));
    Configuration configuration = nutchInstance.getConfiguration();
    FileSystem fileSystem = FileSystem.get(configuration);
    CrawlPath[] listPaths = listPaths(segmentsPath, fileSystem);
    model.addAttribute("segments", listPaths);
    model.addAttribute("crawlFolder", crawlFolder);

    // db's
    CrawlPath[] dbPaths = new CrawlPath[4];
    dbPaths[0] = createCrawlPath(new Path(crawlsPath, new Path(crawlFolder,
        "crawldb")), fileSystem);
    dbPaths[1] = createCrawlPath(new Path(crawlsPath, new Path(crawlFolder,
        "bwdb")), fileSystem);
    dbPaths[2] = createCrawlPath(new Path(crawlsPath, new Path(crawlFolder,
        "metadatadb")), fileSystem);
    dbPaths[3] = createCrawlPath(new Path(crawlsPath, new Path(crawlFolder,
        "linkdb")), fileSystem);
    model.addAttribute("dbs", dbPaths);

    return "startCrawl";
  }

  @RequestMapping(value = "/startCrawl.html", method = RequestMethod.POST)
  public String postStartCrawl(
      @ModelAttribute("crawlCommand") CrawlCommand crawlCommand,
      HttpSession session) throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
        .getAttribute("nutchInstance");

    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();
    // look in the same path in hdfs
    Path path = new Path(instanceFolder.getAbsolutePath(), "crawls");
    Configuration configuration = nutchInstance.getConfiguration();
    Path crawlDir = new Path(path, crawlCommand.getCrawlFolder());
    CrawlTool crawlTool = new CrawlTool(configuration, crawlDir);

    Integer topn = crawlCommand.getTopn();
    Integer depth = crawlCommand.getDepth();
    Runnable runnable = new StartCrawlRunnable(crawlTool, topn, depth);
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    thread.start();
    return "redirect:/index.html";
  }
}
