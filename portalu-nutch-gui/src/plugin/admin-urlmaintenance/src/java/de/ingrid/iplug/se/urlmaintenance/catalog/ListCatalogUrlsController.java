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
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "catalogUrlCommand" })
public class ListCatalogUrlsController extends NavigationSelector {

  private final ICatalogUrlDao _catalogUrlDao;
  private final IMetadataDao _metadataDao;

  @Autowired
  public ListCatalogUrlsController(final ICatalogUrlDao catalogUrlDao, final IMetadataDao metadataDao) {
    _catalogUrlDao = catalogUrlDao;
    _metadataDao = metadataDao;
  }

  @ModelAttribute("catalogUrlCommand")
  public CatalogUrlCommand injectCatalogUrlCommand(
      @ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand) {
    final CatalogUrlCommand command = new CatalogUrlCommand();
    command.setProvider(partnerProviderCommand.getProvider());
    return command;
  }

  @ModelAttribute("metadatas")
  public List<Metadata> injectMetadatas() {
    List<Metadata> arrayList = new ArrayList<Metadata>();
    arrayList.addAll(_metadataDao.getByKey("lang"));
    return arrayList;
  }

  @RequestMapping(value = "/catalog/listTopicUrls.html", method = RequestMethod.GET)
  public String listTopicUrls(@ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) final String sort,
      @RequestParam(value = "dir", required = false) final String dir,
      @RequestParam(value = "lang", required = false) String[] langs, final Model model, final HttpServletRequest request) {
    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;

    final Metadata topics = _metadataDao.getByKeyAndValue("datatype", "topics");
    final ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    if (langs != null) {
        for (String l : langs) {
            metadatas.add(_metadataDao.getByKeyAndValue("lang", l));
        }
    }

    final int start = Paging.getStart(page, hitsPerPage);
    pushUrls(start, hitsPerPage, sort, dir, langs, model, metadatas, partnerProviderCommand.getProvider(), hitsPerPage, page);
    return "catalog/listTopicUrls";
  }

  @RequestMapping(value = "/catalog/listServiceUrls.html", method = RequestMethod.GET)
  public String listServiceUrls(
      @ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) final String sort,
      @RequestParam(value = "dir", required = false) final String dir,
      @RequestParam(value = "lang", required = false) String[] langs, final Model model, final HttpServletRequest request) {

    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;
    final Metadata topics = _metadataDao.getByKeyAndValue("datatype", "service");
    final ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    if (langs != null) {
        for (String l : langs) {
            metadatas.add(_metadataDao.getByKeyAndValue("lang", l));
        }
    }

    final int start = Paging.getStart(page, hitsPerPage);
    pushUrls(start, hitsPerPage, sort, dir, langs, model, metadatas, partnerProviderCommand.getProvider(), hitsPerPage, page);

    return "catalog/listServiceUrls";
  }

  @RequestMapping(value = "/catalog/listMeasureUrls.html", method = RequestMethod.GET)
  public String listMeasureUrls(
      @ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "page", required = false) Integer page,
      @RequestParam(value = "hitsPerPage", required = false) Integer hitsPerPage,
      @RequestParam(value = "sort", required = false) final String sort,
      @RequestParam(value = "dir", required = false) final String dir,
      @RequestParam(value = "lang", required = false) String[] langs, final Model model, final HttpServletRequest request) {

    page = page == null ? 1 : page;
    hitsPerPage = hitsPerPage == null ? 10 : hitsPerPage;
    final Metadata topics = _metadataDao.getByKeyAndValue("datatype", "measure");
    final ArrayList<Metadata> metadatas = new ArrayList<Metadata>();
    metadatas.add(topics);
    if (langs != null) {
        for (String l : langs) {
            metadatas.add(_metadataDao.getByKeyAndValue("lang", l));
        }
    }
    
    final int start = Paging.getStart(page, hitsPerPage);
    pushUrls(start, hitsPerPage, sort, dir, langs, model, metadatas, partnerProviderCommand.getProvider(), hitsPerPage, page);

    return "catalog/listMeasureUrls";
  }

  private void pushUrls(final Integer start, final Integer length, String sort, String dir, final String[] langs, final Model model,
      final ArrayList<Metadata> metadatas, final Provider byName, final Integer hitsPerPage, final Integer page) {

    sort = sort != null ? sort : "created";
    dir = dir != null ? dir : "desc";

    final OrderBy orderBy = "url".equals(sort) ? orderByUrl(dir) : ("edited".equals(sort) ? orderByUpdated(dir)
        : orderByCreated(dir));
    Long count = 0L;
    List<CatalogUrl> catalogUrls = new ArrayList<CatalogUrl>();
    if (byName != null) {
      count = _catalogUrlDao.countByProviderAndMetadatas(byName, metadatas);
      catalogUrls = _catalogUrlDao.getByProviderAndMetadatas(byName, metadatas, start, length, orderBy);
    }
    model.addAttribute("urls", catalogUrls);
    model.addAttribute("count", count);
    model.addAttribute("sort", sort);
    model.addAttribute("dir", dir);
    model.addAttribute("langs", langs);

    final Paging paging = new Paging(10, hitsPerPage, count.intValue(), page);
    model.addAttribute("paging", paging);
  }

  private OrderBy orderByUpdated(final String dir) {
    return "asc".equals(dir) ? OrderBy.UPDATED_ASC : OrderBy.UPDATED_DESC;
  }

  private OrderBy orderByCreated(final String dir) {
    return "asc".equals(dir) ? OrderBy.CREATED_ASC : OrderBy.CREATED_DESC;
  }

  private OrderBy orderByUrl(final String dir) {
    return "asc".equals(dir) ? OrderBy.URL_ASC : OrderBy.URL_DESC;
  }

}
