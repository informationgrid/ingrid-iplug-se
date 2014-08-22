package de.ingrid.iplug.se.webapp.controller.scheduler;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import de.ingrid.iplug.se.webapp.controller.scheduler.ClockCommand.Period;
import de.ingrid.iplug.se.webapp.controller.scheduler.WeeklyCommand.Day;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class SchedulingController extends AbstractController {
    
    private final PatternPersistence _patternPersistence;
    private final CrawlDataPersistence _crawlDataPersistence;

    @Autowired
    public SchedulingController(PatternPersistence patternPersistence,
        CrawlDataPersistence crawlDataPersistence) {
      _patternPersistence = patternPersistence;
      _crawlDataPersistence = crawlDataPersistence;
    }

    @RequestMapping(value = { "/iplug-pages/instanceScheduling.html" }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @RequestParam("instance") String name) {

        String dir = SEIPlug.conf.getInstancesDir();
        File instanceFolder = new File( dir, name );
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

        modelMap.put( "instance", getInstanceData( name ) );

        return AdminViews.SE_INSTANCE_SCHEDULER;
    }

    private Instance getInstanceData(String name) {
        Instance instance = new Instance();
        instance.setName( name );

        return instance;
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

    @ModelAttribute("savedPattern")
    public String pattern(@RequestParam("instance") String name) throws Exception {
        return _patternPersistence.existsPattern(name) ? _patternPersistence.loadPattern(name).getPattern() : "";
    }

    @ModelAttribute("savedCrawlData")
    public String crawlData(@RequestParam("instance") String name) throws Exception {
        return _crawlDataPersistence.existsCrawlData(name) ? _crawlDataPersistence.loadCrawlData(name).toString() : "";
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
            month[i] = i + 1;
        }
        return month;
    }

    @ModelAttribute("depths")
    public Integer[] depth() {
        Integer[] month = new Integer[11];
        for (int i = 0; i < month.length; i++) {
            month[i] = i;
        }
        return month;
    }

    @RequestMapping(value = "/iplug-pages/daily.html", method = RequestMethod.POST)
    public String postDaily(@ModelAttribute("clockCommand") ClockCommand clockCommand, Errors errors, @RequestParam("instance") String name, final ModelMap map)
            throws IOException {

        map.put( "instance", getInstanceData( name ) );
        // validate and return to page on error
        validate( errors, clockCommand );
        if (errors.hasErrors()) {
            return AdminViews.SE_INSTANCE_SCHEDULER;
        }

        // persist scheduling
        Integer hour = clockCommand.getHour();
        Integer minute = clockCommand.getMinute();
        Period period = clockCommand.getPeriod();
        hour = period == Period.PM ? hour + 12 : hour;
        String pattern = minute + " " + hour + " " + "* * 0-6";
        _patternPersistence.savePattern(pattern, name);

        // persist crawldata
        Integer depth = clockCommand.getDepth();
        Integer topn = clockCommand.getTopn();
        _crawlDataPersistence.saveCrawlData(depth, topn, name);
        return "redirect:" + AdminViews.SE_INSTANCE_SCHEDULER + ".html?instance=" + name;
    }

    @RequestMapping(value = "/iplug-pages/weekly.html", method = RequestMethod.POST)
    public String postWeekly(@ModelAttribute("weeklyCommand") WeeklyCommand weeklyCommand, Errors errors, @RequestParam("instance") String name, final ModelMap map)
            throws IOException {

        String tabId = "#tab2";
        map.put( "instance", getInstanceData( name ) );
        map.put( "selectedTab", tabId );
        // validate and return to page on error
        validate( errors, weeklyCommand );
        if (errors.hasErrors()) {
            return AdminViews.SE_INSTANCE_SCHEDULER;
        }

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
        _patternPersistence.savePattern(pattern, name);

        // persist crawldata
        Integer depth = weeklyCommand.getDepth();
        Integer topn = weeklyCommand.getTopn();
        _crawlDataPersistence.saveCrawlData(depth, topn, name);
        return "redirect:" + AdminViews.SE_INSTANCE_SCHEDULER + ".html" + tabId + "?instance=" + name;
    }

    @RequestMapping(value = "/iplug-pages/monthly.html", method = RequestMethod.POST)
    public String postMonthly(@ModelAttribute("monthlyCommand") MonthlyCommand monthlyCommand, Errors errors, @RequestParam("instance") String name, final ModelMap map)
            throws IOException {

        String tabId = "#tab3";
        map.put( "instance", getInstanceData( name ) );
        map.put( "selectedTab", tabId );
        
        // validate and return to page on error
        validate( errors, monthlyCommand );
        if (errors.hasErrors()) {
            return AdminViews.SE_INSTANCE_SCHEDULER;
        }
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
        _patternPersistence.savePattern(pattern, name);

        // persist crawldata
        Integer depth = monthlyCommand.getDepth();
        Integer topn = monthlyCommand.getTopn();
        _crawlDataPersistence.saveCrawlData(depth, topn, name);
        return "redirect:" + AdminViews.SE_INSTANCE_SCHEDULER + ".html?instance=" + name + tabId;
    }

    @RequestMapping(value = "/iplug-pages/advanced.html", method = RequestMethod.POST)
    public String postAdvanced(@ModelAttribute("advancedCommand") AdvancedCommand advancedCommand, Errors errors, @RequestParam("instance") String name, final ModelMap map)
            throws IOException {
        String tabId = "#tab4";
        map.put( "instance", getInstanceData( name ) );
        map.put( "selectedTab", tabId );
        
        // validate and return to page on error
        validate( errors, advancedCommand );
        if (errors.hasErrors()) {
            return AdminViews.SE_INSTANCE_SCHEDULER;
        }
        String pattern = advancedCommand.getPattern();
        _patternPersistence.savePattern(pattern, name);

        // persist crawldata
        Integer depth = advancedCommand.getDepth();
        Integer topn = advancedCommand.getTopn();
        _crawlDataPersistence.saveCrawlData(depth, topn, name);
        return "redirect:" + AdminViews.SE_INSTANCE_SCHEDULER + ".html" + tabId + "?instance=" + name;
    }

    @RequestMapping(value = "/iplug-pages/delete.html", method = RequestMethod.POST)
    public String delete(@RequestParam("instance") String name) throws IOException {
        _patternPersistence.deletePattern(name);
        _crawlDataPersistence.deleteCrawlData(name);
        return "redirect:" + AdminViews.SE_INSTANCE_SCHEDULER + ".html?instance=" + name + "&tab=";
    }

    private void validate(Errors errors, CrawlCommand clockCommand) {
        if (clockCommand.getTopn() == null)
            errors.rejectValue( "topn", "scheduling.error.empty.topn" );
    }

}
