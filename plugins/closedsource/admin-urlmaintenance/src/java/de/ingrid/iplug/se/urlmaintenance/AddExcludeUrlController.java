package de.ingrid.iplug.se.urlmaintenance;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.ExcludeUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class AddExcludeUrlController {

  @RequestMapping(value = "/addExcludeUrl.html", method = RequestMethod.GET)
  public String addExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    return "addExcludeUrl";
  }

  @RequestMapping(value = "/addExcludeUrl.html", method = RequestMethod.POST)
  public String postAddExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    // add new command to fill out
    startUrlCommand.addExcludeUrlCommand(new ExcludeUrlCommand());
    return "redirect:addExcludeUrl.html";
  }

  @RequestMapping(value = "/removeExcludeUrl.html", method = RequestMethod.POST)
  public String removeExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<ExcludeUrlCommand> excludeUrlCommands = startUrlCommand
        .getExcludeUrlCommands();
    excludeUrlCommands.remove(index.intValue());
    return "redirect:addExcludeUrl.html";
  }

}
