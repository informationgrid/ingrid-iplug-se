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

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class AddExcludeUrlController extends NavigationSelector {

  private final IExcludeUrlDao _excludeUrlDao;

  @Autowired
  public AddExcludeUrlController(IExcludeUrlDao excludeUrlDao) {
    _excludeUrlDao = excludeUrlDao;
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

    validateUrl(startUrlCommand, errors, excludeUrlCommand);

    if (errors.hasErrors()) {
      return "web/addExcludeUrl";
    } else {
      startUrlCommand.addExcludeUrlCommand(excludeUrlCommand);
    }
    return "redirect:/web/addExcludeUrl.html";
  }

  private void validateUrl(StartUrlCommand startUrlCommand, Errors errors, ExcludeUrlCommand excludeUrlCommand) {
    try {
      // verify the url as it will be done in BasicURLNormalizer
      new URL(startUrlCommand.getExcludeUrlCommands().get(startUrlCommand.getExcludeUrlCommands().size() - 1).getUrl());
    } catch (MalformedURLException e) {
      errors.rejectValue("excludeUrlCommands[" + (startUrlCommand.getExcludeUrlCommands().size() - 1) + "].url",
          "invalid.url", new String[] { e.getMessage() }, e.getMessage());
    }
  }

  @RequestMapping(value = "/web/removeExcludeUrl.html", method = RequestMethod.POST)
  public String removeExcludeUrl(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<ExcludeUrlCommand> excludeUrls = startUrlCommand.getExcludeUrlCommands();
    excludeUrls.remove(index.intValue());
    return "redirect:/web/addExcludeUrl.html";
  }

}
