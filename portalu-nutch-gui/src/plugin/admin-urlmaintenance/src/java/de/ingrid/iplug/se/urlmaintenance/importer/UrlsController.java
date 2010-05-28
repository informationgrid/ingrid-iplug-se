package de.ingrid.iplug.se.urlmaintenance.importer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "containerCommand" })
public class UrlsController extends NavigationSelector {

  private final IStartUrlDao _startUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IExcludeUrlDao _excludeUrlDao;
  private final ICatalogUrlDao _catalogUrlDao;
  private final IProviderDao _providerDao;
  private final IMetadataDao _metadataDao;
  
  private List<String> _errorURLs;

  @Autowired
  public UrlsController(final IStartUrlDao startUrlDao, final ILimitUrlDao limitUrlDao,
      final IExcludeUrlDao excludeUrlDao, final ICatalogUrlDao catalogUrlDao, final IProviderDao providerDao,
      final IMetadataDao metadataDao) {
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
    _catalogUrlDao = catalogUrlDao;
    _providerDao = providerDao;
    _metadataDao = metadataDao;
    _errorURLs = new ArrayList<String>();
  }
  
  /**
   * This constructor only is used by the external importer, which migrates
   * URLs from an old iplug-SE
   */
  public UrlsController(final IStartUrlDao startUrlDao, final ILimitUrlDao limitUrlDao,
          final IExcludeUrlDao excludeUrlDao, final ICatalogUrlDao catalogUrlDao, final IProviderDao providerDao,
          final IMetadataDao metadataDao, boolean extern) {
      _startUrlDao = startUrlDao;
      _limitUrlDao = limitUrlDao;
      _excludeUrlDao = excludeUrlDao;
      _catalogUrlDao = catalogUrlDao;
      _providerDao = providerDao;
      _metadataDao = metadataDao;
      _errorURLs = new ArrayList<String>();      
  }

  @ModelAttribute("containerCommand")
  public ContainerCommand injectContainerCommand(final HttpSession session) {
    ContainerCommand command = (ContainerCommand) session.getAttribute("containerCommand");
    if (command == null) {
      command = new ContainerCommand();
    }
    return command;
  }

  @ModelAttribute("importErrors")
  public List<String> errorURLList() {
      return _errorURLs;
  }
  
  @RequestMapping(value = "/import/urls.html", method = RequestMethod.GET)
  public String get(@RequestParam(value = "state", required = false) final String state) {
      // clear errors we didn't come from the post-call!
      if (!"failed".equals(state))
          _errorURLs.clear();
      return "import/urls";
  }

  @RequestMapping(value = "/import/urls.html", method = RequestMethod.POST)
  public String post(@ModelAttribute("containerCommand") final ContainerCommand command) {
    if (!command.getIsValid()) {
      return "redirect:/import/importer.html?state=failed";
    }

    _errorURLs.clear();
    for (final UrlContainer container : command.getContainers().keySet()) {
      saveContainer(container);
    }
    command.clear();
    
    if (_errorURLs.isEmpty())
        return "redirect:/import/importer.html?state=succeed";
    
    return "redirect:/import/urls.html?state=failed";        
  }

  public void saveContainer(final UrlContainer container) {
    switch (container.getUrlType()) {
    case WEB:
      saveWebUrl(container);
      break;
    case CATALOG:
      saveCatalogUrl(container);
      break;
    default:
      break;
    }
  }

  private void saveWebUrl(final UrlContainer container) {
    try {
        final Date date = new Date();
        final Provider provider = getProvider(container.getProviderId());
        final Map<String, Map<String, Set<String>>> metadatas = container.getMetadatas();
    
        // start url
        final StartUrl startUrl = saveStartUrl(container.getStartUrl().getUrl(), provider, date);
    
        // limit urls
        final List<LimitUrl> limitUrls = new ArrayList<LimitUrl>();
        for (final Url url : container.getWhiteUrls()) {
          final LimitUrl limitUrl = saveWhiteUrl(url.getUrl(), provider, date, startUrl, metadatas);
          limitUrls.add(limitUrl);
        }
    
        // exclude urls
        final List<ExcludeUrl> excludeUrls = new ArrayList<ExcludeUrl>();
        for (final Url url : container.getBlackUrls()) {
          final ExcludeUrl excludeUrl = saveBlackUrl(url.getUrl(), provider, date, startUrl);
          excludeUrls.add(excludeUrl);
        }
    
        // start url again
        startUrl.setLimitUrls(limitUrls);
        startUrl.setExcludeUrls(excludeUrls);
        _startUrlDao.makePersistent(startUrl);
    } catch (Exception e) {
        // FIXME: remember url and present the user
        _errorURLs.add(container.getStartUrl().getUrl());
        System.out.println("Problem saving url: " + container.getStartUrl());
    }
  }

  private StartUrl saveStartUrl(final String urlString, final Provider provider, final Date date) {
    final StartUrl url = new StartUrl();
    url.setUrl(urlString);
    url.setProvider(provider);
    url.setCreated(date);
    url.setUpdated(date);
    _startUrlDao.makePersistent(url);
    return url;
  }

  private LimitUrl saveWhiteUrl(final String urlString, final Provider provider, final Date date,
      final StartUrl startUrl, final Map<String, Map<String, Set<String>>> metadatas) {
    final LimitUrl url = new LimitUrl();
    url.setUrl(urlString);
    url.setProvider(provider);
    url.setCreated(date);
    url.setUpdated(date);
    url.setStartUrl(startUrl);
    url.setMetadatas(getMetadatas(urlString, metadatas));
    _limitUrlDao.makePersistent(url);
    return url;
  }

  private ExcludeUrl saveBlackUrl(final String urlString, final Provider provider, final Date date,
      final StartUrl startUrl) {
    final ExcludeUrl url = new ExcludeUrl();
    url.setUrl(urlString);
    url.setProvider(provider);
    url.setCreated(date);
    url.setUpdated(date);
    url.setStartUrl(startUrl);
    _excludeUrlDao.makePersistent(url);
    return url;
  }

  private void saveCatalogUrl(final UrlContainer container) {
    try {
        final CatalogUrl url = new CatalogUrl();
    
        final Provider provider = getProvider(container.getProviderId());
        url.setProvider(provider);
    
        final Url white = container.getWhiteUrls().get(0);
        url.setUrl(white.getUrl());
    
        final List<Metadata> metadatas = getMetadatas(white.getUrl(), container.getMetadatas());
        url.setMetadatas(metadatas);
    
        final Date date = new Date();
        url.setCreated(date);
        url.setUpdated(date);
    
        _catalogUrlDao.makePersistent(url);
    } catch (Exception e) {
        _errorURLs.add(container.getMetadatas().keySet().toString());
        System.out.println("Problem saving url: " + container.getStartUrl());
    }
  }

  private Provider getProvider(final Serializable id) {
    return _providerDao.getById(id);
  }

  private List<Metadata> getMetadatas(final String url, final Map<String, Map<String, Set<String>>> metadatas) {
    final Map<String, Set<String>> map = metadatas.get(url);
    final List<Metadata> data = new ArrayList<Metadata>();
    if (map != null) {
      for (final String key : map.keySet()) {
        // will be handeled elsewhere (CsvParser.java)
        if (!"alt_title".equals(key)) {
          for (final String value : map.get(key)) {
            final Metadata meta = _metadataDao.getByKeyAndValue(key, value);
            if (meta != null) {
              data.add(meta);
            }
          }
        } else {
            // alternative title normally have to be added to the metadatas
            // and do not exist beforehand
            for (final String value : map.get(key)) {
                // create new metadata if it doesn't exist
                if (value != null && !_metadataDao.exists("alt_title", value)) {
                    _metadataDao.makePersistent(new Metadata("alt_title", value));
                    data.add(_metadataDao.getByKeyAndValue("alt_title", value));
                }
            }
        }
      }
    }
    return data;
  }
}
