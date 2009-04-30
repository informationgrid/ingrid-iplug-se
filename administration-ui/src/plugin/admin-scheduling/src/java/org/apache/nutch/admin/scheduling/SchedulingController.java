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

  @Autowired
  public SchedulingController(PatternPersistence patternPersistence) {
    _patternPersistence = patternPersistence;
  }

  @ModelAttribute("savedPattern")
  public String pattern() throws Exception {
    return _patternPersistence.existsPattern() ? _patternPersistence
        .loadPattern().getPattern() : "";
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
      @ModelAttribute("clockCommand") ClockCommand schedulingCommand)
      throws IOException {
    Integer hour = schedulingCommand.getHour();
    Integer minute = schedulingCommand.getMinute();
    Period period = schedulingCommand.getPeriod();
    hour = period == Period.PM ? hour + 12 : hour;
    String pattern = minute + " " + hour + " " + "* * 0-6";
    _patternPersistence.savePattern(pattern);
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
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/advanced.html", method = RequestMethod.POST)
  public String postAdvanced(
      @ModelAttribute("advancedCommand") AdvancedCommand advancedCommand)
      throws IOException {
    String pattern = advancedCommand.getPattern();
    _patternPersistence.savePattern(pattern);
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/delete.html", method = RequestMethod.POST)
  public String delete() {
    _patternPersistence.deletePattern();
    return "redirect:/index.html";
  }

}
