package org.apache.nutch.admin.system;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.system.SystemTool.SystemInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SystemController extends NavigationSelector {

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
  public void log(@RequestParam("file") String logFile,
      @RequestParam("lines") Integer lines, HttpServletResponse response)
      throws IOException {
    List<String> list = SystemTool.tailLogFile(new File(logFile), lines);
    for (String line : list) {
      response.getWriter().println(line);
    }
  }
}
