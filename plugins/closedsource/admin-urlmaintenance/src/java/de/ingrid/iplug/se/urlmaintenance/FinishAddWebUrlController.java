package de.ingrid.iplug.se.urlmaintenance;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.commandObjects.ExcludeUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class FinishAddWebUrlController {

  private final IStartUrlDao _startUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IExcludeUrlDao _excludeUrlDao;
  private final IProviderDao _providerDao;

  @Autowired
  public FinishAddWebUrlController(IStartUrlDao startUrlDao,
      ILimitUrlDao limitUrlDao, IExcludeUrlDao excludeDao, IProviderDao providerDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeDao;
    _providerDao = providerDao;
  }

  @RequestMapping(value = "/finishWebUrl.html", method = RequestMethod.GET)
  public String finish(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    List<LimitUrlCommand> limitUrlCommands = startUrlCommand
        .getLimitUrlCommands();
    List<ExcludeUrlCommand> excludeUrlCommands = startUrlCommand
        .getExcludeUrlCommands();
    cleanupLimitUrls(limitUrlCommands);
    cleanupLimitUrls(excludeUrlCommands);
    return "finishWebUrl";
  }

  @RequestMapping(value = "/finishWebUrl.html", method = RequestMethod.POST)
  public String postFinish(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    List<LimitUrlCommand> limitUrlCommands = startUrlCommand
        .getLimitUrlCommands();
    List<ExcludeUrlCommand> excludeUrlCommands = startUrlCommand
        .getExcludeUrlCommands();

    Provider provider = _providerDao.getByName(partnerProviderCommand
        .getProvider());

    List<ExcludeUrl> excludeUrls = new ArrayList<ExcludeUrl>();
    for (ExcludeUrlCommand excludeUrlCommand : excludeUrlCommands) {
      ExcludeUrl excludeUrl = new ExcludeUrl();
      excludeUrl.setUrl(excludeUrlCommand.getUrl());
      excludeUrl.setProvider(provider);
      excludeUrls.add(excludeUrl);
      _excludeUrlDao.makePersistent(excludeUrl);
    }
    List<LimitUrl> limitUrls = new ArrayList<LimitUrl>();
    for (LimitUrlCommand limitUrlCommand : limitUrlCommands) {
      LimitUrl limitUrl = new LimitUrl();
      limitUrl.setUrl(limitUrlCommand.getUrl());
      limitUrl.setProvider(provider);
      limitUrl.setMetadatas(limitUrlCommand.getMetadatas());
      limitUrls.add(limitUrl);
      _limitUrlDao.makePersistent(limitUrl);
    }
    
    StartUrl startUrl = new StartUrl();
    startUrl.setUrl(startUrlCommand.getUrl());
    startUrl.setProvider(provider);
    startUrl.setLimitUrls(limitUrls);
    startUrl.setExcludeUrls(excludeUrls);
    _startUrlDao.makePersistent(startUrl);
    return "redirect:listWebUrls.html";
  }

  @SuppressWarnings("unchecked")
  private void cleanupLimitUrls(List<? extends Url> list) {
    Iterator<Url> iterator = (Iterator<Url>) list.iterator();
    while (iterator.hasNext()) {
      Url url = (Url) iterator.next();
      if (url.getUrl() == null || url.getUrl().equals("")) {
        iterator.remove();
      }
    }
  }
}
