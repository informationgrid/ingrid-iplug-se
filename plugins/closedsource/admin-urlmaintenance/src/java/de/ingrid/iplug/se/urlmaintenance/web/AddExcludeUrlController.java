package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
public class AddExcludeUrlController {

  private final IExcludeUrlDao _excludeUrlDao;

  @Autowired
  public AddExcludeUrlController(IExcludeUrlDao excludeUrlDao) {
    _excludeUrlDao = excludeUrlDao;
  }

  @RequestMapping(value = "/addExcludeUrl.html", method = RequestMethod.GET)
  public String addExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    return "web/addExcludeUrl";
  }

  @RequestMapping(value = "/addExcludeUrl.html", method = RequestMethod.POST)
  public String postAddExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    // add new command to fill out
    ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
    excludeUrlCommand.setProvider(startUrlCommand.getProvider());
    startUrlCommand.addExcludeUrlCommand(excludeUrlCommand);
    return "redirect:addExcludeUrl.html";
  }

  @RequestMapping(value = "/removeExcludeUrl.html", method = RequestMethod.POST)
  public String removeExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<ExcludeUrlCommand> excludeUrls = startUrlCommand
        .getExcludeUrlCommands();
    excludeUrls.remove(index.intValue());
    return "redirect:addExcludeUrl.html";
  }

}
