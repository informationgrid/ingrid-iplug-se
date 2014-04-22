package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.File;
import java.util.ArrayList;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;

public class GoogleSiteXmlParserTest extends TestCase {

  public void testParseWeb() throws Exception {
    final GoogleSiteXmlParser parser = new GoogleSiteXmlParser(UrlType.WEB, 0);
    parser.parse(new File(CsvParserTest.RESOURCE_PATH + "/google-site.xml"));

    final ArrayList<UrlContainer> urls = new ArrayList<UrlContainer>();
    while (parser.hasNext()) {
      urls.add(parser.next());
    }

    assertEquals(3, urls.size());

    final UrlContainer a = urls.get(0);
    assertEquals(0, a.getProviderId());
    assertEquals(1, a.getWhiteUrls().size());
    assertEquals("http://www.foo.com/index.html", a.getStartUrl().getUrl());
    assertEquals("http://www.foo.com", a.getWhiteUrls().get(0).getUrl());
    assertEquals(0, a.getBlackUrls().size());

    final UrlContainer b = urls.get(1);
    assertEquals(0, b.getProviderId());
    assertEquals(1, b.getWhiteUrls().size());
    assertEquals("http://www.bar.com/index.jsp", b.getStartUrl().getUrl());
    assertEquals("http://www.bar.com", b.getWhiteUrls().get(0).getUrl());
    assertEquals(0, b.getBlackUrls().size());

    final UrlContainer c = urls.get(2);
    assertEquals(0, c.getProviderId());
    assertEquals(1, c.getWhiteUrls().size());
    assertEquals("http://www.foobar.com/index.php", c.getStartUrl().getUrl());
    assertEquals("http://www.foobar.com", c.getWhiteUrls().get(0).getUrl());
    assertEquals(0, c.getBlackUrls().size());
  }

  public void testParseCatalog() throws Exception {
    final GoogleSiteXmlParser parser = new GoogleSiteXmlParser(UrlType.CATALOG, 1);
    parser.parse(new File(CsvParserTest.RESOURCE_PATH + "/google-site.xml"));

    final ArrayList<UrlContainer> urls = new ArrayList<UrlContainer>();
    while (parser.hasNext()) {
      urls.add(parser.next());
    }

    assertEquals(3, urls.size());

    final UrlContainer a = urls.get(0);
    assertEquals(1, a.getProviderId());
    assertEquals(1, a.getWhiteUrls().size());
    assertNull(a.getStartUrl());
    assertEquals("http://www.foo.com/index.html", a.getWhiteUrls().get(0).getUrl());
    assertEquals(0, a.getBlackUrls().size());

    final UrlContainer b = urls.get(1);
    assertEquals(1, b.getProviderId());
    assertEquals(1, b.getWhiteUrls().size());
    assertNull(b.getStartUrl());
    assertEquals("http://www.bar.com/index.jsp", b.getWhiteUrls().get(0).getUrl());
    assertEquals(0, b.getBlackUrls().size());

    final UrlContainer c = urls.get(2);
    assertEquals(1, c.getProviderId());
    assertEquals(1, c.getWhiteUrls().size());
    assertNull(c.getStartUrl());
    assertEquals("http://www.foobar.com/index.php", c.getWhiteUrls().get(0).getUrl());
    assertEquals(0, c.getBlackUrls().size());
  }

}
