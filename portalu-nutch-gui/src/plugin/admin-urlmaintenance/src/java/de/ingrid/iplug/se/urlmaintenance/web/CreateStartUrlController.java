package de.ingrid.iplug.se.urlmaintenance.web;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.ExcludeUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class CreateStartUrlController extends NavigationSelector {

  private final IStartUrlDao _startUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IExcludeUrlDao _excludeUrlDao;

  @Autowired
  public CreateStartUrlController(IStartUrlDao startUrlDao,
      ILimitUrlDao limitUrlDao, IExcludeUrlDao excludeUrlDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
  }

  @RequestMapping(value = { "/web/createStartUrl.html",
      "/web/editStartUrl.html" }, method = RequestMethod.GET)
  public String createStartUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam(value = "id", required = false) Long id) {
    if (id != null) {
      StartUrl startUrl = _startUrlDao.getById(id);
      startUrlCommand.read(startUrl);
    }
    return "web/createStartUrl";
  }

  @RequestMapping(value = "/web/createStartUrl.html", method = RequestMethod.POST)
  public String postCreateStartUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {

    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    limitUrlCommand.setProvider(startUrlCommand.getProvider());
    startUrlCommand.addLimitUrlCommand(limitUrlCommand);

    ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
    excludeUrlCommand.setProvider(startUrlCommand.getProvider());
    startUrlCommand.addExcludeUrlCommand(excludeUrlCommand);
    return "redirect:/web/addLimitUrl.html";
  }
}
