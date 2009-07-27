package org.apache.nutch.admin.system;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

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
      @RequestParam("lines") Integer lines, HttpServletResponse response, final Model model)
      throws IOException {

    StringBuilder stringBuilder = new StringBuilder();
    try {
        List<String> list = SystemTool.tailLogFile(new File(logFileName), lines);
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
