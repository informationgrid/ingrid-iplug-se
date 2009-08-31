package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

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
import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "catalogUrlCommand" })
public class ListCatalogUrlsController extends NavigationSelector {

  private final ICatalogUrlDao _catalogUrlDao;
  private final IProviderDao _providerDao;
  private final IMetadataDao _metadataDao;

  @Autowired
  public ListCatalogUrlsController(IProviderDao providerDao,
      ICatalogUrlDao catalogUrlDao, IMetadataDao metadataDao) {
    _providerDao = providerDao;
    _catalogUrlDao = catalogUrlDao;
    _metadataDao = metadataDao;
  }

  @ModelAttribute("catalogUrlCommand")
  public CatalogUrlCommand injectCatalogUrlCommand(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    Provider provider = _providerDao.getByName(partnerProviderCommand
        .getProvider());
    CatalogUrlCommand command = new CatalogUrlCommand();
    command.setProvider(provider);
    return command;
  }

  @RequestMapping(value = "/listTopicUrls.html", method = RequestMethod.GET)
  public String listTopicUrls(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir, Model model,
      HttpServletRequest request) {
    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;

    Metadata topics = _metadataDao.getByKeyAndValue("datatype", "topics");
    ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);

    int start = Paging.getStart(page, hitsPerPage);
    pushUrls(start, hitsPerPage, sort, dir, model, metadatas, byName,
        hitsPerPage, page);
    return "catalog/listTopicUrls";
  }

  @RequestMapping(value = "/listServiceUrls.html", method = RequestMethod.GET)
  public String listServiceUrls(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir, Model model,
      HttpServletRequest request) {

    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;
    Metadata topics = _metadataDao.getByKeyAndValue("datatype", "service");
    ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);

    int start = Paging.getStart(page, hitsPerPage);
    pushUrls(start, hitsPerPage, sort, dir, model, metadatas, byName,
        hitsPerPage, page);

    return "catalog/listServiceUrls";
  }

  @RequestMapping(value = "/listMeasureUrls.html", method = RequestMethod.GET)
  public String listMeasureUrls(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir, Model model,
      HttpServletRequest request) {

    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;
    Metadata topics = _metadataDao.getByKeyAndValue("datatype", "measure");
    ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);

    int start = Paging.getStart(page, hitsPerPage);
    pushUrls(start, hitsPerPage, sort, dir, model, metadatas, byName,
        hitsPerPage, page);

    return "catalog/listMeasureUrls";
  }

  private void pushUrls(Integer start, Integer length, String sort, String dir,
      Model model, ArrayList<Metadata> metadatas, Provider byName,
      Integer hitsPerPage, Integer page) {

    sort = sort != null ? sort : "created";
    dir = dir != null ? dir : "desc";

    OrderBy orderBy = "url".equals(sort) ? orderByUrl(dir) : ("edited"
        .equals(sort) ? orderByUpdated(dir) : orderByCreated(dir));
    Long count = 0L;
    List<CatalogUrl> catalogUrls = new ArrayList<CatalogUrl>();
    if (byName != null) {
      count = _catalogUrlDao.countByProviderAndMetadatas(byName, metadatas);
      catalogUrls = _catalogUrlDao.getByProviderAndMetadatas(byName, metadatas,
          start, length, orderBy);
    }
    model.addAttribute("urls", catalogUrls);
    model.addAttribute("count", count);
    model.addAttribute("sort", sort);
    model.addAttribute("dir", dir);

    Paging paging = new Paging(10, hitsPerPage, count.intValue(), page);
    model.addAttribute("paging", paging);
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
