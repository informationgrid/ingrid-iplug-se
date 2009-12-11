package de.ingrid.iplug.se.urlmaintenance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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

// TODO rwe: clean up code!
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

  @Autowired
  public DatabaseExport(IStartUrlDao startUrlDao, ICatalogUrlDao catalogUrlDao, ILimitUrlDao limitUrlDao,
      IExcludeUrlDao excludeUrlDao) {
    _startUrlDao = startUrlDao;
    _catalogUrlDao = catalogUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
  }

  @Deprecated
  public void export(String urlType, File exportDir) throws IOException {

    exportDir = new File(exportDir, urlType);
    LOG.info("export urls into folder: " + exportDir);

    File start = new File(exportDir, "urls/start");
    start.mkdirs();
    File limit = new File(exportDir, "urls/limit");
    limit.mkdirs();
    File exclude = new File(exportDir, "urls/exclude");
    exclude.mkdirs();
    File metadata = new File(exportDir, "urls/metadata");
    metadata.mkdirs();

    // write urls
    if ("web".equalsIgnoreCase(urlType)) {
      // export web urls
      LOG.info("export web start urls");
      printUrls(new File(start, "urls.txt"), _startUrlDao);
      LOG.info("export web limit urls");
      printUrls(new File(limit, "urls.txt"), _limitUrlDao);
      LOG.info("export web exclude urls");
      printUrls(new File(exclude, "urls.txt"), _excludeUrlDao);
      LOG.info("export web metadata urls");
      printMetadatas(new File(metadata, "urls.txt"), _limitUrlDao);
    } else if ("catalog".equalsIgnoreCase(urlType)) {
      // export catalog urls
      LOG.info("export catalog start urls");
      printUrls(new File(start, "urls.txt"), _catalogUrlDao);
      LOG.info("export catalog limit urls");
      printUrls(new File(limit, "urls.txt"), _catalogUrlDao);
      LOG.info("export catalog metadata urls");
      printMetadatas(new File(metadata, "urls.txt"), _catalogUrlDao);
    } else {
      LOG.warn("configuration invalid. please configure 'url.type' to [web|catalog]");
    }
  }

  @Deprecated
  private void printMetadatas(File file, IDao<? extends Url> urlDao) throws FileNotFoundException {
    List<? extends Url> all = urlDao.getAll();
    PrintWriter printWriter = new PrintWriter(file);
    for (Url url : all) {
      List<Metadata> metadatas = null;
      if (url instanceof LimitUrl) {
        metadatas = ((LimitUrl) url).getMetadatas();
      } else if (url instanceof CatalogUrl) {
        metadatas = ((CatalogUrl) url).getMetadatas();
      }
      Provider provider = url.getProvider();
      metadatas.add(new Metadata("partner", provider.getPartner().getName()));
      metadatas.add(new Metadata("provider", provider.getName()));

      String urlString = url.getUrl();
      StringBuilder builder = new StringBuilder();
      builder.append(urlString + "\t");
      Map<String, List<String>> map = new HashMap<String, List<String>>();
      for (Metadata metadata : metadatas) {
        String metadataKey = metadata.getMetadataKey();
        String metadataValue = metadata.getMetadataValue();
        if (!map.containsKey(metadataKey)) {
          map.put(metadataKey, new ArrayList<String>());
        }
        List<String> list = map.get(metadataKey);
        list.add(metadataValue);
      }
      Set<String> keySet = map.keySet();
      for (String key : keySet) {
        builder.append(key + ":\t");
        List<String> list = map.get(key);
        for (String value : list) {
          builder.append(value + "\t");
        }
      }
      printWriter.println(builder.toString());
    }
    printWriter.close();

  }

  @Deprecated
  private void printUrls(File file, IDao<? extends Url> urlDao) throws FileNotFoundException {
    List<? extends Url> all = urlDao.getAll();
    PrintWriter printWriter = new PrintWriter(file);
    for (Url url : all) {
      String urlString = url.getUrl();
      printWriter.println(urlString);
    }
    printWriter.close();
  }

  List<String> metadataAsStringList(IDao<? extends Url> urlDao) {
    List<String> lines = new ArrayList<String>();

    for (Url url : urlDao.getAll()) {
      StringBuilder urlString = new StringBuilder();
      Provider provider = url.getProvider();
      
      urlString.append(url.getUrl()).append('\t');
      urlString.append("partner:\t").append(provider.getPartner().getName()).append('\t');
      urlString.append("provider:\t").append(provider.getName()).append('\t');
      
      List<Metadata> metadatas = null;
      if (url instanceof LimitUrl) {
        metadatas = ((LimitUrl) url).getMetadatas();
      } else if (url instanceof CatalogUrl) {
        metadatas = ((CatalogUrl) url).getMetadatas();
      }
      // summarize all values for one key
      Map<String, List<String>> key2Values = new HashMap<String, List<String>>();
      for (Metadata metadata : metadatas) {
        String metadataKey = metadata.getMetadataKey();
        String metadataValue = metadata.getMetadataValue();
        if (!key2Values.containsKey(metadataKey)) {
          key2Values.put(metadataKey, new ArrayList<String>());
        }
        key2Values.get(metadataKey).add(metadataValue);
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
      lines.add(urlString.toString());
    }
    return lines;
  }

  @Deprecated
  public IDao<? extends Url> getStartUrlDao() {
    return _startUrlDao;
  }

  @Deprecated
  public IDao<? extends Url> getLimitUrlDao() {
    return _limitUrlDao;
  }

  @Deprecated
  public IDao<? extends Url> getExcludeUrlDao() {
    return _excludeUrlDao;
  }

  @Deprecated
  public IDao<? extends Url> getCatalogUrlDao() {
    return _catalogUrlDao;
  }

  public void exportWebUrls() {
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

  public void exportCatalogUrls() {
    InterplugInCommunication<String> instanceForStringLists = InterplugInCommunication.getInstanceForStringLists();

    LOG.info("export catalog start/limit urls");
    instanceForStringLists.setObjectContent(CATALOG_URLS, getAsStringList(_catalogUrlDao));
    LOG.info("export catalog metadata urls");
    instanceForStringLists.setObjectContent(CATALOG_METADATA, metadataAsStringList(_catalogUrlDao));
  }

  @Deprecated
  private List<String> getAsStringList(Iterator<String> webMetadataIterator) {
    List<String> ret = new ArrayList<String>();
    while (webMetadataIterator.hasNext()) {
      String next = webMetadataIterator.next();
      ret.add(next);
    }
    return ret;
  }

  private List<String> getAsStringList(IDao<? extends Url> urlDao) {
    List<String> ret = new ArrayList<String>();

    for (Url url : urlDao.getAll()) {
      ret.add(url.getUrl());
    }
    return ret;
  }
}
