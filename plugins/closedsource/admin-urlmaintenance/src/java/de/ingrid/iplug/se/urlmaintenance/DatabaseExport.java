package de.ingrid.iplug.se.urlmaintenance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.IPreCrawl;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.CatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.LimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.StartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class DatabaseExport implements IPreCrawl {

  private Configuration _conf;
  private ICatalogUrlDao _catalogUrlDao;
  private IStartUrlDao _startUrlDao;
  private ILimitUrlDao _limitUrlDao;
  private IExcludeUrlDao _excludeUrlDao;
  private String _urlType;
  private static final Log LOG = LogFactory.getLog(DatabaseExport.class);

  @Override
  public void preCrawl(Path crawlDir) throws IOException {

    LOG.info("prepare crawl folder: " + crawlDir);

    FileSystem fileSystem = FileSystem.get(_conf);

    // write urls in local tmp folder
    String tmp = System.getProperty("java.io.tmpdir");
    File tmpFolder = new File(tmp, DatabaseExport.class.getName() + "-"
        + System.currentTimeMillis());
    File start = new File(tmpFolder, "urls/start");
    start.mkdirs();
    File limit = new File(tmpFolder, "urls/limit");
    limit.mkdirs();
    File exclude = new File(tmpFolder, "urls/exclude");
    exclude.mkdirs();
    File metadata = new File(tmpFolder, "urls/metadata");
    metadata.mkdirs();

    // write urls
    if ("web".equalsIgnoreCase(_urlType)) {
      // export web urls
      LOG.info("export web start urls");
      printUrls(new File(start, "urls.txt"), _startUrlDao);
      LOG.info("export web limit urls");
      printUrls(new File(limit, "urls.txt"), _limitUrlDao);
      LOG.info("export web exclude urls");
      printUrls(new File(exclude, "urls.txt"), _excludeUrlDao);
      LOG.info("export web metadata urls");
      printMetadatas(new File(metadata, "urls.txt"), _limitUrlDao);
    } else if ("catalog".equalsIgnoreCase(_urlType)) {
      // export catalog urls
      LOG.info("export catalog start urls");
      printUrls(new File(start, "urls.txt"), _catalogUrlDao);
      LOG.info("export catalog limit urls");
      printUrls(new File(limit, "urls.txt"), _catalogUrlDao);
      LOG.info("export catalog metadata urls");
      printMetadatas(new File(metadata, "urls.txt"), _catalogUrlDao);
    } else {
      LOG
          .warn("configuration invalid. please configure 'url.type' to [web|catalog]");
    }

    // upload urls
    upload(fileSystem, start, crawlDir);
    upload(fileSystem, limit, crawlDir);
    upload(fileSystem, exclude, crawlDir);
    upload(fileSystem, metadata, crawlDir);
  }

  private void upload(FileSystem fileSystem, File urlFolder, Path crawlDir)
      throws IOException {
    if (!new File(urlFolder.getAbsolutePath(), "urls.txt").exists()) {
      return;
    }
    Path urlFile = new Path(urlFolder.getAbsolutePath(), "urls.txt");
    Path uploadPath = new Path(crawlDir, "urls/" + urlFolder.getName()
        + "/urls.txt");
    LOG.info("upload url file [" + urlFile + "] to hdfs [" + uploadPath + "]");
    fileSystem.copyFromLocalFile(true, true, urlFile, uploadPath);
  }

  @Override
  public Configuration getConf() {
    return _conf;
  }

  @Override
  public void setConf(Configuration conf) {
    _conf = conf;
    // instantiate UrlDao's without spring :(
    TransactionService transactionService = new TransactionService();
    _catalogUrlDao = new CatalogUrlDao(transactionService);
    _startUrlDao = new StartUrlDao(transactionService);
    _limitUrlDao = new LimitUrlDao(transactionService);
    _excludeUrlDao = new ExcludeUrlDao(transactionService);
    _urlType = conf.get("url.type");
  }

  private void printMetadatas(File file, IDao<? extends Url> urlDao)
      throws FileNotFoundException {
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
      Partner partner = provider.getPartner();
      metadatas.add(new Metadata("partner", partner.getName()));
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

  private void printUrls(File file, IDao<? extends Url> urlDao)
      throws FileNotFoundException {
    List<? extends Url> all = urlDao.getAll();
    PrintWriter printWriter = new PrintWriter(file);
    for (Url url : all) {
      String urlString = url.getUrl();
      printWriter.println(urlString);
    }
    printWriter.close();
  }

}
