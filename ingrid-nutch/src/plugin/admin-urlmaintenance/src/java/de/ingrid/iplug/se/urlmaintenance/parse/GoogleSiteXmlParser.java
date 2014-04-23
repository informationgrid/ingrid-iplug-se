package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

public class GoogleSiteXmlParser implements IUrlFileParser {

  private static final Logger LOG = Logger.getLogger(GoogleSiteXmlParser.class.getName());
  private final XPath _xpath;
  private final DocumentBuilder _documentBuilder;
  private NodeList _nodes;
  private int _length;
  private int _currentNodeIndex;
  private UrlType _urlType = UrlType.WEB;
  private final Serializable _providerId;

  public static enum URL_TYPE {
    CATALOG, WEB
  }

  public GoogleSiteXmlParser(final UrlType urlType, final Serializable providerId) throws ParserConfigurationException {
    _providerId = providerId;
    _urlType = urlType;
    final XPathFactory factory = XPathFactory.newInstance();
    _xpath = factory.newXPath();
    _documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
  }

  @Override
  public boolean hasNext() throws IOException {
    return _currentNodeIndex < _length;
  }

  @Override
  public UrlContainer next() {
    final Node node = _nodes.item(_currentNodeIndex);
    final String startUrlString = node.getTextContent();
    UrlContainer urlContainer = null;
    switch (_urlType) {
    case WEB:
      urlContainer = createWebContainer(startUrlString);
      break;
    case CATALOG:
      urlContainer = createCatalogContainer(startUrlString);
      break;
    default:
      break;
    }
    _currentNodeIndex++;
    return urlContainer;
  }

  @Override
  public void parse(final File file) throws Exception {
    final Document document = _documentBuilder.parse(file);
    _nodes = parseNodes(document, "/urlset/url/loc");
    _length = _nodes.getLength();
    _currentNodeIndex = 0;
  }

  private NodeList parseNodes(final Document document, final String nodePath) throws Exception {
    final XPathExpression expr = _xpath.compile(nodePath);
    final NodeList list = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
    return list;
  }

  private UrlContainer createCatalogContainer(final String startUrlString) {
    final UrlContainer urlContainer = new UrlContainer();
    final Url whiteUrl = new Url(startUrlString);
    urlContainer.addWhiteUrl(whiteUrl);
    urlContainer.setProviderId(_providerId);
    urlContainer.setUrlType(_urlType);
    urlContainer.addMetadata(whiteUrl, "datatype", "www");
    urlContainer.addMetadata(whiteUrl, "datatype", "default");
    urlContainer.addMetadata(whiteUrl, "datatype", "topics");
    urlContainer.addMetadata(whiteUrl, "lang", "de");
    return urlContainer;
  }

  private UrlContainer createWebContainer(final String startUrlString) {
    String whiteUrlString = startUrlString;
    try {
      final URL url = new URL(startUrlString);
      whiteUrlString = url.getProtocol() + "://" + url.getHost();
    } catch (final MalformedURLException e) {
      LOG.warning(e.getMessage());
    }
    final Url startUrl = new Url(startUrlString);
    final Url whiteUrl = new Url(whiteUrlString);
    final UrlContainer urlContainer = new UrlContainer();
    urlContainer.setStartUrl(startUrl);
    urlContainer.addWhiteUrl(whiteUrl);
    // google site map has no exclude urls
    // add partner/provider
    urlContainer.setProviderId(_providerId);
    urlContainer.setUrlType(_urlType);
    // add metadatas
    urlContainer.addMetadata(whiteUrl, "datatype", "www");
    urlContainer.addMetadata(whiteUrl, "datatype", "default");
    urlContainer.addMetadata(whiteUrl, "lang", "de");
    return urlContainer;
  }

}
