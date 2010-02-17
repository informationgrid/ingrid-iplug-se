package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;


public class CsvParser implements IUrlFileParser {

  private ITupleParser _parser;
  private final UrlType _urlType;
  private final Serializable _providerId;

  public CsvParser(final UrlType urlType, final Serializable providerId) {
    _urlType = urlType;
    _providerId = providerId;
  }

  @Override
  public boolean hasNext() throws IOException {
    return _parser.hasNext();
  }

  @Override
  public UrlContainer next() throws IOException {
    final Tuple next = _parser.next();
    if (next != null) {
      switch (_urlType) {
      case WEB:
        return createWebContainer(next);
      case CATALOG:
        return createCatalogContainer(next);
      default:
        return null;
      }
    }
    return null;
  }

  private UrlContainer createWebContainer(final Tuple tuple) throws IOException {
    final UrlContainer container = new UrlContainer();
    container.setUrlType(UrlType.WEB);
    container.setProviderId(_providerId);

    container.setStartUrl(new Url(tuple.getValue("startUrl")));

    for (final Tuple child : tuple.getChilds()) {
      final String limitUrlString = child.getValue("limitUrl");
      final Url limitUrl = new Url(limitUrlString);
      container.addWhiteUrl(limitUrl);

      final String excludeUrls = child.getValue("excludeUrl");
      if (excludeUrls != null) {
        if (excludeUrls.contains(" ")) {
          final String[] splits = excludeUrls.split(" ");
          for (final String split : splits) {
            if (split.trim().length() > 0) {
              container.addBlackUrl(new Url(split));
            }
          }
        } else {
          if (excludeUrls.trim().length() > 0) {
            container.addBlackUrl(new Url(excludeUrls));
          }
        }
      }

      final String www = child.getValue("www");
      final String law = child.getValue("law");
      final String research = child.getValue("research");
      final String lang = child.getValue("lang");

      if (Boolean.parseBoolean(www)) {
        container.addMetadata(limitUrl, "datatype", "www");
      }
      if (Boolean.parseBoolean(law)) {
        container.addMetadata(limitUrl, "datatype", "law");
      }
      if (Boolean.parseBoolean(research)) {
        container.addMetadata(limitUrl, "datatype", "research");
      }
      container.addMetadata(limitUrl, "datatype", "default");
      container.addMetadata(limitUrl, "lang", lang);

    }

    return container;
  }

  private UrlContainer createCatalogContainer(final Tuple tuple) throws IOException {
    final UrlContainer container = new UrlContainer();
    container.setProviderId(_providerId);
    container.setUrlType(UrlType.CATALOG);

    final Url limitUrl = new Url(tuple.getValue("url"));
    container.addWhiteUrl(limitUrl);

    final String datatype = tuple.getValue("type");
    if (datatype != null) {
        final String[] splits = TokenSplitter.split(datatype);
        for (final String split : splits) {
          if (split != null && split.trim().length() > 0) {
            container.addMetadata(limitUrl, "datatype", split);
          }
        }
      }
    container.addMetadata(limitUrl, "datatype", "www");
    container.addMetadata(limitUrl, "datatype", "default");

    final String topic = tuple.getValue("topic");
    if (topic != null) {
      final String[] splits = TokenSplitter.split(topic);
      for (final String split : splits) {
        if (split != null && split.trim().length() > 0) {
          container.addMetadata(limitUrl, "topics", split);
        }
      }
    }

    final String functCategory = tuple.getValue("funct_category");
    if (functCategory != null) {
      final String[] splits = TokenSplitter.split(functCategory);
      for (final String split : splits) {
        if (split != null && split.trim().length() > 0) {
          container.addMetadata(limitUrl, "funct_category", split);
        }
      }
    }

    final String rubric = tuple.getValue("rubric");
    if (rubric != null) {
      final String[] splits = TokenSplitter.split(rubric);
      for (final String split : splits) {
        if (split != null && split.trim().length() > 0) {
          if ("press".equals(split) || "publication".equals(split) || "event".equals(split)) {
            container.addMetadata(limitUrl, "service", split);
          } else {
            container.addMetadata(limitUrl, "measure", split);
          }
        }
      }
    }

    container.addMetadata(limitUrl, "lang", tuple.getValue("lang"));
    container.addMetadata(limitUrl, "alt_title", tuple.getValue("title"));

    return container;
  }

  @Override
  public void parse(final File file) throws Exception {
    _parser = new LineParser(1);
    if (_urlType == UrlType.WEB) {
      final String[] fields = { "startUrl", "limitUrl", "excludeUrl", "www", "law", "research", "lang" };
      _parser = new MapParser(_parser, LineParser.KEY, fields);
      _parser = new GroupParser(_parser, "startUrl");
    } else {
      final String[] fields = { "url", "type", "topic", "funct_category", "rubric", "title", "lang" };
      _parser = new MapParser(_parser, LineParser.KEY, fields);
      _parser = new TranslateParser(_parser, "type", PropertyLoader.load("/labels/type"));
      _parser = new TranslateParser(_parser, "topic", PropertyLoader.load("/labels/topic", true));
      _parser = new TranslateParser(_parser, "funct_category", PropertyLoader.load("/labels/funct_category", true));
      _parser = new TranslateParser(_parser, "rubric", PropertyLoader.load("/labels/rubric", true));
    }
    _parser.parse(file);
  }

}
