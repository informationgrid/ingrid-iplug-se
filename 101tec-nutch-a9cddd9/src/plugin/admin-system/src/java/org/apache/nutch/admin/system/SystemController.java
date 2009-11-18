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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.system.SystemTool.SystemInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SystemController extends NavigationSelector {

  private static final Log LOG = LogFactory.getLog(SystemController.class);

  @ModelAttribute("systemInfo")
  public SystemInfo referenceDataSystemCommand() {
    SystemInfo systemInfo = SystemTool.getSystemInfo();
    return systemInfo;
  }

  @RequestMapping(value = "/index.html", method = RequestMethod.GET)
  public String system() {
    return "system";
  }

  @RequestMapping(value = "/log.html", method = RequestMethod.GET)
  public String log(@RequestParam("file") String logFileName,
          @RequestParam("lines") Integer lines, HttpServletResponse response,
          final Model model) throws IOException {

    StringBuilder stringBuilder = new StringBuilder();
    try {

      File logfile = new File(logFileName);
      if (!logfile.exists()) {
        // try another folder
        logfile = new File("logs", logFileName);
      }
      // LOG.debug("tail logfile: " + logfile.getAbsolutePath());
      List<String> list = SystemTool.tailLogFile(logfile, lines);
      for (String currentLine : list) {
        stringBuilder.append(currentLine);
        stringBuilder.append("\n");
      }
    } catch (RuntimeException e) {
      stringBuilder.append(e.getMessage());
    }
    model.addAttribute("logText", stringBuilder.toString());
    return "log";
  }

}
