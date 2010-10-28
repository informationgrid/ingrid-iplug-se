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
  private final WebUrlCommandValidator _validator;

  @Autowired
  public CreateStartUrlController(final IStartUrlDao startUrlDao, final ILimitUrlDao limitUrlDao,
      final IExcludeUrlDao excludeUrlDao, final WebUrlCommandValidator validator) {
    _startUrlDao = startUrlDao;
    _validator = validator;
  }

  @RequestMapping(value = { "/web/createStartUrl.html",
      "/web/editStartUrl.html" }, method = RequestMethod.GET)
  public String createStartUrl(
      @ModelAttribute("startUrlCommand") final StartUrlCommand startUrlCommand,
      @RequestParam(value = "id", required = false) final Long id) {
    if (id != null) {
      final StartUrl startUrl = _startUrlDao.getById(id);
      startUrlCommand.read(startUrl);
    }
    return "web/createStartUrl";
  }

  @RequestMapping(value = "/web/createStartUrl.html", method = RequestMethod.POST)
  public String postCreateStartUrl(@ModelAttribute("startUrlCommand") final StartUrlCommand startUrlCommand,
      final Errors errors, @ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand) {

    if (_validator.validateStartUrl(errors).hasErrors()) {
        return createStartUrl(startUrlCommand, null);
    }
    return "redirect:/web/addLimitUrl.html";
  }
}
