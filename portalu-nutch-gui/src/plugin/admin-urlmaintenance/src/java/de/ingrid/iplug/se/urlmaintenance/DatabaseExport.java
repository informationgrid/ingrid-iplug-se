package de.ingrid.iplug.se.urlmaintenance;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.weta.components.communication.util.PooledThreadExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.communication.InterplugInCommunication;
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

  public static final String CATALOG_START_URLS = "catalog-start-urls";

  public static final String CATALOG_LIMIT_URLS = "catalog-limit-urls";
  
  public static final String CATALOG_METADATA = "catalog-metadata";
  
  public static final String EXPORT_NOW = "export-now";
  
  public static final String WEB = "web";
  
  public static final String CATALOG = "catalog";
  
  private static final Pattern REGEXP_SPECIAL_CHARS = Pattern.compile("([\\\\*+\\[\\](){}\\$.?\\^|])");

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
  
  
  
  List<String> metadataAsStringList(IDao<? extends Url> urlDao, boolean checkForRegExpUrl) {
    List<String> lines = new ArrayList<String>();

    for (Url url : urlDao.getAll()) {
      // make sure we get the data fresh from the database
      if (TransactionService.getInstance() != null) {
          TransactionService.getInstance().refresh(url);
      }
      // skip deleted Urls and NULL urls (should not happen)
      if (url.getDeleted() != null || url.getUrl() == null) {
          continue;
      }
      StringBuilder urlString = new StringBuilder();
      Provider provider = url.getProvider();
      
      String urlStr = checkForRegularExpressions(url, checkForRegExpUrl);
      
      urlString.append(urlStr).append('\t');
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
          if (TransactionService.getInstance() != null) {
              TransactionService.getInstance().refresh(metadata);
          }
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
  
  private String checkForRegularExpressions(Url url, boolean checkForRegExpUrl) {
      String urlStr = url.getUrl().trim();
      if (checkForRegExpUrl) {
          if (urlStr.startsWith("/") && urlStr.endsWith("/")) {
              urlStr = urlStr.substring(1, urlStr.length() - 1);
          } else {
              URL uri;
            try {
                uri = new URL(urlStr);
                if (uri.getPath() != null || uri.getQuery() != null) {
                    Matcher match = REGEXP_SPECIAL_CHARS.matcher((uri.getPath() != null ? uri.getPath():"") + (uri.getQuery() != null ? "?"+uri.getQuery():""));
                    urlStr = uri.getProtocol() + "://" + uri.getHost() + (uri.getPort() > 0 ? uri.getPort(): "") + match.replaceAll("\\\\$1");
                }
            } catch (MalformedURLException e) {
                LOG.error("The url pattern: '" + urlStr + "' is not a valid url.");
            }
          }
      }
      return urlStr;
  }

  public synchronized void exportWebUrls() {
    InterplugInCommunication<String> instanceForStringLists = InterplugInCommunication.getInstanceForStringLists();

    LOG.info("export web start urls");
    instanceForStringLists.setObjectContent(WEB_START_URLS, getAsStringList(_startUrlDao, false));
    LOG.info("export web limit urls");
    instanceForStringLists.setObjectContent(WEB_LIMIT_URLS, getAsStringList(_limitUrlDao, true));
    LOG.info("export web exclude urls");
    instanceForStringLists.setObjectContent(WEB_EXCLUDE_URLS, getAsStringList(_excludeUrlDao, true));
    LOG.info("export web metadata urls");
    instanceForStringLists.setObjectContent(WEB_METADATA, metadataAsStringList(_limitUrlDao, true));
  }

  public synchronized void exportCatalogUrls() {
    InterplugInCommunication<String> instanceForStringLists = InterplugInCommunication.getInstanceForStringLists();

    LOG.info("export catalog start urls");
    instanceForStringLists.setObjectContent(CATALOG_START_URLS, getAsStringList(_catalogUrlDao, false));
    LOG.info("export catalog limit urls");
    instanceForStringLists.setObjectContent(CATALOG_LIMIT_URLS, getAsStringList(_catalogUrlDao, true));
    LOG.info("export catalog metadata urls");
    instanceForStringLists.setObjectContent(CATALOG_METADATA, metadataAsStringList(_catalogUrlDao, true));
  }

  private List<String> getAsStringList(IDao<? extends Url> urlDao, boolean checkForRegExpUrl) {
    List<String> ret = new ArrayList<String>();

    for (Url url : urlDao.getAll()) {
        // make sure we get the data fresh from the database
        TransactionService.getInstance().refresh(url);
        // get only not deleted URLs
        if (url.getDeleted() == null && url.getUrl() != null) {
            String urlStr = checkForRegularExpressions(url, checkForRegExpUrl);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Do export url: " + urlStr);
            }
            ret.add(urlStr);
        } else if (url.getUrl() == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Do NOT export NULL url with DB ID: " + url.getId());
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Do NOT export deleted url: " + url.getUrl());
            }
        }
    }
    return ret;
  }
}
