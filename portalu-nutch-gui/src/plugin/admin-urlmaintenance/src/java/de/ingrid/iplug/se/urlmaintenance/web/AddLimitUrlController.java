package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.List;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.EntityEditor;
import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class AddLimitUrlController extends NavigationSelector {

  private final IMetadataDao _metadataDao;
  private final ILimitUrlDao _limitUrlDao;

  @Autowired
  public AddLimitUrlController(IMetadataDao metadataDao,
      ILimitUrlDao limitUrlDao) {
    _metadataDao = metadataDao;
    _limitUrlDao = limitUrlDao;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(Metadata.class, new EntityEditor(_metadataDao));
  }

  @ModelAttribute("langs")
  public List<Metadata> injectLang() {
    return _metadataDao.getByKey("lang");
  }

  @ModelAttribute("datatypes")
  public List<Metadata> injectDatatypes() {
    return _metadataDao.getByKey("datatype");
  }

  @RequestMapping(value = "/web/addLimitUrl.html", method = RequestMethod.GET)
  public String addLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    return "web/addLimitUrl";
  }

  @RequestMapping(value = "/web/addLimitUrl.html", method = RequestMethod.POST)
  public String postAddLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    // add new command to fill out
    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    limitUrlCommand.setProvider(startUrlCommand.getProvider());
    startUrlCommand.addLimitUrlCommand(limitUrlCommand);

    return "redirect:/web/addLimitUrl.html";
  }

  @RequestMapping(value = "/web/removeLimitUrl.html", method = RequestMethod.POST)
  public String removeLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<LimitUrlCommand> limitUrls = startUrlCommand.getLimitUrlCommands();
    limitUrls.remove(index.intValue());
    return "redirect:/web/addLimitUrl.html";
  }
}
