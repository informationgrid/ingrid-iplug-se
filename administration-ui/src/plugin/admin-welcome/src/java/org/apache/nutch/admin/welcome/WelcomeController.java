package org.apache.nutch.admin.welcome;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/index.html")
public class WelcomeController extends NavigationSelector {

  @RequestMapping(method = RequestMethod.GET)
  public String welcome(Model model) {
    return "welcome";
  }
}
