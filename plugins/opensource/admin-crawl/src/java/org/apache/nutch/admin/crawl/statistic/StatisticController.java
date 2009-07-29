package org.apache.nutch.admin.crawl.statistic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.nutch.admin.NutchInstance;
import org.apache.nutch.tools.HostStatistic.StatisticWritable;
import org.apache.nutch.tools.HostStatistic.StatisticWritableContainer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StatisticController {

  @RequestMapping(value = "/statistic.html")
  public String statistic(
      @RequestParam(value = "crawlFolder", required = true) String crawlFolder,
      @RequestParam(value = "segment", required = true) String segment,
      @RequestParam(value = "maxCount", required = true) Integer maxCount,
      HttpSession session, Model model) throws IOException {
    // TODO read statistic from segment
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
        .getAttribute("nutchInstance");
    // local folder for configuration files
    File instanceFolder = nutchInstance.getInstanceFolder();
    // look in the same path in hdfs
    Path crawlsPath = new Path(instanceFolder.getAbsolutePath(), "crawls");
    Path segmentsPath = new Path(crawlsPath, new Path(crawlFolder, "segments"));
    Path segmentPath = new Path(segmentsPath, segment);
    Configuration configuration = nutchInstance.getConfiguration();
    FileSystem fileSystem = FileSystem.get(configuration);
    Path hostStatistic = new Path(segmentPath, "statistic/host");
    Reader reader = new SequenceFile.Reader(fileSystem, hostStatistic,
        configuration);
    StatisticWritableContainer key = new StatisticWritableContainer();
    Text value = new Text();
    int tmpCount = 0;
    List<Statistic> statistics = new ArrayList<Statistic>();
    while (tmpCount < maxCount && reader.next(key, value)) {
      tmpCount++;
      StatisticWritable crawldbStatistic = key.getCrawldbStatistic();
      StatisticWritable fetchStatistic = key.getFetchStatistic();
      Statistic statistic = new Statistic(value.toString(), crawldbStatistic
          .getValue(), fetchStatistic.getValue());
      statistics.add(statistic);
    }
    reader.close();
    model.addAttribute("statistics", statistics);
    return "statistic";
  }
}
