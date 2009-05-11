package de.ingrid.iplug.se.urlmaintenance;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes("partnerProviderCommand")
@RequestMapping("/index.html")
public class UrlMaintenanceController extends NavigationSelector {

  @ModelAttribute("partners")
  public String[] referenceDataPartners() {
    return new String[] { "partner1", "partner2" };
  }

  @ModelAttribute("providers")
  public String[] referenceDataProviders() {
    return new String[] { "provider1", "provider2", "foo" };
  }

  @RequestMapping(method = RequestMethod.GET)
  public String urlMaintenance(Model model) {
    System.out.println("UrlMaintenanceController.urlMaintenance()");
    model.addAttribute("partnerProviderCommand", new PartnerProviderCommand());
    return "urlmaintenance";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String postUrlMaintenance(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    return "redirect:/welcomeEditUrls.html";
  }
}
