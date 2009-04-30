package org.apache.nutch.admin.scheduling;

import java.util.ArrayList;
import java.util.List;

public class WeeklyCommand extends ClockCommand {

  public static enum Day {
    SUN, MON, TUE, WED, THU, FRI, SAT
  }

  private List<Day> _days = new ArrayList<Day>();

  public List<Day> getDays() {
    return _days;
  }

  public void setDays(List<Day> days) {
    _days = days;
  }

}
