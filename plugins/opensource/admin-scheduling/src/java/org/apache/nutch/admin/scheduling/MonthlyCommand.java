package org.apache.nutch.admin.scheduling;

import java.util.ArrayList;
import java.util.List;

public class MonthlyCommand extends ClockCommand {

  private List<Integer> _daysOfMonth = new ArrayList<Integer>();

  public List<Integer> getDaysOfMonth() {
    return _daysOfMonth;
  }

  public void setDaysOfMonth(List<Integer> daysOfMonth) {
    _daysOfMonth = daysOfMonth;
  }

}
