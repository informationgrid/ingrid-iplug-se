package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import de.ingrid.iplug.se.urlmaintenance.validation.WebUrlCommandValidator;
import de.ingrid.nutch.admin.NavigationSelector;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand", "newLimitUrl" })
public class AddLimitUrlController extends NavigationSelector {

  private final IMetadataDao _metadataDao;
  private final ILimitUrlDao _limitUrlDao;
  private final WebUrlCommandValidator _validator;

  @Autowired
  public AddLimitUrlController(IMetadataDao metadataDao, ILimitUrlDao limitUrlDao, final WebUrlCommandValidator validator) {
    _metadataDao = metadataDao;
    _limitUrlDao = limitUrlDao;
    _validator = validator;
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
      List<Metadata> arrayList = new ArrayList<Metadata>();
      arrayList.add(_metadataDao.getByKeyAndValue("datatype", "default"));
      arrayList.add(_metadataDao.getByKeyAndValue("datatype", "law"));
      arrayList.add(_metadataDao.getByKeyAndValue("datatype", "research"));
    
      return arrayList;
  }
  
  @ModelAttribute("newLimitUrl")
  public LimitUrlCommand injectLimitUrlCommand() {
    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    return limitUrlCommand;
  }

  @RequestMapping(value = "/web/addLimitUrl.html", method = RequestMethod.GET)
  public String addLimitUrl(@ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand, @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
          Model model) {
    return "web/addLimitUrl";
  }

  @RequestMapping(value = "/web/addLimitUrl.html", method = RequestMethod.POST)
  public String postAddLimitUrl(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
          @ModelAttribute("newLimitUrl") LimitUrlCommand limitUrlCommand, BindingResult errorsLimit,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "limitUrl", required = false) final String limitUrl) {
    limitUrlCommand.setProvider(startUrlCommand.getProvider());
    limitUrlCommand.setUrl(limitUrl);
    
    // add datatype:www if datatype:default is present
    List<Metadata> metas = limitUrlCommand.getMetadatas();
    for (Metadata meta : metas) {
        if (meta.getMetadataKey().equals("datatype") && meta.getMetadataValue().equals("default")) {
            metas.add( _metadataDao.getByKeyAndValue("datatype", "www"));
            break;
        }        
    }

    if (_validator.validateLimitUrl(errorsLimit, startUrlCommand).hasErrors()) {
      return "web/addLimitUrl";
    }
    
    startUrlCommand.addLimitUrlCommand(limitUrlCommand);

    return "redirect:/web/addLimitUrl.html";
  }

  @RequestMapping(value = "/web/removeLimitUrl.html", method = RequestMethod.POST)
  public String removeLimitUrl(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @RequestParam("index") Integer index) {
    List<LimitUrlCommand> limitUrls = startUrlCommand.getLimitUrlCommands();
    limitUrls.remove(index.intValue());
    return "redirect:/web/addLimitUrl.html";
  }
}
