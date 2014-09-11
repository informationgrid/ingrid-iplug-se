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
package de.ingrid.iplug.se.webapp.controller.instance.scheduler;

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