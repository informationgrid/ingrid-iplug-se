package org.apache.nutch.admin.scheduling;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.IOException;

public class PatternPersistence extends Persistence<Pattern> {

  private Scheduler _scheduler;
  private final SchedulingRunnable _runnable;
  private Object _scheduleId;

  public PatternPersistence(SchedulingRunnable runnable) {
    _runnable = runnable;
    _scheduler = new Scheduler();
    _scheduler.start();
  }

  public void savePattern(String pattern) throws IOException {
    Pattern patternObject = new Pattern();
    patternObject.setPattern(pattern);
    makePersistent(patternObject);
    if (_scheduleId == null) {
      _scheduleId = _scheduler.schedule(pattern, _runnable);
    } else {
      _scheduler.reschedule(_scheduleId, pattern);
    }

  }

  public Pattern loadPattern() throws Exception {
    return load(Pattern.class);
  }

  public boolean existsPattern() {
    return exists(Pattern.class);
  }

  public void deletePattern() throws IOException {
    makeTransient(Pattern.class);
    _scheduler.deschedule(_scheduleId);
    _scheduleId = null;
  }

  public void setWorkingDirectory(File workingDirectory) throws Exception {
    super.setWorkingDirectory(workingDirectory);
    if (existsPattern()) {
      Pattern pattern = loadPattern();
      _scheduleId = _scheduler.schedule(pattern.getPattern(), _runnable);
    }
  }
}
