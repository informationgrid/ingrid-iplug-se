package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

@Controller
public class DeleteWebUrlController extends NavigationSelector {

  private static final Log LOG = LogFactory
      .getLog(DeleteWebUrlController.class);
  private final IExcludeUrlDao _excludeUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IStartUrlDao _startUrlDao;

  @Autowired
  public DeleteWebUrlController(IStartUrlDao startUrlDao,
      ILimitUrlDao limitUrlDao, IExcludeUrlDao excludeUrlDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
  }

  @RequestMapping(method = RequestMethod.POST, value = "/web/deleteWebUrl.html")
  public String deleteWebUrl(
      @RequestParam(value = "id", required = true) final Long id) {

    StartUrl startUrl = _startUrlDao.getById(id);
    if (startUrl != null) {
      // TODO: check security user is allowed
      List<LimitUrl> limitUrls = startUrl.getLimitUrls();
      for (LimitUrl limitUrl : limitUrls) {
        LOG.info("delete limit url: " + limitUrl);
        _limitUrlDao.makeTransient(limitUrl);
      }

      List<ExcludeUrl> excludeUrls = startUrl.getExcludeUrls();
      for (ExcludeUrl excludeUrl : excludeUrls) {
        LOG.info("delete exclude url: " + excludeUrl);
        _excludeUrlDao.makeTransient(excludeUrl);
      }
      LOG.info("delete start url: " + startUrl);
      _startUrlDao.makeTransient(startUrl);
    }

    String view = "redirect:/web/listWebUrls.html";
    return view;
  }
}
