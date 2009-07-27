package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.EntityEditor;
import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class AddLimitUrlController {

  private final IMetadataDao _metadataDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IProviderDao _providerDao;

  @Autowired
  public AddLimitUrlController(IMetadataDao metadataDao,
      ILimitUrlDao limitUrlDao, IProviderDao providerDao) {
    _metadataDao = metadataDao;
    _limitUrlDao = limitUrlDao;
    _providerDao = providerDao;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(Metadata.class, new EntityEditor(_metadataDao));
  }

  @ModelAttribute("langs")
  public List<Metadata> injectLang() {
    return _metadataDao.getByKey("lang");
  }

  @ModelAttribute("datatypes")
  public List<Metadata> injectDatatypes() {
    return _metadataDao.getByKey("datatype");
  }

  @RequestMapping(value = "/addLimitUrl.html", method = RequestMethod.GET)
  public String addLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    return "web/addLimitUrl";
  }

  @RequestMapping(value = "/addLimitUrl.html", method = RequestMethod.POST)
  public String postAddLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    // add new command to fill out
    String provider = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(provider);
    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    limitUrlCommand.setProvider(byName);
    startUrlCommand.addLimitUrlCommand(limitUrlCommand);

    return "redirect:addLimitUrl.html";
  }

  @RequestMapping(value = "/removeLimitUrl.html", method = RequestMethod.POST)
  public String removeLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<LimitUrlCommand> limitUrls = startUrlCommand.getLimitUrlCommands();
    limitUrls.remove(index.intValue());
    return "redirect:addLimitUrl.html";
  }
}
