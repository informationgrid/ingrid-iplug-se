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
package org.apache.nutch.admin.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemTool {

  private static final Logger LOG = Logger
      .getLogger(SystemTool.class.getName());

  public static class SystemInfo {

    private long _usedMemory;

    private long _maxMemory;

    private float _usedMemoryInPercent;

    private int _openFiles;

    public long getUsedMemory() {
      return _usedMemory;
    }

    public void setUsedMemory(long usedMemory) {
      _usedMemory = usedMemory;
    }

    public long getMaxMemory() {
      return _maxMemory;
    }

    public void setMaxMemory(long maxMemory) {
      _maxMemory = maxMemory;
    }

    public float getUsedMemoryInPercent() {
      return _usedMemoryInPercent;
    }

    public void setUsedMemoryInPercent(float usedMemoryInPercent) {
      _usedMemoryInPercent = usedMemoryInPercent;
    }

    public int getOpenFiles() {
      return _openFiles;
    }

    public void setOpenFiles(int openFiles) {
      _openFiles = openFiles;
    }

  }

  public static SystemInfo getSystemInfo() {
    Runtime runtime = Runtime.getRuntime();
    long freeMemory = runtime.freeMemory();
    long maxMemory = runtime.maxMemory();
    long reservedMemory = runtime.totalMemory();
    long used = reservedMemory - freeMemory;
    float percent = 100 * used / maxMemory;
    int lineCount = 0;
    try {
      String property = System.getProperty("pid");
      if (property != null) {
        Integer integer = new Integer(property);
        String[] cmd = { "lsof", "-p", "" + integer };
        Process proccess = Runtime.getRuntime().exec(cmd);
        BufferedReader bufferedReader = new BufferedReader(
            new InputStreamReader(proccess.getInputStream()));
        while ((bufferedReader.readLine()) != null) {
          lineCount++;
        }
        bufferedReader.close();
      }
    } catch (Exception e) {
      LOG.warning("can not parse process id: " + e.getMessage());
    }
    SystemInfo systemInfo = new SystemInfo();
    systemInfo.setMaxMemory((maxMemory / (1024 * 1024)));
    systemInfo.setUsedMemory((used / (1024 * 1024)));
    systemInfo.setUsedMemoryInPercent(percent);
    systemInfo.setOpenFiles(lineCount);
    return systemInfo;
  }

  public static List<String> tailLogFile(File logFile, int lines) {
    List<String> list = new ArrayList<String>();
    try {
      String[] cmd = { "tail", "-n", "" + lines, logFile.getAbsolutePath() };
      Process proccess = Runtime.getRuntime().exec(cmd);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
          proccess.getInputStream()));
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        list.add(line);
      }
      bufferedReader.close();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "can not parse log file: " + logFile, e);
    }

    return list;
  }
}
