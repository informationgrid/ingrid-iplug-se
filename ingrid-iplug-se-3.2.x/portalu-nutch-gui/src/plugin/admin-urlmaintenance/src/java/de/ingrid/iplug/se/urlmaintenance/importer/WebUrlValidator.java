package de.ingrid.iplug.se.urlmaintenance.importer;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

public class WebUrlValidator implements IUrlValidator {

  private final Map<String, Set<String>> _supportedMetadatas = new HashMap<String, Set<String>>();
  private final IProviderDao _providerDao;

    private final IStartUrlDao _urlDao;

  public WebUrlValidator(final IProviderDao providerDao, final IStartUrlDao urlDao) {
    _providerDao = providerDao;
        _urlDao = urlDao;
    _supportedMetadatas.put("datatype", new HashSet<String>());
    _supportedMetadatas.put("lang", new HashSet<String>());
  }

  @Override
  public boolean validate(final UrlContainer urlContainer, final Map<String, String> errorCodes) {
    final Url startUrl = urlContainer.getStartUrl();
    final List<Url> whiteUrls = urlContainer.getWhiteUrls();
    final List<Url> blackUrls = urlContainer.getBlackUrls();
    final Map<String, Map<String, Set<String>>> metadatas = urlContainer.getMetadatas();
    final Serializable providerId = urlContainer.getProviderId();
    int errorCount = 0;
    errorCount += validateDuple(startUrl, providerId, errorCodes);
    errorCount += validateStartUrlWithWhiteUrl(startUrl, whiteUrls, errorCodes);
    errorCount += validateStartUrlWithBlackUrl(startUrl, blackUrls, errorCodes);
    errorCount += validateWhiteUrlWithMetadatas(whiteUrls, metadatas, errorCodes);
    errorCount += validateProvider(providerId, errorCodes);
    final boolean valid = (errorCount == 0);
    urlContainer.setValid(valid);
    return errorCount == 0;
  }

    private int validateDuple(final Url startUrl, Serializable providerId, final Map<String, String> errorCodes) {
        if (_urlDao.getByUrl(startUrl.getUrl(), providerId).size() > 0) {
            errorCodes.put("starturl.duple", startUrl.getUrl());
            return 1;
    }
        return 0;
    }

  private int validateStartUrlWithBlackUrl(final Url startUrl, final List<Url> blackUrls,
      final Map<String, String> errorCodes) {
    int errorCount = 0;
    if (startUrl != null) {
      final String urlStringStart = startUrl.getUrl();
      if (urlStringStart != null && urlStringStart.trim().length() > 0) {
        for (final Url url : blackUrls) {
          final String urlStringBlack = url.getUrl();
          if (urlStringStart.startsWith(urlStringBlack)) {
            errorCodes.put("blackurl.inalid", urlStringBlack);
            errorCount++;
          }
        }
      }
    }
    return errorCount;
  }

  private int validateProvider(final Serializable providerId, final Map<String, String> errorCodes) {
    int errorCount = 0;
    if (providerId == null) {
      errorCodes.put("provider.empty", "null");
      errorCount++;
    } else {
      if (!existsProvider(providerId)) {
        errorCodes.put("provider.invalid", "" + providerId);
        errorCount++;
      }
    }
    return errorCount;
  }

  private boolean existsProvider(final Serializable id) {
    final Provider provider = _providerDao.getById(id);
    return provider != null;
  }

  private int validateStartUrlWithWhiteUrl(final Url startUrl, final List<Url> whiteUrls,
      final Map<String, String> errorCodes) {
    int errorCount = 0;
    if (startUrl == null || startUrl.getUrl() == null || startUrl.getUrl().trim().length() <= 0) {
      errorCodes.put("starturl.empty", "null");
      errorCount++;
    } else {
      try {
        new URL(startUrl.getUrl());
      } catch (final MalformedURLException e) {
        errorCodes.put("starturl.invalid", startUrl.getUrl());
        errorCount++;
      }
    }
    if (whiteUrls.isEmpty()) {
      errorCodes.put("whiteurl.empty", "null");
      errorCount++;
    } else {
      for (final Url whiteUrl : whiteUrls) {
        final String urlString = whiteUrl.getUrl();
        if (urlString == null) {
          // FIXME: ignore this error since it doesn't recognize multiple excludes without limit urls!!!
          //errorCodes.put("whiteurl.empty", "null");
          //errorCount++;
        } else {
          try {
            new URL(urlString);
          } catch (final MalformedURLException e) {
            errorCodes.put("whiteurl.invalid", urlString);
            errorCount++;
          }
        }
      }
    }
    return errorCount;
  }

  private int validateWhiteUrlWithMetadatas(final List<Url> whiteUrls,
      final Map<String, Map<String, Set<String>>> metadatas, final Map<String, String> errorCodes) {
    int errorCount = 0;
    for (final Url whiteUrl : whiteUrls) {
      final String whiteUrlString = whiteUrl.getUrl();
      if (!metadatas.containsKey(whiteUrlString)) {
        errorCodes.put("metadata.empty", whiteUrlString);
        errorCount++;
      } else {
        final Map<String, Set<String>> keyValues = metadatas.get(whiteUrlString);
        if (keyValues.isEmpty()) {
          errorCodes.put("metadata.empty", whiteUrlString);
          errorCount++;
        } else if (!keyValues.containsKey("datatype")) {
            errorCodes.put("metadata.missing", whiteUrlString);
            errorCount++;
        } else {
          for (final String key : keyValues.keySet()) {
            if (!_supportedMetadatas.containsKey(key)) {
              errorCodes.put("metadata.invalid", key);
              errorCount++;
            }
          }
        }
      }
    }
    return errorCount;

  }
}
