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
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.apache.nutch.tools.HostStatistic.StatisticWritable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StatisticController extends NavigationSelector {

  @RequestMapping(value = "/statistic.html")
  public String statistic(
      @RequestParam(value = "crawlFolder", required = true) String crawlFolder,
      @RequestParam(value = "segment", required = true) String segment,
      @RequestParam(value = "maxCount", required = true) Integer maxCount,
      HttpSession session, Model model) throws IOException {
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

    // crawldb
    List<Statistic> crawldbStatistics = satistic(maxCount, configuration,
        fileSystem, new Path(segmentPath, "statistic/host/crawldb"));
    model.addAttribute("crawldbStatistic", crawldbStatistics);

    // shard
    List<Statistic> shardStatistics = satistic(maxCount, configuration,
        fileSystem, new Path(segmentPath, "statistic/host/shard"));
    model.addAttribute("shardStatistic", shardStatistics);

    for (Statistic crawldbStatistic : crawldbStatistics) {
      for (Statistic shardStatistic : shardStatistics) {
        if (crawldbStatistic.getHost().equals(shardStatistic.getHost())) {
          Long count1 = crawldbStatistic.getFetchSuccessCount();
          Long count2 = shardStatistic.getFetchSuccessCount();
          crawldbStatistic.setFetchSuccessCount(count1 + count2);
        }
      }
    }

    return "statistic";
  }

  private List<Statistic> satistic(Integer maxCount,
      Configuration configuration, FileSystem fileSystem, Path in)
      throws IOException {
    Reader reader = new SequenceFile.Reader(fileSystem, in, configuration);
    StatisticWritable key = new StatisticWritable();
    Text value = new Text();
    int tmpCount = 0;
    List<Statistic> statistics = new ArrayList<Statistic>();
    while (tmpCount < maxCount && reader.next(key, value)) {
      tmpCount++;
      Statistic statistic = new Statistic(value.toString(), key
          .getOverallCount(), key.getFetchSuccessCount());
      statistics.add(statistic);
    }
    reader.close();
    return statistics;
  }
}
