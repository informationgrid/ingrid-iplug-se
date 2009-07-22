package de.ingrid.iplug.se.urlmaintenance.catalog;

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
public class ListCatalogUrlsController {

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
  public String listTopicUrls() {
    return "catalog/listTopicUrls";
  }
  
  @RequestMapping(value = "/listServiceUrls.html", method = RequestMethod.GET)
  public String listServiceUrls() {
    return "catalog/listServiceUrls";
  }
  
  @RequestMapping(value = "/listMeasureUrls.html", method = RequestMethod.GET)
  public String listMeasureUrls() {
    return "catalog/listMeasureUrls";
  }

  @RequestMapping(value = "/topicsUrlSubset.html", method = RequestMethod.GET)
  public String topicsUrlSubset(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "startIndex", required = false) Integer start,
      @RequestParam(value = "pageSize", required = false) Integer length,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir, Model model,
      HttpServletRequest request) {
    start = start == null ? 0 : start;
    length = length == null ? 10 : length;

    Metadata topics = _metadataDao.getByKeyAndValue("datatype", "topics");
    ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);

    pushUrls(start, length, sort, dir, model, metadatas, byName);
    return "catalog/topicsUrlSubset";
  }

  @RequestMapping(value = "/serviceUrlSubset.html", method = RequestMethod.GET)
  public String serviceUrlSubset(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "startIndex", required = false) Integer start,
      @RequestParam(value = "pageSize", required = false) Integer length,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir, Model model,
      HttpServletRequest request) {
    start = start == null ? 0 : start;
    length = length == null ? 10 : length;

    Metadata topics = _metadataDao.getByKeyAndValue("datatype", "service");
    ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);

    pushUrls(start, length, sort, dir, model, metadatas, byName);
    return "catalog/serviceUrlSubset";
  }

  @RequestMapping(value = "/measureUrlSubset.html", method = RequestMethod.GET)
  public String measureUrlSubset(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "startIndex", required = false) Integer start,
      @RequestParam(value = "pageSize", required = false) Integer length,
      @RequestParam(value = "sort", required = false) String sort,
      @RequestParam(value = "dir", required = false) String dir, Model model,
      HttpServletRequest request) {
    start = start == null ? 0 : start;
    length = length == null ? 10 : length;

    Metadata topics = _metadataDao.getByKeyAndValue("datatype", "measure");
    ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);

    pushUrls(start, length, sort, dir, model, metadatas, byName);
    return "catalog/measureUrlSubset";
  }

  private void pushUrls(Integer start, Integer length, String sort, String dir,
      Model model, ArrayList<Metadata> metadatas, Provider byName) {
    OrderBy orderBy = "url".equals(sort) ? ("asc".equals(dir) ? OrderBy.URL_ASC
        : OrderBy.URL_DESC) : ("asc".equals(dir) ? OrderBy.CREATED_ASC
        : OrderBy.CREATED_DESC);
    Long count = 0L;
    List<CatalogUrl> catalogUrls = new ArrayList<CatalogUrl>();
    if (byName != null) {
      count = _catalogUrlDao.countByProviderAndMetadatas(byName, metadatas);
      catalogUrls = _catalogUrlDao.getByProviderAndMetadatas(byName, metadatas,
          start, length, orderBy);
    }
    model.addAttribute("urls", catalogUrls);
    model.addAttribute("count", count);
  }

}
