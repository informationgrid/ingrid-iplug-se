/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import org.apache.hadoop.fs.PathFilter;
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.apache.nutch.admin.searcher.SearcherFactory;
import org.apache.nutch.crawl.CrawlTool;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CrawlController extends NavigationSelector {

  private DateFormat _format = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");

  @ModelAttribute("depths")
  public Integer[] referenceDataDepths() {
    return new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
  }

  @ModelAttribute("crawlCommand")
  public CrawlCommand referenceDataCrawlCommand() {
    return new CrawlCommand();
  }

  @RequestMapping(value = "/index.html", method = RequestMethod.GET)
  public String crawl(Model model, HttpSession session) throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
            .getAttribute("nutchInstance");

    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();
    // look in the same path in hdfs
    Path path = new Path(instanceFolder.getAbsolutePath(), "crawls");
    Configuration configuration = nutchInstance.getConfiguration();
    FileSystem fileSystem = FileSystem.get(configuration);
    CrawlPath[] crawlPathArray = listPaths(path, fileSystem, new PathFilter() {
      @Override
      public boolean accept(Path path) {
        return path.getName().startsWith("Crawl");
      }
    });
    model.addAttribute("crawlPaths", crawlPathArray);
    for (CrawlPath crawlPath : crawlPathArray) {
      if (crawlPath.isRunning()) {
        model.addAttribute("runningCrawl", new Object());
        break;
      }
    }
    model.addAttribute("showDialog", false);
    return "listCrawls";
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

    return "redirect:/index.html";
  }

  @RequestMapping(value = "/crawlDetails.html", method = RequestMethod.GET)
  public String startCrawl(
          Model model,
          @RequestParam(value = "crawlFolder", required = true) String crawlFolder,
          HttpSession session) throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
            .getAttribute("nutchInstance");
    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();
    // look in the same path in hdfs
    Path crawlsPath = new Path(instanceFolder.getAbsolutePath(), "crawls");
    Path crawlPath = new Path(crawlsPath, new Path(crawlFolder));
    Path segmentsPath = new Path(crawlPath, "segments");
    Configuration configuration = nutchInstance.getConfiguration();
    FileSystem fileSystem = FileSystem.get(configuration);
    model.addAttribute("crawlFolder", crawlFolder);

    CrawlPath[] segments = listPaths(segmentsPath, fileSystem,
            new PathFilter() {
              @Override
              public boolean accept(Path path) {
                return path.getName().startsWith("20");
              }
            });
    model.addAttribute("segments", segments);

    CrawlPath[] dbs = listPaths(crawlPath, fileSystem, new PathFilter() {
      @Override
      public boolean accept(Path path) {
        return path.getName().endsWith("db");
      }
    });
    model.addAttribute("dbs", dbs);

    CrawlPath[] index = listPaths(crawlPath, fileSystem, new PathFilter() {
      @Override
      public boolean accept(Path path) {
        return path.getName().equals("index");
      }
    });
    model.addAttribute("indexes", index);

    return "crawlDetails";
  }

  @RequestMapping(value = "/startCrawl.html", method = RequestMethod.POST)
  public String postStartCrawl(
          @ModelAttribute("crawlCommand") CrawlCommand crawlCommand,
          BindingResult errors, Model model, HttpSession session)
          throws IOException {
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

    String agentName = configuration.get("http.agent.name");
    if (agentName == null || "".equals(agentName.trim())) {
      errors.rejectValue("globalRejectAttribute", "configure.http.agent.name");
      model.addAttribute("showDialog", true);
      return "listCrawls";
    }
    Integer topn = crawlCommand.getTopn();
    Integer depth = crawlCommand.getDepth();
    Runnable runnable = new StartCrawlRunnable(crawlTool, topn, depth);
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    thread.start();
    return "redirect:/index.html";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/addToSearch.html")
  public String addToSearch(
          HttpSession session,
          @RequestParam(required = true, value = "crawlFolder") final String crawlFolder)
          throws IOException {

    switchSearch(session, crawlFolder, true);

    return "redirect:/index.html";
  }

  @RequestMapping(method = RequestMethod.POST, value = "/removeFromSearch.html")
  public String removeFromSearch(
          HttpSession session,
          @RequestParam(required = true, value = "crawlFolder") final String crawlFolder)
          throws IOException {
    switchSearch(session, crawlFolder, false);
    return "redirect:/index.html";
  }

  private void switchSearch(HttpSession session, final String crawlFolder,
          boolean create) throws IOException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
            .getAttribute("nutchInstance");

    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();

    FileSystem fileSystem = FileSystem.get(nutchInstance.getConfiguration());
    Path searchDoneFile = new Path(instanceFolder.getAbsolutePath(), "crawls"
            + File.separator + crawlFolder + File.separator + "search.done");
    if (create) {
      fileSystem.createNewFile(searchDoneFile);
    } else {
      fileSystem.delete(searchDoneFile, false);
    }

    SearcherFactory factory = SearcherFactory.getInstance(nutchInstance
            .getConfiguration());
    factory.reload();
  }

  private CrawlPath[] listPaths(Path path, FileSystem fileSystem,
          PathFilter filter) throws IOException {
    FileStatus[] fileStatusArray = fileSystem.listStatus(path, filter);
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
    crawlPath.setSearchable(fileSystem.exists(new Path(path, "search.done")));
    crawlPath.setRunning(fileSystem.exists(new Path(path, "crawl.running")));
    return crawlPath;
  }
}
