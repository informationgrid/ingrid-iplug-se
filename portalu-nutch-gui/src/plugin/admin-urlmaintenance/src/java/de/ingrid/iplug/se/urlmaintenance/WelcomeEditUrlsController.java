package de.ingrid.iplug.se.urlmaintenance;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("partnerProviderCommand")
@RequestMapping("/welcomeEditUrls.html")
public class WelcomeEditUrlsController extends NavigationSelector {

  @RequestMapping(method = RequestMethod.GET)
  public String index(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    return "welcomeEditUrls";
  }
}
