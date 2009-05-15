package de.ingrid.iplug.se.urlmaintenance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

@Controller
@SessionAttributes(value = { "partnerProviderCommand" })
public class ListWebUrlsController {

  private final IStartUrlDao _startUrlDao;
  private final IProviderDao _providerDao;
  private final IMetadataDao _metadataDao;

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
    arrayList.add(_metadataDao.getByKeyAndValue("datatype", "topics"));
    arrayList.add(_metadataDao.getByKeyAndValue("datatype", "research"));
    arrayList.add(_metadataDao.getByKeyAndValue("datatype", "law"));
    arrayList.add(_metadataDao.getByKeyAndValue("lang", "de"));
    arrayList.add(_metadataDao.getByKeyAndValue("lang", "en"));
    return arrayList;
  }

  @RequestMapping(value = "/listWebUrls.html", method = RequestMethod.GET)
  public String listWebUrls() {
    return "listWebUrls";
  }

  @RequestMapping(value = "/startUrlSubset.html", method = RequestMethod.GET)
  public String startUrlSubset(
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand,
      @RequestParam(value = "startIndex", required = false) Integer start,
      @RequestParam(value = "pageSize", required = false) Integer length,
      @RequestParam(value = "sort", required = false) String sort, Model model) {
    start = start == null ? 0 : start;
    length = length == null ? 10 : length;

    OrderBy orderBy = "url".equals(sort) ? OrderBy.URL : OrderBy.TIMESTAMP;
    System.out.println(start);
    System.out.println(length);
    System.out.println(sort);
    System.out.println();
    String providerString = partnerProviderCommand.getProvider();
    Provider byName = _providerDao.getByName(providerString);
    Long count = 0L;
    if (byName != null) {
      count = _startUrlDao.countByProvider(byName);
      List<StartUrl> startUrls = _startUrlDao.getByProvider(byName, start,
          length, orderBy);
      model.addAttribute("urls", startUrls);
    }

    model.addAttribute("count", count);

    return "startUrlSubset";
  }
}
