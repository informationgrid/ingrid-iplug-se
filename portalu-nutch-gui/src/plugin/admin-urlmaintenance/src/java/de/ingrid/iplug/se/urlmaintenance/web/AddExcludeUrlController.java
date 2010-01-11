package de.ingrid.iplug.se.urlmaintenance.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.ExcludeUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.validation.WebUrlCommandValidator;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class AddExcludeUrlController extends NavigationSelector {

  private final IExcludeUrlDao _excludeUrlDao;
  private final WebUrlCommandValidator _validator;

  @Autowired
  public AddExcludeUrlController(IExcludeUrlDao excludeUrlDao, final WebUrlCommandValidator validator) {
    _excludeUrlDao = excludeUrlDao;
    _validator = validator;
  }

  @RequestMapping(value = "/web/addExcludeUrl.html", method = RequestMethod.GET)
  public String addExcludeUrl(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    return "web/addExcludeUrl";
  }

  @RequestMapping(value = "/web/addExcludeUrl.html", method = RequestMethod.POST)
  public String postAddExcludeUrl(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand, Errors errors) {
    // add new command to fill out
    ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
    excludeUrlCommand.setProvider(startUrlCommand.getProvider());
    
    if (_validator.validateExcludeUrl(errors).hasErrors()) {
        return "web/addExcludeUrl";
    }
    
    startUrlCommand.addExcludeUrlCommand(excludeUrlCommand);
    return "redirect:/web/addExcludeUrl.html";
  }

  @RequestMapping(value = "/web/removeExcludeUrl.html", method = RequestMethod.POST)
  public String removeExcludeUrl(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<ExcludeUrlCommand> excludeUrls = startUrlCommand.getExcludeUrlCommands();
    excludeUrls.remove(index.intValue());
    return "redirect:/web/addExcludeUrl.html";
  }

}
