package de.ingrid.iplug.se.searcher;

import static org.mockito.Mockito.when;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.nutch.admin.searcher.MultipleSearcher;
import org.apache.nutch.admin.searcher.TestSearcherFactory;
import org.apache.nutch.util.NutchConfiguration;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ingrid.iplug.se.util.TestUtil;
import de.ingrid.iplug.util.TimeProvider;

public class DirectoryScanningSearcherFactoryTest extends TestCase {

  private File _folder = new File("build/junit", TestSearcherFactory.class.getName());
  @Mock
  private TimeProvider _timeProvider;

  @Override
  protected void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    if (_folder.exists()) {
      FileUtils.forceDelete(_folder);
    }
    assertTrue(_folder.mkdirs());
    assertTrue(new File(_folder, "general").mkdirs());
    // create test crawl folder
    assertTrue(new File(_folder, "testCrawl").mkdirs());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.00").mkdirs());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.00/search.done").createNewFile());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.01").mkdirs());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.01/search.done").createNewFile());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.02").mkdirs());
    // create second test crawl folder
    assertTrue(new File(_folder, "testCrawl2").mkdirs());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.00").mkdirs());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.00/search.done").createNewFile());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.01").mkdirs());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.01/search.done").createNewFile());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.02").mkdirs());
    when(_timeProvider.getTime()).thenReturn(1L);
  }

  @Override
  protected void tearDown() throws Exception {
    assertTrue(FileUtil.fullyDelete(_folder));
  }

  public void testInstanceCreate() throws Exception {
    Configuration configuration = createConfiguration("testCrawl");
    DirectoryScanningSearcherFactory instance = new DirectoryScanningSearcherFactory(configuration, _timeProvider);
    MultipleSearcher searcher = instance.get();
    assertEquals(2, searcher.getNutchBeanLength());
  }

  public void testInstanceReloadCreate() throws Exception {
    Configuration configuration = createConfiguration("testCrawl");
    DirectoryScanningSearcherFactory instance = new DirectoryScanningSearcherFactory(configuration, _timeProvider);
    MultipleSearcher searcher = instance.get();
    assertEquals(2, searcher.getNutchBeanLength());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.02/search.done").createNewFile());
    searcher = instance.get();
    assertEquals(2, searcher.getNutchBeanLength());
    instance.reload();
    searcher = instance.get();
    assertEquals(3, searcher.getNutchBeanLength());
  }

  public void testGeneralReloadCreate() throws Exception {
    Configuration configuration = createConfiguration("general");
    DirectoryScanningSearcherFactory instance = new DirectoryScanningSearcherFactory(configuration, _timeProvider);
    MultipleSearcher searcher = instance.get();
    assertEquals(4, searcher.getNutchBeanLength());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.02/search.done").createNewFile());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.02/search.done").createNewFile());
    searcher = instance.get();
    assertEquals(4, searcher.getNutchBeanLength());
    instance.reload();
    searcher = instance.get();
    assertEquals(6, searcher.getNutchBeanLength());
  }

  public void testRescanDirectoryFolder() throws Exception {
    Configuration configuration = createConfiguration("testCrawl");
    DirectoryScanningSearcherFactory instance = new DirectoryScanningSearcherFactory(configuration, _timeProvider);
    // verify 2 index directories are detected
    assertEquals(2, instance.get().getNutchBeanLength());

    // add new indexed directory and verify that no reload was done immediately
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.02/search.done").createNewFile());
    assertEquals(2, instance.get().getNutchBeanLength());

    // wait until timeout.. By the reload the Factory must now provide a
    // searcher for 3 indexed directories
    when(_timeProvider.getTime()).thenReturn(1L + DirectoryScanningSearcherFactory.NEXTRELOADWAITINGTIME);
    MultipleSearcher multipleSearcher = instance.get();
    assertEquals(3, multipleSearcher.getNutchBeanLength());

    // Test that no reload will be performed when the time elapsed next
    // NEXTRELOADWAITINGTIME and the directory structure has not changed.
    when(_timeProvider.getTime()).thenReturn(1L + (2L * DirectoryScanningSearcherFactory.NEXTRELOADWAITINGTIME));
    assertSame(multipleSearcher, instance.get());
  }

  private Configuration createConfiguration(String nutchInstanceSubFolder) {
    Configuration configuration = NutchConfiguration.create();
    configuration.set("nutch.instance.folder", new File(_folder, nutchInstanceSubFolder).getAbsolutePath());
    configuration.set("plugin.folders", TestUtil.JUNIT_TEST_PLUGIN_FOLDERS);
    configuration
        .set(
            "plugin.includes",
            "protocol-http|urlfilter-regex|parse-(text|html|js)|index-(basic|anchor|metadata)|query-(basic|site|url)|response-(json|xml)|summary-basic|scoring-opic|urlnormalizer-(pass|regex|basic)");
    return configuration;
  }
}
