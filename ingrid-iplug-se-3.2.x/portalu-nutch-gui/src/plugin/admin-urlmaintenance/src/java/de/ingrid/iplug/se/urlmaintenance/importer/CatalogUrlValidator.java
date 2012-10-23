package de.ingrid.iplug.se.urlmaintenance.importer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

public class CatalogUrlValidator implements IUrlValidator {

  private final Log LOG = LogFactory.getLog(CatalogUrlValidator.class);

  private final Map<String, Set<String>> _supportedMetadatas = new HashMap<String, Set<String>>();
  private final IProviderDao _providerDao;
  private final ICatalogUrlDao _urlDao;

  public CatalogUrlValidator(final IProviderDao providerDao, final ICatalogUrlDao urlDao) {
    _providerDao = providerDao;
    _urlDao = urlDao;
    _supportedMetadatas.put("datatype", new HashSet<String>());
    _supportedMetadatas.put("lang", new HashSet<String>());
    _supportedMetadatas.put("alt_title", new HashSet<String>());
    _supportedMetadatas.put("topic", new HashSet<String>());
    _supportedMetadatas.put("service", new HashSet<String>());
    _supportedMetadatas.put("measure", new HashSet<String>());
  }

  @Override
  public boolean validate(final UrlContainer urlContainer, final Map<String, String> errorCodes) {
    final List<Url> whiteUrls = urlContainer.getWhiteUrls();
    final Map<String, Map<String, Set<String>>> metadatas = urlContainer.getMetadatas();
    final Serializable providerId = urlContainer.getProviderId();
    int errorCount = 0;
    errorCount += validateDuple(whiteUrls, providerId, errorCodes);
    errorCount += validateWhiteUrl(whiteUrls, errorCodes);
    errorCount += validateWhiteUrlWithMetadatas(whiteUrls, metadatas, errorCodes);
    errorCount += validateProvider(providerId, errorCodes);
    final boolean valid = (errorCount == 0);
    urlContainer.setValid(valid);
    return errorCount == 0;
  }
  
  private int validateDuple(final List<Url> whiteUrls, Serializable providerId, final Map<String, String> errorCodes) {
      if (whiteUrls.size() > 0) {
        final Url startUrl = whiteUrls.get(0);
        if (_urlDao.getByUrl(startUrl.getUrl(), providerId).size() > 0) {
            errorCodes.put("whiteurl.duple", startUrl.getUrl());
            if (LOG.isWarnEnabled()) {
                LOG.warn("   !!!   Duplicate URL, already in database: " + startUrl.getUrl());
            }
            return 1;
        }
      }
      return 0;
  }

  private int validateProvider(final Serializable providerId, final Map<String, String> errorCodes) {
    int errorCount = 0;
    if (providerId == null) {
      errorCodes.put("provider.empty", "null");
      errorCount++;
    } else {
      final boolean existsProvider = existsProvider(providerId);
      if (!existsProvider) {
        errorCodes.put("provider.invalid", "" + providerId);
        errorCount++;
        if (LOG.isWarnEnabled()) {
            LOG.warn("   !!!   Provider not found: " + providerId);
        }
      }
    }
    return errorCount;
  }

  private boolean existsProvider(final Serializable id) {
    final Provider provider = _providerDao.getById(id);
    return provider != null;
  }

  private int validateWhiteUrl(final List<Url> whiteUrls, final Map<String, String> errorCodes) {
    int errorCount = 0;
    if (whiteUrls.isEmpty()) {
      errorCodes.put("whiteurl.empty", "null");
      errorCount++;
      if (LOG.isWarnEnabled()) {
          LOG.warn("   !!!   URL is missing !");
      }
    } else if (whiteUrls.size() > 1) {
      errorCodes.put("whiteurl.reduntant", "" + whiteUrls.size());
      errorCount++;
      if (LOG.isWarnEnabled()) {
          LOG.warn("   !!!   More than one URL !");
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
        if (LOG.isWarnEnabled()) {
            LOG.warn("   !!!   Metadata is missing to URL: " + whiteUrlString);
        }
      } else {
        final Map<String, Set<String>> keyValues = metadatas.get(whiteUrlString);
        if (keyValues.isEmpty()) {
          errorCodes.put("metadata.empty", whiteUrlString);
          errorCount++;
          if (LOG.isWarnEnabled()) {
              LOG.warn("   !!!   Metadata is missing to URL: " + whiteUrlString);
          }
        } else {
          final Set<String> keys = keyValues.keySet();
          for (final String key : keys) {
            if (!_supportedMetadatas.containsKey(key)) {
              errorCodes.put("metadata.invalid", key);
              errorCount++;
              if (LOG.isWarnEnabled()) {
                  LOG.warn("   !!!   Metadata '" + key + "' not supported to URL: " + whiteUrlString);
              }
            }
          }

          final Set<String> set = keyValues.get("datatype");
          if (!set.contains("topics") && !set.contains("measure") && !set.contains("service")) {
            errorCodes.put("metadata.invalid", set.toString());
            errorCount++;
            if (LOG.isWarnEnabled()) {
                LOG.warn("   !!!   Wrong Type, no 'topics', no 'measure', no 'service' to URL: " + whiteUrlString);
            }
          }
        }
      }
    }
    return errorCount;

  }
}
