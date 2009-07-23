package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
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

  private static final Log LOG = LogFactory
      .getLog(FinishAddWebUrlController.class);
  private final IStartUrlDao _startUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IExcludeUrlDao _excludeUrlDao;
  private final IProviderDao _providerDao;

  @Autowired
  public FinishAddWebUrlController(IStartUrlDao startUrlDao,
      ILimitUrlDao limitUrlDao, IExcludeUrlDao excludeDao,
      IProviderDao providerDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeDao;
    _providerDao = providerDao;
  }

  @RequestMapping(value = "/finishWebUrl.html", method = RequestMethod.GET)
  public String finish(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    List<LimitUrl> limitUrls = startUrlCommand.getLimitUrls();
    List<ExcludeUrl> excludeUrls = startUrlCommand.getExcludeUrls();
    cleanupEmptyUrls(limitUrls);
    cleanupEmptyUrls(excludeUrls);
    return "web/finishWebUrl";
  }

  @RequestMapping(value = "/finishWebUrl.html", method = RequestMethod.POST)
  public String postFinish(
      @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {

    Provider provider = _providerDao.getByName(partnerProviderCommand
        .getProvider());

    // load start url from db if id exists
    StartUrl startUrl = new StartUrl();
    if (startUrlCommand.getId() != null) {
      LOG.info("load startUrl with id: " + startUrlCommand.getId());
      startUrl = _startUrlDao.getById(startUrlCommand.getId());
      startUrl.setUpdated(new Date());
      startUrl.setUrl(startUrlCommand.getUrl());
    } else {
      LOG.info("create new start url: " + startUrlCommand.getUrl());
      startUrl.setUrl(startUrlCommand.getUrl());
      startUrl.setProvider(provider);
      startUrl.setCreated(new Date());
      startUrl.setUpdated(new Date());
    }

    // delete/update/add previous limit urls
    List<LimitUrl> newLimitUrls = startUrlCommand.getLimitUrls();
    for (LimitUrl newLimitUrl : newLimitUrls) {
      List<LimitUrl> limitUrlsFromDb = startUrl.getLimitUrls();

      // update
      boolean update = false;
      Iterator<LimitUrl> iterator = limitUrlsFromDb.iterator();
      while (iterator.hasNext()) {
        LimitUrl limitUrlFromDb = (LimitUrl) iterator.next();
        if (limitUrlFromDb.equals(newLimitUrl)) {
          // reload limit url
          LOG.info("reload limiturl with id: " + limitUrlFromDb.getId());
          LimitUrl refreshedUrl = _limitUrlDao.getById(limitUrlFromDb.getId());
          LOG.info("update limit url: " + refreshedUrl.getId());
          refreshedUrl.setUrl(newLimitUrl.getUrl());
          refreshedUrl.setMetadatas(newLimitUrl.getMetadatas());
          refreshedUrl.setUpdated(new Date());
          iterator.remove();
          update = true;
          break;
        }
      }

      // create
      if (!update) {
        newLimitUrl.setProvider(provider);
        newLimitUrl.setCreated(new Date());
        newLimitUrl.setUpdated(new Date());
        newLimitUrl.setStartUrl(startUrl);
        // startUrl.addLimitUrl(newLimitUrl);
        _limitUrlDao.makePersistent(newLimitUrl);
      }

      // delete
      iterator = limitUrlsFromDb.iterator();
      while (iterator.hasNext()) {
        LimitUrl limitUrlFromDb = (LimitUrl) iterator.next();
        // reload url
        LimitUrl refreshedUrl = _limitUrlDao.getById(limitUrlFromDb.getId());
        LOG.info("delete limit url: " + refreshedUrl.getId());
        _limitUrlDao.makeTransient(refreshedUrl);
      }
    }

    if (startUrl.getId() == null) {
      _startUrlDao.makePersistent(startUrl);
    }
    return "redirect:listWebUrls.html";
  }

  @SuppressWarnings("unchecked")
  private void cleanupEmptyUrls(List<? extends Url> list) {
    Iterator<Url> iterator = (Iterator<Url>) list.iterator();
    while (iterator.hasNext()) {
      Url url = (Url) iterator.next();
      if (url.getUrl() == null || url.getUrl().equals("")) {
        iterator.remove();
      }
    }
  }

}
