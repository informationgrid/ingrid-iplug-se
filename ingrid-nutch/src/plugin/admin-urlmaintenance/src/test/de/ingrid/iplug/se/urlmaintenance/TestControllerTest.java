package de.ingrid.iplug.se.urlmaintenance;

import junit.framework.TestCase;

import org.apache.nutch.protocol.ProtocolStatus;
import org.mockito.Mockito;
import org.springframework.ui.ModelMap;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.UrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

public class TestControllerTest extends TestCase {

  private final Url _wrongUrl;
  private final Url _correctUrl;

  public TestController _controller;
  private final UrlDao _urlDao = Mockito.mock(UrlDao.class);

  public TestControllerTest() {
    _wrongUrl = new Url();
    _wrongUrl.setUrl("http://www.a-wrong-url.com/wrong/wronger/wrongest.html");
    _correctUrl = new Url();
    _correctUrl
        .setUrl("http://www.google.de/#hl=de&source=hp&q=correct+url&btnG=Google-Suche&meta=&aq=f&oq=correct+url&fp=d6463d7c0ae7a1a9");
    _controller = new TestController(_urlDao);
  }

  public void testWrongUrl() {
    final ModelMap map = new ModelMap();

    Mockito.when(_urlDao.getById(1l)).thenReturn(_wrongUrl);
    _controller.testURL(1l, map);

    assertNotSame(ProtocolStatus.SUCCESS, map.get("code"));
    assertEquals(_wrongUrl.getUrl(), map.get("url"));
    assertNotNull(map.get("status"));
    assertNull(map.get("outlinks"));
  }

  public void testCorrectUrl() {
    final ModelMap map = new ModelMap();

    Mockito.when(_urlDao.getById(2l)).thenReturn(_correctUrl);
    _controller.testURL(2l, map);

    assertEquals(ProtocolStatus.SUCCESS, map.get("code"));
    assertEquals(_correctUrl.getUrl(), map.get("url"));
    assertNotNull(map.get("status"));
    assertNull(map.get("error"));
    assertNotNull(map.get("outlinks"));
  }
}
