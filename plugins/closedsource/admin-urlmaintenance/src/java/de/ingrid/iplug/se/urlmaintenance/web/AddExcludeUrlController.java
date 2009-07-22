package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class AddExcludeUrlController {

  @RequestMapping(value = "/addExcludeUrl.html", method = RequestMethod.GET)
  public String addExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    return "web/addExcludeUrl";
  }

  @RequestMapping(value = "/addExcludeUrl.html", method = RequestMethod.POST)
  public String postAddExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    // add new command to fill out
    startUrlCommand.addExcludeUrl(new ExcludeUrl());
    return "redirect:addExcludeUrl.html";
  }

  @RequestMapping(value = "/removeExcludeUrl.html", method = RequestMethod.POST)
  public String removeExcludeUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<ExcludeUrl> excludeUrls = startUrlCommand.getExcludeUrls();
    excludeUrls.remove(index.intValue());
    return "redirect:addExcludeUrl.html";
  }

}
