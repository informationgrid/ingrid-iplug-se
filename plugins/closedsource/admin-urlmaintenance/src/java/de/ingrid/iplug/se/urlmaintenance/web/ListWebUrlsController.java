package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class ListWebUrlsController {

  private final IStartUrlDao _startUrlDao;
  private final IProviderDao _providerDao;
  private final IMetadataDao _metadataDao;
  private int _hitsPerPage = 10;

  @Autowired
  public ListWebUrlsController(IProviderDao providerDao,
      IStartUrlDao startUrlDao, IMetadataDao metadataDao) {
    _providerDao = providerDao;
    _startUrlDao = startUrlDao;
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

  @RequestMapping(value = "/listWebUrls.html", method = RequestMethod.GET)
  public String listWebUrls(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir, Model model,
      HttpServletRequest request) {
    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;

    // paging
    int start = Paging.getStart(page, hitsPerPage);

    OrderBy orderBy = "url".equals(sort) ? ("asc".equals(dir) ? OrderBy.URL_ASC
        : OrderBy.URL_DESC) : ("asc".equals(dir) ? OrderBy.TIMESTAMP_ASC
        : OrderBy.TIMESTAMP_DESC);
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);
    Long count = 0L;
    if (byName != null) {
      count = _startUrlDao.countByProvider(byName);
      List<StartUrl> startUrls = _startUrlDao.getByProvider(byName, start,
          hitsPerPage, orderBy);
      model.addAttribute("urls", startUrls);
    }
    Paging paging = new Paging(10, hitsPerPage, count.intValue(), page);
    model.addAttribute("paging", paging);
    return "web/listWebUrls";
  }

  @ModelAttribute("startUrlCommand")
  public StartUrlCommand injectStartUrlCommand() {
    return new StartUrlCommand();
  }

  // @RequestMapping(value = "/startUrlSubset.html", method = RequestMethod.GET)
  // public String startUrlSubset(
  // @ModelAttribute("partnerProviderCommand") PartnerProviderCommand
  // partnerProviderCommand,
  // @RequestParam(value = "startIndex", required = false) Integer start,
  // @RequestParam(value = "pageSize", required = false) Integer length,
  // @RequestParam(value = "sort", required = false) String sort,
  // @RequestParam(value = "dir", required = false) String dir, Model model,
  // HttpServletRequest request) {
  // System.out.println(request.getParameterMap());
  // start = start == null ? 0 : start;
  // length = length == null ? 10 : length;
  //
  // OrderBy orderBy = "url".equals(sort) ? ("asc".equals(dir) ? OrderBy.URL_ASC
  // : OrderBy.URL_DESC) : ("asc".equals(dir) ? OrderBy.TIMESTAMP_ASC
  // : OrderBy.TIMESTAMP_DESC);
  // String providerString = partnerProviderCommand.getProvider();
  // Provider byName = _providerDao.getByName(providerString);
  // Long count = 0L;
  // if (byName != null) {
  // count = _startUrlDao.countByProvider(byName);
  // List<StartUrl> startUrls = _startUrlDao.getByProvider(byName, start,
  // length, orderBy);
  // model.addAttribute("urls", startUrls);
  // }
  //
  // model.addAttribute("count", count);
  //
  // return "web/startUrlSubset";
  // }
}
