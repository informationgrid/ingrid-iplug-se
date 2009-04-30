package org.apache.nutch.admin.scheduling;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PatternPersistence {

  private Scheduler _scheduler;
  private final SchedulingRunnable _runnable;
  private File _workingDirectory;

  public PatternPersistence(SchedulingRunnable runnable) {
    _runnable = runnable;
    _scheduler = new Scheduler();
    _scheduler.start();
  }

  public void savePattern(String pattern) throws IOException {
    checkWorkingDirectory();
    ObjectOutputStream objectOutputStream = new ObjectOutputStream(
        new FileOutputStream(new File(_workingDirectory, "pattern.ser")));
    Pattern patternObject = new Pattern();
    patternObject.setPattern(pattern);
    objectOutputStream.writeObject(patternObject);
    objectOutputStream.close();

    _scheduler.schedule(pattern, _runnable);

  }

  public Pattern loadPattern() throws Exception {
    checkWorkingDirectory();
    ObjectInputStream objectInputStream = new ObjectInputStream(
        new FileInputStream(new File(_workingDirectory, "pattern.ser")));
    Pattern pattern = (Pattern) objectInputStream.readObject();
    return pattern;
  }

  public boolean existsPattern() {
    checkWorkingDirectory();
    return new File(_workingDirectory, "pattern.ser").exists();
  }

  public void deletePattern() {
    checkWorkingDirectory();
    new File(_workingDirectory, "pattern.ser").delete();
  }

  private void checkWorkingDirectory() {
    if (_workingDirectory == null || !_workingDirectory.exists()) {
      throw new RuntimeException("working directory does ot exists");
    }
  }

  public void setWorkingDirectory(File workingDirectory) throws Exception {
    _workingDirectory = workingDirectory;
    if (existsPattern()) {
      Pattern pattern = loadPattern();
      _scheduler.schedule(pattern.getPattern(), _runnable);
    }
  }
}
