package de.ingrid.iplug.se.urlmaintenance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("partnerProviderCommand")
@RequestMapping("/welcomeEditUrls.html")
public class WelcomeEditUrlsController {

  @RequestMapping(method = RequestMethod.GET)
  public String index(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    System.out.println("WelcomeEditUrlsController.index() "
        + partnerProviderCommand.getProvider());
    return "welcomeEditUrls";
  }
}
