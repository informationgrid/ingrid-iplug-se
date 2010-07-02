package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
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
import de.ingrid.iplug.se.urlmaintenance.validation.CatalogUrlCommandValidator;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "catalogUrlCommand" })
public class CreateCatalogUrlController extends NavigationSelector {

  private final ICatalogUrlDao _catalogUrlDao;
  private final IMetadataDao _metadataDao;
  
  private Metadata oldAltTitleMD = null;

  private final CatalogUrlCommandValidator _validator;

  @Autowired
  public CreateCatalogUrlController(final ICatalogUrlDao catalogUrlDao,
          final IMetadataDao metadataDao,
          final CatalogUrlCommandValidator validator) {
    _catalogUrlDao = catalogUrlDao;
    _metadataDao   = metadataDao;
    _validator     = validator;
  }

  @InitBinder
  public void initBinder(final WebDataBinder binder) {
    binder.registerCustomEditor(Metadata.class, new EntityEditor(_metadataDao));
  }

  @ModelAttribute("langs")
  public List<Metadata> injectLang() {
    return _metadataDao.getByKey("lang");
  }
  
  @ModelAttribute("metadatas")
  public Map<String, List<Metadata>> injectMetadatas(
      @RequestParam(value = "type", required = true) final String type) {
    final Map<String, List<Metadata>> metadatas = new HashMap<String, List<Metadata>>();
    if (type.equals("topics")) {
      // push all topics into view
      metadatas.put("topic", _metadataDao.getByKey("topic"));
      // push funct category into view
      metadatas.put("funct_category", _metadataDao.getByKey("funct_category"));
    } else if (type.equals("service")) {
      // push all rubrics into view
      metadatas.put("rubric", _metadataDao.getByKey("service"));
    } else if (type.equals("measure")) {
      // push all rubrics into view
      metadatas.put("rubric", _metadataDao.getByKey("measure"));
    }
    metadatas.put("alt_title", _metadataDao.getByKey("alt_title"));
    return metadatas;
  }

  @RequestMapping(value = { "/catalog/createCatalogUrl.html",
      "/catalog/editCatalogUrl.html" }, method = RequestMethod.GET)
  public String editCatalogUrl(final Model model,
      @ModelAttribute("catalogUrlCommand") final CatalogUrlCommand catalogUrlCommand,
      @RequestParam(value = "id", required = false) final Long id,
      @RequestParam(value = "type", required = true) final String type) {

    if (id != null) {
      // load url and fill out command
      final CatalogUrl byId = _catalogUrlDao.getById(id);
      catalogUrlCommand.setId(byId.getId());
      catalogUrlCommand.setUrl(byId.getUrl());
      catalogUrlCommand.setMetadatas(byId.getMetadatas());
      catalogUrlCommand.setProvider(byId.getProvider());
      catalogUrlCommand.setCreated(byId.getCreated());
      catalogUrlCommand.setUpdated(byId.getUpdated());
      
    }
    model.addAttribute("type", type);
    oldAltTitleMD = getMetadataValue(catalogUrlCommand.getMetadatas(), "alt_title");
    String altTitle = oldAltTitleMD != null ? oldAltTitleMD.getMetadataValue() : "";
    model.addAttribute("altTitle", altTitle);
    return "catalog/createCatalogUrl";
  }

  @RequestMapping(value = "/catalog/createCatalogUrl.html", method = RequestMethod.POST)
  public String anotherPostEditCatalogUrl(final Model model,
      @ModelAttribute("catalogUrlCommand") final CatalogUrlCommand catalogUrlCommand, final Errors errors,
      @RequestParam(value = "type", required = true) final String type,
      @RequestParam(value = "altTitle", required = true) final String altTitle) {
    final Metadata defaultMetadata = _metadataDao.getByKeyAndValue("datatype", "default");
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
    
    // update alternative title but don't make it persistent yet
    if (oldAltTitleMD != null) {
        oldAltTitleMD.setMetadataValue(altTitle);
        catalogUrlCommand.addMetadata(oldAltTitleMD);
    } else {
        Metadata altTitleMeta = new Metadata("alt_title", altTitle);
        catalogUrlCommand.addMetadata(altTitleMeta);
    }
    
    _validator.setUsedDatatype(type);
    if (_validator.validate(errors).hasErrors()) {
        return editCatalogUrl(model, catalogUrlCommand, null, type);
    }
    return "redirect:/catalog/saveCatalogUrl.html";
  }
  
  private Metadata getMetadataValue(List<Metadata> metadatas, String key) throws NumberFormatException {
      Metadata ret = null;
      for (Metadata metadata : metadatas) {
          if (metadata.getMetadataKey().equals(key)) {
              ret = metadata;//.getMetadataValue();
              break;
          }
      }
      return ret;
  }
}
