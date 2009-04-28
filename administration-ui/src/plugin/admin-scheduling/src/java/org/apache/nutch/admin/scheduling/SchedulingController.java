package org.apache.nutch.admin.scheduling;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.apache.nutch.admin.scheduling.ClockCommand.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class SchedulingController extends NavigationSelector {

  private Scheduler _scheduler;
  private final PatternPersistence _patternPersistence;

  @Autowired
  public SchedulingController(PatternPersistence patternPersistence) {
    _patternPersistence = patternPersistence;
    _scheduler = new Scheduler();
  }

  @ModelAttribute("savedPattern")
  public String pattern(HttpSession session) throws Exception {
    NutchInstance nutchInstance = (NutchInstance) session.getServletContext()
        .getAttribute("nutchInstance");
    File instanceFolder = nutchInstance.getInstanceFolder();
    return _patternPersistence.existsPattern(instanceFolder) ? _patternPersistence
        .loadPattern(instanceFolder).getPattern()
        : "";
  }

  @ModelAttribute("periods")
  public Period[] periods() {
    return Period.values();
  }

  @ModelAttribute("hours")
  public Integer[] hours() {
    return new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
  }

  @ModelAttribute("minutes")
  public Integer[] minutes() {
    return new Integer[] { 0, 15, 30, 45 };
  }

  @ModelAttribute("clockCommand")
  public ClockCommand referenceDataPeriod() {
    return new ClockCommand();
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
      @ModelAttribute("clockCommand") ClockCommand schedulingCommand,
      HttpSession session) throws IOException {
    Integer hour = schedulingCommand.getHour();
    Integer minute = schedulingCommand.getMinute();
    Period period = schedulingCommand.getPeriod();
    hour = period == Period.PM ? hour + 12 : hour;
    String pattern = minute + " " + hour + " " + "* * 1-7";
    NutchInstance nutchInstance = (NutchInstance) session.getServletContext()
        .getAttribute("nutchInstance");
    File instanceFolder = nutchInstance.getInstanceFolder();
    _patternPersistence.savePattern(instanceFolder, pattern);
    
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/advanced.html", method = RequestMethod.POST)
  public String postAdvanced(
      @ModelAttribute("advancedCommand") AdvancedCommand advancedCommand,
      HttpSession session) throws IOException {
    String pattern = advancedCommand.getPattern();
    NutchInstance nutchInstance = (NutchInstance) session.getServletContext()
        .getAttribute("nutchInstance");
    File instanceFolder = nutchInstance.getInstanceFolder();
    _patternPersistence.savePattern(instanceFolder, pattern);
    return "redirect:/index.html";
  }

  @RequestMapping(value = "/delete.html", method = RequestMethod.POST)
  public String delete(HttpSession session) {
    NutchInstance nutchInstance = (NutchInstance) session.getServletContext()
        .getAttribute("nutchInstance");
    File instanceFolder = nutchInstance.getInstanceFolder();
    _patternPersistence.deletePattern(instanceFolder);
    return "redirect:/index.html";
  }

}
