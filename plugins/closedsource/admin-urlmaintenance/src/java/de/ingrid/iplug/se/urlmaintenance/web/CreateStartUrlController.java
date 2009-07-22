package de.ingrid.iplug.se.urlmaintenance.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class CreateStartUrlController {

  private final IStartUrlDao _startUrlDao;

  @Autowired
  public CreateStartUrlController(IStartUrlDao startUrlDao) {
    _startUrlDao = startUrlDao;
  }

  @RequestMapping(value = { "/createStartUrl.html", "/editStartUrl.html" }, method = RequestMethod.GET)
  public String createStartUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam(value = "id", required = false) Long id) {
    if (id != null) {
      StartUrl byId = _startUrlDao.getById(id);
      startUrlCommand.setId(byId.getId());
      startUrlCommand.setCreated(byId.getCreated());
      startUrlCommand.setProvider(byId.getProvider());
      startUrlCommand.setUrl(byId.getUrl());
      startUrlCommand.setLimitUrls(byId.getLimitUrls());
      startUrlCommand.setExcludeUrls(byId.getExcludeUrls());
    }
    return "web/createStartUrl";
  }

  @RequestMapping(value = "/createStartUrl.html", method = RequestMethod.POST)
  public String postCreateStartUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {

    if (startUrlCommand.getLimitUrls().isEmpty()) {
      startUrlCommand.addLimitUrl(new LimitUrl());
    }
    if (startUrlCommand.getExcludeUrls().isEmpty()) {
      startUrlCommand.getExcludeUrls().add(new ExcludeUrl());
    }
    return "redirect:addLimitUrl.html";
  }
}
