package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.Date;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.iplug.se.urlmaintenance.DatabaseExport;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;

@Controller
public class DeleteCatalogUrlController extends NavigationSelector {

  private ICatalogUrlDao _catalogUrlDao;
  private final DatabaseExport _databaseExport;

  @Autowired
  public DeleteCatalogUrlController(ICatalogUrlDao catalogUrlDao, DatabaseExport databaseExport) {
    _catalogUrlDao = catalogUrlDao;
    _databaseExport = databaseExport;

  }

  @RequestMapping(method = RequestMethod.POST, value = "/catalog/deleteCatalogUrl.html")
  public String deleteCatalogUrl(
      @RequestParam(value = "id", required = true) final Long id,
      @RequestParam(value = "type", required = false) final String type) {

    CatalogUrl url = _catalogUrlDao.getById(id);
    if (url != null) {
        url.setDeleted(new Date());
        _catalogUrlDao.makePersistent(url);
        _databaseExport.exportCatalogUrls();
    }

    String view = "redirect:/catalog/listTopicUrls.html";
    if ("service".equals(type)) {
      view = "redirect:/catalog/listServiceUrls.html";
    } else if ("measure".equals(type)) {
      view = "redirect:/catalog/listMeasureUrls.html";
    }
    return view;
  }

}
