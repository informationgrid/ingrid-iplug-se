package de.ingrid.iplug.se;

import java.io.File;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.hadoop.fs.FileUtil;

import de.ingrid.iplug.se.IPlugSeOperatorFinder;

public class IPlugSeOperatorFinderTest extends TestCase {

  private File _folder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis()
      + IPlugSeOperatorFinder.class.getName());

  @Override
  protected void setUp() throws Exception {
    _folder.mkdir();
    new File(_folder, "Web-Urls/crawls/Crawl-123/index").mkdirs();
    new File(_folder, "Web-Urls/crawls/Crawl-123/search.done").createNewFile();
    new File(_folder, "Katalog-Urls/crawls/Crawl-123/index").mkdirs();
    new File(_folder, "Katalog-Urls/crawls/Crawl-123/search.done").createNewFile();
    new File(_folder, "Other-Urls/crawls/Crawl-123/index").mkdirs();
    new File(_folder, "AnOther-Urls/").mkdirs();

  }

  @Override
  protected void tearDown() throws Exception {
    assertTrue(FileUtil.fullyDelete(_folder));
  }

  public void testFindIndex() throws Exception {
    IPlugSeOperatorFinder finder = new IPlugSeOperatorFinder();
    List<File> indices = finder.findIndices(_folder);
    assertNotNull(indices);
    assertEquals(2, indices.size());
  }

  public void testFindIndexValues() throws Exception {
    IPlugSeOperatorFinder finder = new IPlugSeOperatorFinder();
    Set<String> providerSet = finder.findIndexValues(new File("ingrid-nutch-search/src/test/resources/instances"),
        "provider");
    Set<String> partnerSet = finder.findIndexValues(new File("ingrid-nutch-search/src/test/resources/instances"), "partner");
    assertEquals(1, providerSet.size());
    assertTrue(providerSet.contains("bw_lu"));
    assertEquals(5, partnerSet.size());
    assertTrue(providerSet.contains("bw_lu"));
  }

}
