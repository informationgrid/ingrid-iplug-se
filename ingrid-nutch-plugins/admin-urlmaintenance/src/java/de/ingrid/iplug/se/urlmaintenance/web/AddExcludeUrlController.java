package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.ExcludeUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.validation.WebUrlCommandValidator;
import de.ingrid.nutch.admin.NavigationSelector;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand", "newLimitUrl", "newExcludeUrl" })
public class AddExcludeUrlController extends NavigationSelector {

  private final IExcludeUrlDao _excludeUrlDao;
  private final WebUrlCommandValidator _validator;

  @Autowired
  public AddExcludeUrlController(IExcludeUrlDao excludeUrlDao, final WebUrlCommandValidator validator) {
    _excludeUrlDao = excludeUrlDao;
    _validator = validator;
  }
  
  @ModelAttribute("newExcludeUrl")
  public ExcludeUrlCommand injectLimitUrlCommand() {
    ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
    return excludeUrlCommand;
  }

  @RequestMapping(value = "/web/addExcludeUrl.html", method = RequestMethod.GET)
  public String addExcludeUrl(@ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand, @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
          Model model) {
    //model.addAttribute("excludeUrls", startUrlCommand.getExcludeUrlCommands());
    //model.addAttribute("newExcludeUrl", new ExcludeUrlCommand(_excludeUrlDao));
    return "web/addExcludeUrl";
  }

  @RequestMapping(value = "/web/addExcludeUrl.html", method = RequestMethod.POST)
  public String postAddExcludeUrl(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
          @ModelAttribute("newExcludeUrl") ExcludeUrlCommand excludeUrlCommand, Errors errors,
          @RequestParam(value = "excludeUrl", required = false) final String excludeUrl) {
    // add new command to fill out
    //ExcludeUrlCommand excludeUrlCommand = new ExcludeUrlCommand(_excludeUrlDao);
    excludeUrlCommand.setProvider(startUrlCommand.getProvider());
    excludeUrlCommand.setUrl(excludeUrl);
    
    if (_validator.validateExcludeUrl(errors, startUrlCommand).hasErrors()) {
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
