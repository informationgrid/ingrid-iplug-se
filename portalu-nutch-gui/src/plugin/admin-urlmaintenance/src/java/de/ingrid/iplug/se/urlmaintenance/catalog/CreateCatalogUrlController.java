package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.EntityEditor;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "catalogUrlCommand" })
public class CreateCatalogUrlController extends NavigationSelector {

  private final ICatalogUrlDao _catalogUrlDao;
  private final IMetadataDao _metadataDao;

  @Autowired
  public CreateCatalogUrlController(ICatalogUrlDao catalogUrlDao,
      IMetadataDao metadataDao) {
    _catalogUrlDao = catalogUrlDao;
    _metadataDao = metadataDao;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(Metadata.class, new EntityEditor(_metadataDao));
  }

  @ModelAttribute("metadatas")
  public Map<String, List<Metadata>> injectMetadatas(
      @RequestParam(value = "type", required = true) String type) {
    Map<String, List<Metadata>> metadatas = new HashMap<String, List<Metadata>>();
    if (type.equals("topics")) {
      // push all topics into view
      metadatas.put("topics", _metadataDao.getByKey("topics"));
      // push funct category into view
      metadatas.put("funct_category", _metadataDao.getByKey("funct_category"));
    } else if (type.equals("service")) {
      // push all rubrics into view
      metadatas.put("rubric", _metadataDao.getByKey("service"));
    } else if (type.equals("measure")) {
      // push all rubrics into view
      metadatas.put("rubric", _metadataDao.getByKey("measure"));
    }
    return metadatas;
  }

  @RequestMapping(value = { "/createCatalogUrl.html", "/editCatalogUrl.html" }, method = RequestMethod.GET)
  public String editCatalogUrl(Model model,
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand,
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "type", required = true) String type) {

    if (id != null) {
      // load url and fill out command
      CatalogUrl byId = _catalogUrlDao.getById(id);
      catalogUrlCommand.setId(byId.getId());
      catalogUrlCommand.setUrl(byId.getUrl());
      catalogUrlCommand.setMetadatas(byId.getMetadatas());
      catalogUrlCommand.setProvider(byId.getProvider());
      catalogUrlCommand.setCreated(byId.getCreated());
      catalogUrlCommand.setUpdated(byId.getUpdated());
    }
    model.addAttribute("type", type);
    return "catalog/createCatalogUrl";
  }

  @RequestMapping(value = "/createCatalogUrl.html", method = RequestMethod.POST)
  public String anotherPostEditCatalogUrl(
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand,
      @RequestParam(value = "type", required = true) String type) {

    Metadata defaultMetadata = _metadataDao.getByKeyAndValue("datatype",
        "default");
    catalogUrlCommand.addMetadata(defaultMetadata);
    Metadata datatypeMetadata = null;
    if (type.equals("topics")) {
      datatypeMetadata = _metadataDao.getByKeyAndValue("datatype", "topics");
    } else if (type.equals("service")) {
      datatypeMetadata = _metadataDao.getByKeyAndValue("datatype", "service");
    } else if (type.equals("measure")) {
      datatypeMetadata = _metadataDao.getByKeyAndValue("datatype", "measure");
    }
    catalogUrlCommand.addMetadata(datatypeMetadata);
    return "redirect:/saveCatalogUrl.html";
  }
}
