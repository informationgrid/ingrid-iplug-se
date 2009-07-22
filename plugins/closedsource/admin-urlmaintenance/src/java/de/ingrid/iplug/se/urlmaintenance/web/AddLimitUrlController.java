package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.List;

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
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class AddLimitUrlController {

  private final IMetadataDao _metadataDao;

  @Autowired
  public AddLimitUrlController(IMetadataDao metadataDao) {
    _metadataDao = metadataDao;
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

  @RequestMapping(value = "/addLimitUrl.html", method = RequestMethod.GET)
  public String addLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    return "web/addLimitUrl";
  }

  @RequestMapping(value = "/addLimitUrl.html", method = RequestMethod.POST)
  public String postAddLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    // add new command to fill out
    startUrlCommand.addLimitUrl(new LimitUrl());
    return "redirect:addLimitUrl.html";
  }

  @RequestMapping(value = "/removeLimitUrl.html", method = RequestMethod.POST)
  public String removeLimitUrl(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<LimitUrl> limitUrls = startUrlCommand.getLimitUrls();
    limitUrls.remove(index.intValue());
    return "redirect:addLimitUrl.html";
  }
}
