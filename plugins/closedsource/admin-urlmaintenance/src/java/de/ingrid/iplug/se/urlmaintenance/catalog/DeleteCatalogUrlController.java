package de.ingrid.iplug.se.urlmaintenance.catalog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;

@Controller
public class DeleteCatalogUrlController {
  
  private ICatalogUrlDao _catalogUrlDao;

  @Autowired
  public DeleteCatalogUrlController(ICatalogUrlDao catalogUrlDao){
    _catalogUrlDao = catalogUrlDao;
    
  }
  
  @RequestMapping(method = RequestMethod.POST, value = "deleteCatalogUrl.html")
  public String deleteCatalogUrl(@RequestParam(value = "id", required = true) final Long id, 
      @RequestParam(value = "type", required = false) final String type){
    
    CatalogUrl url = _catalogUrlDao.getById(id);
    if(url != null){
      //TODO: check security user is allowed
      _catalogUrlDao.makeTransient(url);
    }
    
    String view = "/listTopicUrls.html";
    if(type != null && "service".equals(type)){
      view = "/listServiceUrls.html";
    }else if(type != null && "measure".equals(type)){
      view = "/listMeasureUrls.html";
    }
    return "redirect:"+view ;
  }

}
