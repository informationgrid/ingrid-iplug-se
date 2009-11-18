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
