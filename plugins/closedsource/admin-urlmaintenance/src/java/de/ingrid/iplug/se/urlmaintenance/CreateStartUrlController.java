package de.ingrid.iplug.se.urlmaintenance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class CreateStartUrlController {

  private final IStartUrlDao _startUrlDao;

  @Autowired
  public CreateStartUrlController(IStartUrlDao startUrlDao) {
    _startUrlDao = startUrlDao;
  }

  @RequestMapping(value = "/createStartUrl.html", method = RequestMethod.GET)
  public String createStartUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam(value = "id", required = false) Long id) {
    if (id != null) {
      // TODO set url etc.
    }
    // add limit url to fill out
    startUrlCommand.addLimitUrlCommand(new LimitUrlCommand());
    return "createStartUrl";
  }

  @RequestMapping(value = "/createStartUrl.html", method = RequestMethod.POST)
  public String postCreateStartUrl(
      @ModelAttribute("webUrlCommand") WebUrlCommand webUrlCommand) {

    return "redirect:addLimitUrl.html";
  }
}
