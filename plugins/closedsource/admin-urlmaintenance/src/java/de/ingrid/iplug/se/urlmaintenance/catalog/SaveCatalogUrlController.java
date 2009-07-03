package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "catalogUrlCommand" })
public class SaveCatalogUrlController {

  private final ICatalogUrlDao _catalogUrlDao;

  @Autowired
  public SaveCatalogUrlController(ICatalogUrlDao catalogUrlDao) {
    _catalogUrlDao = catalogUrlDao;
  }

  @RequestMapping(value = "/saveCatalogUrl.html", method = RequestMethod.GET)
  public String saveCatalogUrl(
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand) {
    return "catalog/saveCatalogUrl";
  }

  @RequestMapping(value = "/saveCatalogUrl.html", method = RequestMethod.POST)
  public String postSaveCatalogUrl(
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    
    CatalogUrl catalogUrl = new CatalogUrl();
    if (catalogUrlCommand.getId() > -1) {
      catalogUrl = _catalogUrlDao.getById(catalogUrlCommand.getId());
    } else {
      catalogUrl.setProvider(catalogUrlCommand.getProvider());
      _catalogUrlDao.makePersistent(catalogUrl);
    }
    catalogUrl.setUrl(catalogUrlCommand.getUrl());
    catalogUrl.setMetadatas(catalogUrlCommand.getMetadatas());
    catalogUrl.setEdited(new Date());

    return "redirect:/listCatalogUrls.html";
  }
}
