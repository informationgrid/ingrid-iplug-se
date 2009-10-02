package de.ingrid.iplug.se.urlmaintenance.security;

import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController extends NavigationSelector {

  @RequestMapping(value = "/auth/login.html", method = RequestMethod.GET)
  public String login(Model model, HttpSession session) {
    Boolean secure = (Boolean) session.getAttribute("securityEnabled");
    model.addAttribute("securityEnabled", secure);
    return "login";
  }

  @RequestMapping(value = "/auth/loginFailure.html", method = RequestMethod.GET)
  public String loginFailure() {
    return "loginFailure";
  }

  @RequestMapping(value = "/auth/roleFailure.html", method = RequestMethod.GET)
  public String roleFailure() {
    return "roleFailure";
  }

  @RequestMapping(value = "/auth/logout.html", method = RequestMethod.GET)
  public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/index.html";
  }

}
