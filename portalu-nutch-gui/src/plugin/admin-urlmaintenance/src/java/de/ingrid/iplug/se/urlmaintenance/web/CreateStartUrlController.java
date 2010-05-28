package de.ingrid.iplug.se.urlmaintenance.web;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
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
import de.ingrid.iplug.se.urlmaintenance.validation.WebUrlCommandValidator;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class CreateStartUrlController extends NavigationSelector {

  private final IStartUrlDao _startUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IExcludeUrlDao _excludeUrlDao;
  private final WebUrlCommandValidator _validator;
  private String _mode;

  @Autowired
  public CreateStartUrlController(final IStartUrlDao startUrlDao, final ILimitUrlDao limitUrlDao,
      final IExcludeUrlDao excludeUrlDao, final WebUrlCommandValidator validator) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
    _validator = validator;
    _mode = "new";
  }

  @RequestMapping(value = { "/web/createStartUrl.html",
      "/web/editStartUrl.html" }, method = RequestMethod.GET)
  public String createStartUrl(
      @ModelAttribute("startUrlCommand") final StartUrlCommand startUrlCommand,
      @RequestParam(value = "id", required = false) final Long id) {
    if (id != null) {
      final StartUrl startUrl = _startUrlDao.getById(id);
      startUrlCommand.read(startUrl);
      _mode = "edit";
    } else {
      _mode = "new";
    }
    return "web/createStartUrl";
  }

  @RequestMapping(value = "/web/createStartUrl.html", method = RequestMethod.POST)
  public String postCreateStartUrl(@ModelAttribute("startUrlCommand") final StartUrlCommand startUrlCommand,
      final Errors errors, @ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand) {

    if (_validator.validateStartUrl(errors).hasErrors()) {
        return createStartUrl(startUrlCommand, null);
    }
      
    if ("new".equals(_mode)) {
      final LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
      limitUrlCommand.setProvider(startUrlCommand.getProvider());
      limitUrlCommand.setUrl(startUrlCommand.getUrl());
      startUrlCommand.addLimitUrlCommand(limitUrlCommand);

      final ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
      excludeUrlCommand.setProvider(startUrlCommand.getProvider());
      startUrlCommand.addExcludeUrlCommand(excludeUrlCommand);
    }
    return "redirect:/web/addLimitUrl.html";
  }
}
