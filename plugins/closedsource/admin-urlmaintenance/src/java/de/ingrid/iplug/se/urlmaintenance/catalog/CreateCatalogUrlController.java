package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class CreateCatalogUrlController {

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

  @RequestMapping(value = "/createCatalogUrl.html", method = RequestMethod.GET)
  public String editCatalogUrl(Model model,
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand,
      @RequestParam(value = "id", required = false) Long id,
      @RequestParam(value = "type", required = true) String type) {

    Map<String, List<Metadata>> metadatas = new HashMap<String, List<Metadata>>();
    if (id != null) {
      // load url and fill out command
      CatalogUrl byId = _catalogUrlDao.getById(id);
      catalogUrlCommand.setId(byId.getId());
      catalogUrlCommand.setUrl(byId.getUrl());
      catalogUrlCommand.setMetadatas(byId.getMetadatas());
      catalogUrlCommand.setProvider(byId.getProvider());
      catalogUrlCommand.setCreated(byId.getCreated());
    } else {
      // create new command
      if (type.equals("topics")) {
        Metadata metadata = _metadataDao.getByKeyAndValue("datatype", "topics");
        Metadata defaultMetadata = _metadataDao.getByKeyAndValue("datatype",
            "default");
        catalogUrlCommand.addMetadata(metadata);
        catalogUrlCommand.addMetadata(defaultMetadata);

        // push more metadatas into vew
        // push all topics into view
        metadatas.put("topics", _metadataDao.getByKey("topics"));
        // push funct category into view
        metadatas
            .put("funct_category", _metadataDao.getByKey("funct_category"));
      } else if (type.equals("service")) {
        Metadata metadata = _metadataDao
            .getByKeyAndValue("datatype", "service");
        Metadata defaultMetadata = _metadataDao.getByKeyAndValue("datatype",
            "default");
        catalogUrlCommand.addMetadata(defaultMetadata);

        // push more metadatas into vew
        // push all rubrics into view
        metadatas.put("rubric", _metadataDao.getByKey("service"));
      } else if (type.equals("measure")) {
        Metadata metadata = _metadataDao
            .getByKeyAndValue("datatype", "measure");
        Metadata defaultMetadata = _metadataDao.getByKeyAndValue("datatype",
            "default");
        catalogUrlCommand.addMetadata(metadata);
        catalogUrlCommand.addMetadata(defaultMetadata);

        // push more metadatas into vew
        // push all rubrics into view
        metadatas.put("rubric", _metadataDao.getByKey("measure"));
      }
      model.addAttribute("metadatas", metadatas);
    }
    return "catalog/createCatalogUrl";
  }

  @RequestMapping(value = "/createCatalogUrl.html", method = RequestMethod.POST)
  public String anotherPostEditCatalogUrl(
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand) {
    return "redirect:/saveCatalogUrl.html";
  }
}
