package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.Paging;
import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class ListWebUrlsController extends NavigationSelector {

  private final IStartUrlDao _startUrlDao;
  private final IMetadataDao _metadataDao;
  private static final Log LOG = LogFactory.getLog(ListWebUrlsController.class);
  private final ILimitUrlDao _limitUrlDao;
  private final IExcludeUrlDao _excludeUrlDao;

  @Autowired
  public ListWebUrlsController(IStartUrlDao startUrlDao, ILimitUrlDao limitUrlDao, IExcludeUrlDao excludeUrlDao,
      IMetadataDao metadataDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
    _metadataDao = metadataDao;
  }

  @ModelAttribute("metadatas")
  public List<Metadata> injectMetadatas() {
    List<Metadata> arrayList = new ArrayList<Metadata>();
    arrayList.add(_metadataDao.getByKeyAndValue("datatype", "www"));
    arrayList.add(_metadataDao.getByKeyAndValue("datatype", "research"));
    arrayList.add(_metadataDao.getByKeyAndValue("datatype", "law"));
    arrayList.add(_metadataDao.getByKeyAndValue("lang", "de"));
    arrayList.add(_metadataDao.getByKeyAndValue("lang", "en"));
    return arrayList;
  }

  @RequestMapping(value = "/web/listWebUrls.html", method = RequestMethod.GET)
  public String listWebUrls(@ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir,
      @RequestParam(value = "datatype", required = false) String[] datatypes,
      @RequestParam(value = "lang", required = false) String[] langs, Model model, HttpServletRequest request) {
    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;
    langs = langs != null ? langs : new String[] {};
    datatypes = datatypes != null ? datatypes : new String[] {};
    sort = sort != null ? sort : "created";
    dir = dir != null ? dir : "desc";

    // filter by metadata
    List<Metadata> metadatas = new ArrayList<Metadata>();
    for (String lang : langs) {
      Metadata metadata = _metadataDao.getByKeyAndValue("lang", lang);
      metadatas.add(metadata);
    }
    for (String datatype : datatypes) {
      Metadata metadata = _metadataDao.getByKeyAndValue("datatype", datatype);
      metadatas.add(metadata);
    }

    int start = Paging.getStart(page, hitsPerPage);
    OrderBy orderBy = "url".equals(sort) ? orderByUrl(dir) : ("edited".equals(sort) ? orderByUpdated(dir)
        : orderByCreated(dir));
    Provider provider = partnerProviderCommand.getProvider();
    Long count = 0L;
    if (provider != null) {
      count = _startUrlDao.countByProviderAndMetadatas(provider, metadatas);
      LOG.debug("load start by provider [" + provider.getId() + "] with metadatas [" + metadatas + "] start: [" + start
          + "] hitsPerPage: [" + hitsPerPage + "] orderBy: [" + orderBy + "]");
      List<StartUrl> startUrls = _startUrlDao.getByProviderAndMetadatas(provider, metadatas, start, hitsPerPage,
          orderBy);
      model.addAttribute("urls", startUrls);
    }
    Paging paging = new Paging(10, hitsPerPage, count.intValue(), page);
    model.addAttribute("paging", paging);
    model.addAttribute("hitsPerPage", hitsPerPage);
    model.addAttribute("datatypes", datatypes);
    model.addAttribute("langs", langs);
    model.addAttribute("sort", sort);
    model.addAttribute("dir", dir);
    return "web/listWebUrls";
  }

  @ModelAttribute("startUrlCommand")
  public StartUrlCommand injectStartUrlCommand(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    StartUrlCommand startUrlCommand = new StartUrlCommand(_startUrlDao, _limitUrlDao, _excludeUrlDao);
    startUrlCommand.setProvider(partnerProviderCommand.getProvider());
    return startUrlCommand;
  }

  private OrderBy orderByUpdated(String dir) {
    return "asc".equals(dir) ? OrderBy.UPDATED_ASC : OrderBy.UPDATED_DESC;
  }

  private OrderBy orderByCreated(String dir) {
    return "asc".equals(dir) ? OrderBy.CREATED_ASC : OrderBy.CREATED_DESC;
  }

  private OrderBy orderByUrl(String dir) {
    return "asc".equals(dir) ? OrderBy.URL_ASC : OrderBy.URL_DESC;
  }

}
