package org.apache.nutch.admin.scheduling;

public class ClockCommand extends CrawlCommand {

  public static enum Period {
    AM, PM
  }

  private Integer _hour;

  private Integer _minute;

  private Period _period;

  public Integer getHour() {
    return _hour;
  }

  public void setHour(Integer hour) {
    _hour = hour;
  }

  public Integer getMinute() {
    return _minute;
  }

  public void setMinute(Integer minute) {
    _minute = minute;
  }

  public Period getPeriod() {
    return _period;
  }

  public void setPeriod(Period period) {
    _period = period;
  }

}
