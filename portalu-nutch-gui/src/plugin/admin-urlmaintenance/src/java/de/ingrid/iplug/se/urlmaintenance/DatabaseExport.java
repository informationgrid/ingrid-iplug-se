package de.ingrid.iplug.se.urlmaintenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.weta.components.communication.util.PooledThreadExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.communication.InterplugInCommunication;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.CatalogUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class DatabaseExport {

  private static final Log LOG = LogFactory.getLog(DatabaseExport.class);

  protected ICatalogUrlDao _catalogUrlDao;
  protected IStartUrlDao _startUrlDao;
  protected ILimitUrlDao _limitUrlDao;
  protected IExcludeUrlDao _excludeUrlDao;

  public static final String WEB_START_URLS = "web-start-urls";

  public static final String WEB_LIMIT_URLS = "web-limit-urls";

  public static final String WEB_EXCLUDE_URLS = "web-exclude-urls";

  public static final String WEB_METADATA = "web-metadata";

  public static final String CATALOG_URLS = "catalog-urls";

  public static final String CATALOG_METADATA = "catalog-metadata";
  
  public static final String EXPORT_NOW = "export-now";
  
  public static final String WEB = "web";
  
  public static final String CATALOG = "catalog";

  
  private class DatabaseExportPoller extends Thread {

    DatabaseExportPoller() {
        LOG.info("Database url export poller created.");
    }
      
    @Override
    public void run() {
        LOG.info("Database url export poller started.");
        while (!this.isInterrupted()) {
            InterplugInCommunication<String> instanceForStringLists = InterplugInCommunication.getInstanceForStringLists();
            List<String> exportNow = instanceForStringLists.getObjectContent(EXPORT_NOW);
            if (exportNow != null && !exportNow.isEmpty()) {
                if (exportNow.contains(WEB)) {
                    LOG.info("Request for WEB Url Export detected. Trigger export now.");
                    exportWebUrls();
                }
                if (exportNow.contains(CATALOG)) {
                    LOG.info("Request for CATALOG Url Export detected. Trigger export now.");
                    exportCatalogUrls();
                }
                exportNow.clear();
                instanceForStringLists.setObjectContent(EXPORT_NOW, exportNow);
            }
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                LOG.error("Database url export poller was interrupted while sleeping!");
            }
        }
        LOG.info("Database url export poller was interrupted!");
    }
      
  }
  
  @Autowired
  public DatabaseExport(IStartUrlDao startUrlDao, ICatalogUrlDao catalogUrlDao, ILimitUrlDao limitUrlDao,
      IExcludeUrlDao excludeUrlDao) {
    _startUrlDao = startUrlDao;
    _catalogUrlDao = catalogUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
    
    PooledThreadExecutor.execute(new DatabaseExportPoller());
  }
  
  
  
  List<String> metadataAsStringList(IDao<? extends Url> urlDao) {
    List<String> lines = new ArrayList<String>();

    for (Url url : urlDao.getAll()) {
      // make sure we get the data fresh from the database
      TransactionService.getInstance().refresh(url);
      // skip deleted Urls
      if (url.getDeleted() != null) {
          continue;
      }
      StringBuilder urlString = new StringBuilder();
      Provider provider = url.getProvider();
      
      urlString.append(url.getUrl()).append('\t');
      urlString.append("partner:\t").append(provider.getPartner().getShortName()).append('\t');
      urlString.append("provider:\t").append(provider.getShortName()).append('\t');
      
      List<Metadata> metadatas = null;
      if (url instanceof LimitUrl) {
        metadatas = ((LimitUrl) url).getMetadatas();
      } else if (url instanceof CatalogUrl) {
        metadatas = ((CatalogUrl) url).getMetadatas();
      }
      // summarize all values for one key
      Map<String, List<String>> key2Values = new HashMap<String, List<String>>();
      for (Metadata metadata : metadatas) {
        // make sure we get the data fresh from the database
        TransactionService.getInstance().refresh(metadata);
        String metadataKey = metadata.getMetadataKey();
        String metadataValue = metadata.getMetadataValue();
        if (!key2Values.containsKey(metadataKey)) {
          key2Values.put(metadataKey, new ArrayList<String>());
        }
        key2Values.get(metadataKey).add(metadataValue);
      }
      if (LOG.isDebugEnabled()) {
          LOG.debug("Found metadata for '" + url.getUrl() + "' :"+ metadatas.toString());
      }
      
      // for each metadata append its information in form: '<key>:\t<value>\t[<value>\t]..' 
      for(Entry<String, List<String>> entry : key2Values.entrySet()){
        String key = entry.getKey();
        List<String> values = entry.getValue();
        
        urlString.append(key).append(":\t");
        for (String value : values) {
          urlString.append(value).append('\t');
        }
      }
      if (LOG.isDebugEnabled()) {
          LOG.debug("Export metadata: " + urlString.toString());
      }
      lines.add(urlString.toString());
    }
    return lines;
  }

  public synchronized void exportWebUrls() {
    InterplugInCommunication<String> instanceForStringLists = InterplugInCommunication.getInstanceForStringLists();

    LOG.info("export web start urls");
    instanceForStringLists.setObjectContent(WEB_START_URLS, getAsStringList(_startUrlDao));
    LOG.info("export web limit urls");
    instanceForStringLists.setObjectContent(WEB_LIMIT_URLS, getAsStringList(_limitUrlDao));
    LOG.info("export web exclude urls");
    instanceForStringLists.setObjectContent(WEB_EXCLUDE_URLS, getAsStringList(_excludeUrlDao));
    LOG.info("export web metadata urls");
    instanceForStringLists.setObjectContent(WEB_METADATA, metadataAsStringList(_limitUrlDao));
  }

  public synchronized void exportCatalogUrls() {
    InterplugInCommunication<String> instanceForStringLists = InterplugInCommunication.getInstanceForStringLists();

    LOG.info("export catalog start/limit urls");
    instanceForStringLists.setObjectContent(CATALOG_URLS, getAsStringList(_catalogUrlDao));
    LOG.info("export catalog metadata urls");
    instanceForStringLists.setObjectContent(CATALOG_METADATA, metadataAsStringList(_catalogUrlDao));
  }

  private List<String> getAsStringList(IDao<? extends Url> urlDao) {
    List<String> ret = new ArrayList<String>();

    for (Url url : urlDao.getAll()) {
        // make sure we get the data fresh from the database
        TransactionService.getInstance().refresh(url);
        // get only not deleted URLs
        if (url.getDeleted() == null) {
            if (LOG.isDebugEnabled()) {
                LOG.info("Do export url: " + url.getUrl());
            }
            ret.add(url.getUrl());
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.info("Do NOT export deleted url: " + url.getUrl());
            }
        }
    }
    return ret;
  }
}
