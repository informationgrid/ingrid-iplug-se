package de.ingrid.iplug.se.urlmaintenance;

import java.util.List;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IPartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Controller
@SessionAttributes("partnerProviderCommand")
@RequestMapping("/index.html")
public class UrlMaintenanceController extends NavigationSelector {

  private final IPartnerDao _partnerDao;
  private final IProviderDao _providerDao;

  @Autowired
  public UrlMaintenanceController(IPartnerDao partnerDao,
      IProviderDao providerDao) {
    _partnerDao = partnerDao;
    _providerDao = providerDao;
  }

  @ModelAttribute("partners")
  public List<Partner> referenceDataPartners() {
    return _partnerDao.getAll();
  }

  @ModelAttribute("providers")
  public List<Provider> referenceDataProviders() {
    return _providerDao.getAll();
  }

  @RequestMapping(method = RequestMethod.GET)
  public String urlMaintenance(Model model) {
    model.addAttribute("partnerProviderCommand", new PartnerProviderCommand());
    return "urlmaintenance";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String postUrlMaintenance(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    return "redirect:/welcomeEditUrls.html";
  }
}
