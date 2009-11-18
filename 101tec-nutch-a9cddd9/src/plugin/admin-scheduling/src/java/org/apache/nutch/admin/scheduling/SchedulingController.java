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
package org.apache.nutch.admin.scheduling;

import java.io.IOException;
import java.util.List;

import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.scheduling.ClockCommand.Period;
import org.apache.nutch.admin.scheduling.WeeklyCommand.Day;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SchedulingController extends NavigationSelector {

  private final PatternPersistence _patternPersistence;
  private final CrawlDataPersistence _crawlDataPersistence;

  @Autowired
  public SchedulingController(PatternPersistence patternPersistence,
      CrawlDataPersistence crawlDataPersistence) {
    _patternPersistence = patternPersistence;
    _crawlDataPersistence = crawlDataPersistence;
  }

  @ModelAttribute("savedPattern")
  public String pattern() throws Exception {
    return _patternPersistence.existsPattern() ? _patternPersistence
        .loadPattern().getPattern() : "";
  }

  @ModelAttribute("savedCrawlData")
  public String crawlData() throws Exception {
    return _crawlDataPersistence.existsCrawlData() ? _crawlDataPersistence
        .loadCrawlData().toString() : "";
  }

  @ModelAttribute("periods")
  public Period[] periods() {
    return Period.values();
  }

  @ModelAttribute("minutes")
  public Integer[] minutes() {
    return new Integer[] { 0, 15, 30, 45 };
  }

  @ModelAttribute("hours")
  public Integer[] hours() {
    return new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
  }

  @ModelAttribute("days")
  public Day[] days() {
    return Day.values();
  }

  @ModelAttribute("month")
  public Integer[] month() {
    Integer[] month = new Integer[31];
    for (int i = 0; i < 31; i++) {
      month[i] = i;
    }
    return month;
  }

  @ModelAttribute("depths")
  public Integer[] depth() {
    Integer[] month = new Integer[10];
    for (int i = 0; i < month.length; i++) {
      month[i] = i;
    }
    return month;
  }

  @ModelAttribute("clockCommand")
  public ClockCommand referenceDataPeriod() {
    return new ClockCommand();
  }

  @ModelAttribute("weeklyCommand")
  public WeeklyCommand referenceDataWeek() {
    return new WeeklyCommand();
  }

  @ModelAttribute("monthlyCommand")
  public MonthlyCommand referenceDataMonth() {
    return new MonthlyCommand();
  }

  @ModelAttribute("advancedCommand")
  public AdvancedCommand referenceDataAdvancedCommand() {
    return new AdvancedCommand();
  }

  @RequestMapping(value = "/index.html", method = RequestMethod.GET)
  public String scheduling(Model model) {
    return "scheduling";
  }

  @RequestMapping(value = "/daily.html", method = RequestMethod.POST)
  public String postDaily(
      @ModelAttribute("clockCommand") ClockCommand clockCommand)
      throws IOException {

    // persist scheduling
    Integer hour = clockCommand.getHour();
    Integer minute = clockCommand.getMinute();
    Period period = clockCommand.getPeriod();
    hour = period == Period.PM ? hour + 12 : hour;
    String pattern = minute + " " + hour + " " + "* * 0-6";
    _patternPersistence.savePattern(pattern);

    // persist crawldata
    Integer depth = clockCommand.getDepth();
    Integer topn = clockCommand.getTopn();
    _crawlDataPersistence.saveCrawlData(depth, topn);
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/weekly.html", method = RequestMethod.POST)
  public String postWeekly(
      @ModelAttribute("weeklyCommand") WeeklyCommand weeklyCommand)
      throws IOException {
    Integer hour = weeklyCommand.getHour();
    Integer minute = weeklyCommand.getMinute();
    Period period = weeklyCommand.getPeriod();
    hour = period == Period.PM ? hour + 12 : hour;
    List<Day> days = weeklyCommand.getDays();
    String dayPattern = "*";
    int counter = 0;
    for (Day day : days) {
      if (counter == 0) {
        dayPattern = "";
      } else if (counter > 0) {
        dayPattern += ",";
      }
      dayPattern += day.ordinal();
      counter++;
    }
    String pattern = minute + " " + hour + " " + "* * " + dayPattern;
    _patternPersistence.savePattern(pattern);

    // persist crawldata
    Integer depth = weeklyCommand.getDepth();
    Integer topn = weeklyCommand.getTopn();
    _crawlDataPersistence.saveCrawlData(depth, topn);
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/monthly.html", method = RequestMethod.POST)
  public String postMonthly(
      @ModelAttribute("monthlyCommand") MonthlyCommand monthlyCommand)
      throws IOException {
    Integer hour = monthlyCommand.getHour();
    Integer minute = monthlyCommand.getMinute();
    Period period = monthlyCommand.getPeriod();
    hour = period == Period.PM ? hour + 12 : hour;
    List<Integer> daysOfMonth = monthlyCommand.getDaysOfMonth();
    String dayPattern = "*";
    int counter = 0;
    for (Integer dayOfMonth : daysOfMonth) {
      if (counter == 0) {
        dayPattern = "";
      } else if (counter > 0) {
        dayPattern += ",";
      }
      dayPattern += dayOfMonth;
      counter++;
    }
    String pattern = minute + " " + hour + " " + dayPattern + " * *";
    _patternPersistence.savePattern(pattern);

    // persist crawldata
    Integer depth = monthlyCommand.getDepth();
    Integer topn = monthlyCommand.getTopn();
    _crawlDataPersistence.saveCrawlData(depth, topn);
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/advanced.html", method = RequestMethod.POST)
  public String postAdvanced(
      @ModelAttribute("advancedCommand") AdvancedCommand advancedCommand)
      throws IOException {
    String pattern = advancedCommand.getPattern();
    _patternPersistence.savePattern(pattern);

    // persist crawldata
    Integer depth = advancedCommand.getDepth();
    Integer topn = advancedCommand.getTopn();
    _crawlDataPersistence.saveCrawlData(depth, topn);
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/delete.html", method = RequestMethod.POST)
  public String delete() throws IOException {
    _patternPersistence.deletePattern();
    _crawlDataPersistence.deleteCrawlData();
    return "redirect:/index.html";
  }

}
