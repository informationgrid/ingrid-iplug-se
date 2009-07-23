package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "catalogUrlCommand" })
public class SaveCatalogUrlController {

  private final ICatalogUrlDao _catalogUrlDao;

  private static final Set<String> _supportedTypes = new HashSet<String>(Arrays
      .asList(new String[] { "topics", "service", "measure" }));

  @Autowired
  public SaveCatalogUrlController(ICatalogUrlDao catalogUrlDao) {
    _catalogUrlDao = catalogUrlDao;
  }

  @RequestMapping(value = "/saveCatalogUrl.html", method = RequestMethod.GET)
  public String saveCatalogUrl(
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand,
      Model model) {
    List<Metadata> metadatas = catalogUrlCommand.getMetadatas();
    String type = "topics";
    for (Metadata metadata : metadatas) {
      String metadataKey = metadata.getMetadataKey();
      String metadataValue = metadata.getMetadataValue();
      if ("datatype".equals(metadataKey)
          && _supportedTypes.contains(metadataValue)) {
        type = metadataValue;
        break;
      }
    }
    model.addAttribute("type", type);
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
    catalogUrl.setUpdated(new Date());
    catalogUrl.setMetadatas(catalogUrlCommand.getMetadatas());

    String redirectUrl = "redirect:/listTopicUrls.html";
    List<Metadata> metadatas = catalogUrlCommand.getMetadatas();
    for (Metadata metadata : metadatas) {
      String metadataKey = metadata.getMetadataKey();
      String metadataValue = metadata.getMetadataValue();
      if (metadataKey.equals("datatype") && metadataValue.equals("service")) {
        redirectUrl = "redirect:/listServiceUrls.html";
        break;
      } else if (metadataKey.equals("datatype")
          && metadataValue.equals("measure")) {
        redirectUrl = "redirect:/listMeasureUrls.html";
      }
    }
    return redirectUrl;
  }
}
