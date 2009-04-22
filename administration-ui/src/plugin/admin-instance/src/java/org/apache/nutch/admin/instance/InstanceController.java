package org.apache.nutch.admin.instance;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.ConfigurationUtil;
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
  
  @RequestMapping(method = RequestMethod.POST)
  public String createIstance(HttpSession httpSession) throws IOException {
    ServletContext servletContext = httpSession.getServletContext();
    ConfigurationUtil configurationUtil = (ConfigurationUtil) servletContext
        .getAttribute("configurationUtil");
    

    System.out.println(servletContext.getAttribute("foo"));
    return "redirect:welcome.html";
  }

}
