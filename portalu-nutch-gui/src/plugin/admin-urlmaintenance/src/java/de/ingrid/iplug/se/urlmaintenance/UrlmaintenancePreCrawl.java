package de.ingrid.iplug.se.urlmaintenance;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.IPreCrawl;

import de.ingrid.iplug.se.communication.InterplugInCommunication;

public class UrlmaintenancePreCrawl implements IPreCrawl {

  private Configuration _conf;

  private static final Log LOG = LogFactory.getLog(UrlmaintenancePreCrawl.class);

  private FileSystem _fileSystem;
  private final String _lineSeparator;
  
  public UrlmaintenancePreCrawl() {
    super();
    _lineSeparator = System.getProperty("line.separator");
  }

  @Override
  public void preCrawl(Path crawlDir) throws IOException {

    String urlType = _conf.get("url.type");
    uploadUrlsFromCommunicationObject(urlType, crawlDir);
  }

  private void uploadUrlsFromCommunicationObject(String urlType, Path crawlDir) throws IOException {
    LOG.info("Sync urls of type '" + urlType + "' from InterplugInCommunication...");
    if (urlType.equals("web")) {
      writeFromCommunicationObjectIntoFile(DatabaseExport.WEB_START_URLS, new Path(crawlDir, "urls/start/urls.txt"));
      writeFromCommunicationObjectIntoFile(DatabaseExport.WEB_LIMIT_URLS, new Path(crawlDir, "urls/limit/urls.txt"));
      writeFromCommunicationObjectIntoFile(DatabaseExport.WEB_EXCLUDE_URLS, new Path(crawlDir, "urls/exclude/urls.txt"));
      writeFromCommunicationObjectIntoFile(DatabaseExport.WEB_METADATA, new Path(crawlDir, "urls/metadata/urls.txt"));
    } else if (urlType.equals("catalog")) {
      writeFromCommunicationObjectIntoFile(DatabaseExport.CATALOG_URLS, new Path(crawlDir, "urls/start/urls.txt"));
      writeFromCommunicationObjectIntoFile(DatabaseExport.CATALOG_URLS, new Path(crawlDir, "urls/limit/urls.txt"));
      writeFromCommunicationObjectIntoFile(DatabaseExport.CATALOG_METADATA,
          new Path(crawlDir, "urls/metadata/urls.txt"));
    } else {
      throw new IllegalArgumentException("Internal error: The given parameter 'urlType'='" + urlType + "' is invalid.");
    }
    LOG.info("Sync urls of type '" + urlType + "' from InterplugInCommunication...OK");
  }

  @Override
  public Configuration getConf() {
    return _conf;
  }

  @Override
  public void setConf(Configuration conf) {
    _conf = conf;
    try {
      _fileSystem = FileSystem.get(conf);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void writeFromCommunicationObjectIntoFile(String key, Path uploadPath) throws IOException {
    InterplugInCommunication<String> instanceForStringLists = InterplugInCommunication.getInstanceForStringLists();
    List<String> lines = instanceForStringLists.getObjectContent(key);
    
    /* save to local fs */
    String tempKeyFile = new String("file.tmp");
    try {
        FileOutputStream fos = new FileOutputStream(tempKeyFile);
        OutputStreamWriter osw = new OutputStreamWriter(fos , "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        for (String line : lines) {
            bw.write(line);
            bw.write(_lineSeparator);
        }
        bw.close();
        osw.close();
        fos.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    /* put local file to hdfs */
    Path localKeyFilePath = new Path(tempKeyFile);
    _fileSystem.copyFromLocalFile(localKeyFilePath, uploadPath);
    
    /* remove local file*/
    _fileSystem.delete(localKeyFilePath, true);
  }
  
}
