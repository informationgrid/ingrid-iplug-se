package de.ingrid.iplug.se.urlmaintenance;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.IPreCrawl;

public class UrlmaintenancePreCrawl implements IPreCrawl {

  private Configuration _conf;

  private static final Log LOG = LogFactory
      .getLog(UrlmaintenancePreCrawl.class);

  private FileSystem _fileSystem;

  @Override
  public void preCrawl(Path crawlDir) throws IOException {
    String tmp = System.getProperty("java.io.tmpdir");
    File tmpFolder = new File(tmp, "portal-u-" + DatabaseExport.class.getName());

    String urlType = _conf.get("url.type");
    File inDir = new File(tmpFolder, urlType);
    File start = new File(inDir, "urls/start");
    File limit = new File(inDir, "urls/limit");
    File exclude = new File(inDir, "urls/exclude");
    File metadata = new File(inDir, "urls/metadata");

    // upload urls
    upload(_fileSystem, start, crawlDir);
    upload(_fileSystem, limit, crawlDir);
    upload(_fileSystem, exclude, crawlDir);
    upload(_fileSystem, metadata, crawlDir);
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

  private void upload(FileSystem fileSystem, File urlFolder, Path crawlDir)
      throws IOException {
    if (!new File(urlFolder.getAbsolutePath(), "urls.txt").exists()) {
      return;
    }
    Path urlFile = new Path(urlFolder.getAbsolutePath(), "urls.txt");
    Path uploadPath = new Path(crawlDir, "urls/" + urlFolder.getName()
        + "/urls.txt");
    LOG.info("upload url file [" + urlFile + "] to hdfs [" + uploadPath + "]");
    fileSystem.copyFromLocalFile(false, true, urlFile, uploadPath);
  }

}
