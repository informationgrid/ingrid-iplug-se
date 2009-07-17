package org.apache.nutch.admin.crawl.statistic;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StatisticController {

  @ModelAttribute("statistics")
  public List<Statistic> injectStatistics() {
    List<Statistic> list = new ArrayList<Statistic>();
    list.add(new Statistic("overall", 123192, 100100));
    for (int i = 21; i > 0; i--) {
      list.add(new Statistic("www." + i + ".com", i * 10, i * 5));
    }
    return list;
  }

  @RequestMapping(value = "/statistic.html")
  public String statistic(
      @RequestParam(value = "segment", required = true) String segment) {
    // TODO read statistic from segment
    return "statistic";
  }
}
