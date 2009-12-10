package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;

public class CsvParserTest extends TestCase {

  public static String RESOURCE_PATH = "portalu-nutch-gui/src/plugin/admin-urlmaintenance/conf/test";

  public CsvParserTest() {
  }

  public void testWebContainer() throws Exception {
    final CsvParser parser = new CsvParser(UrlType.WEB, 0);
    parser.parse(new File(RESOURCE_PATH + "/web.csv"));

    final ArrayList<UrlContainer> urls = new ArrayList<UrlContainer>();
    while (parser.hasNext()) {
      urls.add(parser.next());
    }

    assertEquals(urls.size(), 2);

    final UrlContainer a = urls.get(0);
    assertEquals(0, a.getProviderId());
    assertEquals(UrlType.WEB, a.getUrlType());
    assertEquals("http://www.kst.de/index.html", a.getStartUrl().getUrl());
    assertEquals(3, a.getWhiteUrls().size());
    assertEquals(4, a.getBlackUrls().size());

    final UrlContainer b = urls.get(1);
    assertEquals(0, b.getProviderId());
    assertEquals(UrlType.WEB, b.getUrlType());
    assertEquals("http://www.101tec.com/0,1,2,00.html", b.getStartUrl().getUrl());
    assertEquals(3, b.getWhiteUrls().size());
    assertEquals(3, b.getBlackUrls().size());
  }

  public void testCatalogContainer() throws Exception {
    final CsvParser parser = new CsvParser(UrlType.CATALOG, 1);
    parser.parse(new File(RESOURCE_PATH + "/catalog.csv"));

    final ArrayList<UrlContainer> urls = new ArrayList<UrlContainer>();
    while (parser.hasNext()) {
      urls.add(parser.next());
    }

    assertEquals(3, urls.size());

    final UrlContainer a = urls.get(0);
    assertEquals(1, a.getProviderId());
    assertEquals(1, a.getWhiteUrls().size());
    assertEquals("http://www.kst.de/thema.html", a.getWhiteUrls().get(0).getUrl());
    assertEquals(0, a.getBlackUrls().size());

    final UrlContainer b = urls.get(1);
    assertEquals(1, b.getProviderId());
    assertEquals(1, b.getWhiteUrls().size());
    assertEquals("http://www.kst.de/messwert.html", b.getWhiteUrls().get(0).getUrl());
    assertEquals(0, b.getBlackUrls().size());

    final UrlContainer c = urls.get(2);
    assertEquals(1, c.getProviderId());
    assertEquals(1, c.getWhiteUrls().size());
    assertEquals("http://www.kst.de/service.html", c.getWhiteUrls().get(0).getUrl());
    assertEquals(0, c.getBlackUrls().size());
  }
}
