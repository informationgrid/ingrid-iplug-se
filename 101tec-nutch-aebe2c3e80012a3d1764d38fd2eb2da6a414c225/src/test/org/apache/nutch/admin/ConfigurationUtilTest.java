package org.apache.nutch.admin;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;

public class ConfigurationUtilTest extends TestCase {

  private File _folder = new File(System.getProperty("java.io.tmpdir"),
          ConfigurationUtilTest.class.getSimpleName()
                  + System.currentTimeMillis());

  @Override
  protected void setUp() throws Exception {
    assertTrue(_folder.mkdir());
  }

  @Override
  protected void tearDown() throws Exception {
    assertTrue(FileUtil.fullyDelete(_folder));
  }

  public void testCreate() throws Exception {
    ConfigurationUtil configurationUtil = new ConfigurationUtil(_folder);
    Configuration configuration = configurationUtil
            .createNewConfiguration("foo");
    assertNotNull(configuration);
    String folderName = configuration.get("nutch.instance.folder");
    assertEquals(folderName, new File(_folder, "foo").getCanonicalPath());
  }

  public void testLoad() throws Exception {
    ConfigurationUtil configurationUtil = new ConfigurationUtil(_folder);
    configurationUtil.createNewConfiguration("bar");

    Configuration configuration = configurationUtil.loadConfiguration("bar");
    assertNotNull(configuration);
    String folderName = configuration.get("nutch.instance.folder");
    assertEquals(folderName, new File(_folder, "bar").getCanonicalPath());
  }

  public void testLoadAll() throws Exception {
    ConfigurationUtil configurationUtil = new ConfigurationUtil(_folder);
    configurationUtil.createNewConfiguration("foo");
    configurationUtil.createNewConfiguration("bar");
    configurationUtil.createNewConfiguration("foobar");

    List<Configuration> list = configurationUtil.loadAll();
    assertEquals(3, list.size());
  }

  public void testExists() throws Exception {
    ConfigurationUtil configurationUtil = new ConfigurationUtil(_folder);
    configurationUtil.createNewConfiguration("foo");
    assertTrue(configurationUtil.existsConfiguration("foo"));
  }

}
