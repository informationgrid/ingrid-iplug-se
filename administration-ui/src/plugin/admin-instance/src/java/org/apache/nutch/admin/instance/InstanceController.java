package org.apache.nutch.admin.instance;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/welcome.html")
public class InstanceController {

  public InstanceController() {
    System.out.println("InstanceController.InstanceController()");
  }
  @RequestMapping(method = RequestMethod.GET)
  public String welcome(Model model) {
    model.addAttribute("foo", "bar");
    return "welcome";
  }
  
}
