package de.ingrid.iplug.se.urlmaintenance;

import java.io.UnsupportedEncodingException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.OutlinkExtractor;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.ProtocolFactory;
import org.apache.nutch.protocol.ProtocolNotFound;
import org.apache.nutch.protocol.ProtocolOutput;
import org.apache.nutch.protocol.ProtocolStatus;
import org.apache.nutch.util.EncodingDetector;
import org.apache.nutch.util.NutchConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.UrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

@Controller
public class TestController extends NavigationSelector {

  private final UrlDao _urlDao;

  @Autowired
  public TestController(final UrlDao urlDao) {
    _urlDao = urlDao;
  }

  @RequestMapping(value = "/test.html", method = RequestMethod.GET)
  public String testURL(@RequestParam("id") final Long id, final ModelMap map) {
    final Url url = _urlDao.getById(id);
    if (url != null) {
      final String urlString = url.getUrl();
      int code = ProtocolStatus.NOTFETCHING;
      String info = null;
      String error = null;

      final Configuration config = NutchConfiguration.create();
      ProtocolOutput output = null;
      Outlink[] outlinks = null;

      try {
        output = getOutput(config, urlString, 3);
      } catch (final ProtocolNotFound e) {
        code = ProtocolStatus.PROTO_NOT_FOUND;
        info = "ProtocolNotFound";
        error = "Kein gültiges Protokoll.";
      }

      if (output != null) {
        code = output.getStatus().getCode();
        info = getInfoForCode(code);

        if (code == ProtocolStatus.SUCCESS) {
          try {
            outlinks = getOutlinks(config, output.getContent());
          } catch (final UnsupportedEncodingException e) {
            code = ProtocolStatus.EXCEPTION;
            info = "UnsupportedEncodingException";
            error = "Fehler beim Ermitteln des Inhalts.";
          }
        }
      }

      map.addAttribute("code", code);
      map.addAttribute("url", urlString);
      map.addAttribute("error", error);
      map.addAttribute("status", info);
      map.addAttribute("outlinks", outlinks);
    }

    return "test";
  }

  private static ProtocolOutput getOutput(final Configuration config, final String url, final int maxRedirects)
      throws ProtocolNotFound {
    final ProtocolFactory pf = new ProtocolFactory(config);
    final CrawlDatum date = new CrawlDatum();
    int tries = 0;
    int code = ProtocolStatus.NOTFETCHING;
    ProtocolOutput output = null;

    String fetchUrl = url;
    while (tries < maxRedirects && code != ProtocolStatus.SUCCESS) {
      tries++;
      final Text textUrl = new Text(fetchUrl);
      final Protocol p = pf.getProtocol(fetchUrl);
      output = p.getProtocolOutput(textUrl, date);
      final ProtocolStatus status = output.getStatus();
      code = status.getCode();
      if (code == ProtocolStatus.MOVED || code == ProtocolStatus.TEMP_MOVED) {
        fetchUrl = status.getMessage();
      }
    }

    return output;
  }

  private static String getInfoForCode(final int code) {
    switch (code) {
    case ProtocolStatus.SUCCESS:
      return "Erfolgreich";
    case ProtocolStatus.ACCESS_DENIED:
      return "Zurgriff verweigert";
    case ProtocolStatus.BLOCKED:
      return "Anfrage blockiert";
    case ProtocolStatus.EXCEPTION:
      return "Unbekannter Fehler";
    case ProtocolStatus.FAILED:
      return "Anfrage fehlgeschlagen";
    case ProtocolStatus.GONE:
      return "Quelle ist fort";
    case ProtocolStatus.NOTFETCHING:
      return "Kein fetching";
    case ProtocolStatus.NOTFOUND:
      return "Quelle wurde nicht gefunden";
    case ProtocolStatus.NOTMODIFIED:
      return "Quelle ist unverändert";
    case ProtocolStatus.PROTO_NOT_FOUND:
      return "Protokoll nicht gefunden";
    case ProtocolStatus.REDIR_EXCEEDED:
      return "Zu viele Verweiße";
    case ProtocolStatus.RETRY:
      return "Temporärer Fehler";
    case ProtocolStatus.ROBOTS_DENIED:
      return "Keine Robots zugelassen";
    case ProtocolStatus.WOULDBLOCK:
      return "Anfrage würde blocken";
    case ProtocolStatus.TEMP_MOVED:
      return "Temporär bewegt";
    case ProtocolStatus.MOVED:
      return "Quelle wurde dauerhaft bewegt";
    default:
      return "Unbekannter Status";
    }
  }

  private static Outlink[] getOutlinks(final Configuration config, final Content content)
      throws UnsupportedEncodingException {
    final EncodingDetector detector = new EncodingDetector(config);
    detector.autoDetectClues(content, false);
    final String encoding = detector.guessEncoding(content, config.get("parser.character.encoding.default",
        "windows-1252"));
    final String textContent = new String(content.getContent(), encoding);
    return textContent == null ? null : OutlinkExtractor.getOutlinks(textContent, config);
  }
}
