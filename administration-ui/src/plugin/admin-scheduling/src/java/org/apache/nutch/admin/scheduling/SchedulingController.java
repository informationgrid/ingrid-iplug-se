package org.apache.nutch.admin.scheduling;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/index.html")
public class SchedulingController extends NavigationSelector {

  @RequestMapping(method = RequestMethod.GET)
  public String scheduling(Model model) {
    return "scheduling";
  }
}
