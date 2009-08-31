package de.ingrid.iplug.se.urlmaintenance.catalog;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;

@Controller
public class DeleteCatalogUrlController extends NavigationSelector {

  private ICatalogUrlDao _catalogUrlDao;

  @Autowired
  public DeleteCatalogUrlController(ICatalogUrlDao catalogUrlDao) {
    _catalogUrlDao = catalogUrlDao;

  }

  @RequestMapping(method = RequestMethod.POST, value = "/deleteCatalogUrl.html")
  public String deleteCatalogUrl(
      @RequestParam(value = "id", required = true) final Long id,
      @RequestParam(value = "type", required = false) final String type) {

    CatalogUrl url = _catalogUrlDao.getById(id);
    if (url != null) {
      // TODO: check security user is allowed
      _catalogUrlDao.makeTransient(url);
    }

    String view = "redirect:/listTopicUrls.html";
    if ("service".equals(type)) {
      view = "redirect:/listServiceUrls.html";
    } else if ("measure".equals(type)) {
      view = "redirect:/listMeasureUrls.html";
    }
    return view;
  }

}
