package de.ingrid.admin.security;

import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.ingrid.admin.service.PlugDescriptionService;

@Controller
public class LoginController extends NavigationSelector {

    @ModelAttribute("securityEnabled")
    public Boolean injectAuthenticate(final PlugDescriptionService pds) {
        return pds.existsPlugDescription();
    }

    @RequestMapping(value = "/base/auth/login.html", method = RequestMethod.GET)
    public String login(final Model model, final HttpSession session) {
        return "/base/login";
    }

    @RequestMapping(value = "/base/auth/loginFailure.html", method = RequestMethod.GET)
    public String loginFailure() {
        return "/base/loginFailure";
    }

    @RequestMapping(value = "/base/auth/roleFailure.html", method = RequestMethod.GET)
    public String roleFailure() {
        return "/base/roleFailure";
    }

    @RequestMapping(value = "/base/auth/logout.html", method = RequestMethod.GET)
    public String logout(final HttpSession session) {
        session.invalidate();
        return "redirect:/base/welcome.html";
    }

}
