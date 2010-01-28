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
