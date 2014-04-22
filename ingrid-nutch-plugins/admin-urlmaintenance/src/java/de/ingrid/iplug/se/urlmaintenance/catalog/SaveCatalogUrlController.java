package de.ingrid.iplug.se.urlmaintenance.catalog;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.DatabaseExport;
import de.ingrid.iplug.se.urlmaintenance.EntityEditor;
import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;
import de.ingrid.nutch.admin.NavigationSelector;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "catalogUrlCommand" })
public class SaveCatalogUrlController extends NavigationSelector {
  private static final Log LOG = LogFactory.getLog(DatabaseExport.class);
  
  private final ICatalogUrlDao _catalogUrlDao;
  private final IMetadataDao _metadataDao;
  private final DatabaseExport _databaseExport;
  private TransactionService _transactionService;

  private static final Set<String> _supportedTypes = new HashSet<String>(Arrays
      .asList(new String[] { "topics", "service", "measure" }));

  @Autowired
  public SaveCatalogUrlController(ICatalogUrlDao catalogUrlDao,
          final IMetadataDao metadataDao, TransactionService transactionService,
          DatabaseExport databaseExport) {
    _catalogUrlDao      = catalogUrlDao;
    _metadataDao        = metadataDao;
    _databaseExport     = databaseExport;
    _transactionService = transactionService;
  }

  @InitBinder
  public void initBinder(final WebDataBinder binder) {
    binder.registerCustomEditor(Metadata.class, new EntityEditor(_metadataDao));
  }
  
  @RequestMapping(value = "/catalog/saveCatalogUrl.html", method = RequestMethod.GET)
  public String saveCatalogUrl(
      @ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand,
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand,
      Model model) {
    List<Metadata> metadatas = catalogUrlCommand.getMetadatas();
    String type = "topics";
    for (Metadata metadata : metadatas) {
      String metadataKey = metadata.getMetadataKey();
      String metadataValue = metadata.getMetadataValue();
      if ("datatype".equals(metadataKey)
          && _supportedTypes.contains(metadataValue)) {
        type = metadataValue;
        break;
      }
    }
    model.addAttribute("type", type);
    return "catalog/saveCatalogUrl";
  }

  @RequestMapping(value = "/catalog/saveCatalogUrl.html", method = RequestMethod.POST)
  public String postSaveCatalogUrl(
      @ModelAttribute("catalogUrlCommand") CatalogUrlCommand catalogUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {

    CatalogUrl catalogUrl = new CatalogUrl();
    if (catalogUrlCommand.getId() > -1) {
      catalogUrl = _catalogUrlDao.getById(catalogUrlCommand.getId());
      if (!catalogUrl.getUrl().equals(catalogUrlCommand.getUrl())) {
          // url has changed, mark DB Url as deleted
          catalogUrl.setDeleted(new Date());
          _catalogUrlDao.makePersistent(catalogUrl);
          
          // disconnect url from DB
          catalogUrl = new CatalogUrl();
      }
    }
    
    // check for new created URLs
    if (catalogUrl.getId() == null) {
        catalogUrl.setProvider(catalogUrlCommand.getProvider());
        catalogUrl.setUrl(catalogUrlCommand.getUrl());
        _catalogUrlDao.makePersistent(catalogUrl);
    }
    catalogUrl.setUpdated(new Date());
    
    Metadata altTitleMD = getMetadata(catalogUrl, "alt_title");
    int indexToReplace = -1;
    long id = -1;
    String redirectUrl = "redirect:/catalog/listTopicUrls.html";
    List<Metadata> metadatas = catalogUrlCommand.getMetadatas();
    for (Metadata metadata : metadatas) {
      String metadataKey = metadata.getMetadataKey();
      String metadataValue = metadata.getMetadataValue();
      if (metadataKey.equals("datatype") && metadataValue.equals("service")) {
        redirectUrl = "redirect:/catalog/listServiceUrls.html";
      } else if (metadataKey.equals("datatype")
          && metadataValue.equals("measure")) {
        redirectUrl = "redirect:/catalog/listMeasureUrls.html";
      } else if ("alt_title".equals(metadataKey)) {
          // update metadata inside a transaction
          if (altTitleMD != null) {
            altTitleMD.setMetadataValue(metadataValue);
            _transactionService.flipTransaction();
          } else {
            Metadata mdNew = new Metadata("alt_title", metadataValue);
            _metadataDao.makePersistent(mdNew);
            // since we do allow multiple same key-value pairs, we get the one
            // with the highest ID, which is the latest created one
            for (Metadata md : _metadataDao.getByKey("alt_title")) {
              if (md.getMetadataValue().equals(metadataValue)) {
                if (md.getId() > id)
                    id = md.getId();
              }
            }
            if (id != -1)
              indexToReplace = metadatas.indexOf(metadata);
            else {
              LOG.error("Metadata couldn't be created in database!");
            }
          }
      }
    }
    // replace alternative title (metadata)
    if (indexToReplace != -1)
      metadatas.set(indexToReplace, _metadataDao.getById(id));
    
    catalogUrl.setMetadatas(catalogUrlCommand.getMetadatas());
    _catalogUrlDao.makePersistent(catalogUrl);
    _transactionService.flipTransaction();
//    _databaseExport.exportCatalogUrls();
    
    return redirectUrl;
  }

  private Metadata getMetadata(CatalogUrl catalogUrl, String key) {
      for (Metadata metadata : catalogUrl.getMetadatas()) {
          if (key.equals(metadata.getMetadataKey())) {
              return metadata;
          }
      } 
      return null;
  }
}
