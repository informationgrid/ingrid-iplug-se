package de.ingrid.iplug.se.urlmaintenance.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class CreateStartUrlController {

  private final IStartUrlDao _startUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IProviderDao _providerDao;

  @Autowired
  public CreateStartUrlController(
      IStartUrlDao startUrlDao,
      ILimitUrlDao limitUrlDao,
 IProviderDao providerDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _providerDao = providerDao;
  }

  @RequestMapping(value = { "/createStartUrl.html", "/editStartUrl.html" }, method = RequestMethod.GET)
  public String createStartUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam(value = "id", required = false) Long id) {
    if (id != null) {
      StartUrl byId = _startUrlDao.getById(id);
      startUrlCommand.read(byId);
    }
    return "web/createStartUrl";
  }

  @RequestMapping(value = "/createStartUrl.html", method = RequestMethod.POST)
  public String postCreateStartUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {

    String provider = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(provider);

    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    limitUrlCommand.setProvider(byName);
    startUrlCommand.addLimitUrlCommand(limitUrlCommand);
    startUrlCommand.addExcludeUrl(new ExcludeUrl());
    return "redirect:addLimitUrl.html";
  }
}
