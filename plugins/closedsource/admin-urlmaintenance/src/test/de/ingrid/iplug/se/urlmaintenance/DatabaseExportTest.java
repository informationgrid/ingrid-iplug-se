package de.ingrid.iplug.se.urlmaintenance;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.util.NutchConfiguration;

public class DatabaseExportTest extends TestCase {

  private File _tmpFolder = new File(System.getProperty("java.io.tmpdir"),
      DatabaseExportTest.class.getName() + "-" + System.currentTimeMillis());

  @Override
  protected void setUp() throws Exception {
    assertTrue(_tmpFolder.mkdirs());
  }

  @Override
  protected void tearDown() throws Exception {
    // assertTrue(FileUtil.fullyDelete(_tmpFolder));
  }

  public void testExport() throws Exception {
    DatabaseExport databaseExport = new DatabaseExport();
    Configuration create = NutchConfiguration.create();
    create.set("url.type", "catalog");
    databaseExport.setConf(create);
    databaseExport.preCrawl(new Path(_tmpFolder.getAbsolutePath()));
    
    
  }
}
