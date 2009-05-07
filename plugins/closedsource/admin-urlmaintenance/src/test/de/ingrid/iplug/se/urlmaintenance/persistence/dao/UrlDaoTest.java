package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class UrlDaoTest extends DaoTest {

  public void testCreate() throws Exception {
    createProvider();

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    ProviderDao providerDao = new ProviderDao(transactionService);
    Provider byName = providerDao.getByName("foo");

    IStartUrlDao startUrlDao = new StartUrlDao(transactionService);
    ILimitUrlDao limitUrlDao = new LimitUrlDao(transactionService);
    IExcludeDao excludeUrlDao = new ExcludeUrlDao(transactionService);
    ICatalogUrlDao catalogUrlDao = new CatalogUrlDao(transactionService);

    StartUrl startUrl = new StartUrl();
    startUrl.setUrl("http://www.start.com");
    startUrl.setProvider(byName);

    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setUrl("http://www.limit.com");
    limitUrl.setProvider(byName);

    ExcludeUrl excludeUrl = new ExcludeUrl();
    excludeUrl.setUrl("http://www.exclude.com");
    excludeUrl.setProvider(byName);

    CatalogUrl catalogUrl = new CatalogUrl();
    catalogUrl.setUrl("http://www.catalog.com");
    catalogUrl.setProvider(byName);

    startUrlDao.makePersistent(startUrl);
    catalogUrlDao.makePersistent(catalogUrl);
    limitUrlDao.makePersistent(limitUrl);
    excludeUrlDao.makePersistent(excludeUrl);

    startUrl.addLimitUrl(limitUrl);
    startUrl.addExcludeUrl(excludeUrl);

    transactionService.commitTransaction();
    transactionService.close();

    transactionService.beginTransaction();

    List<StartUrl> allStartUrls = startUrlDao.getAll();
    assertEquals(1, allStartUrls.size());
    List<LimitUrl> allLimitUrls = limitUrlDao.getAll();
    assertEquals(1, allLimitUrls.size());
    List<ExcludeUrl> allExcludeUrls = excludeUrlDao.getAll();
    assertEquals(1, allExcludeUrls.size());
    List<CatalogUrl> allCatalogUrls = catalogUrlDao.getAll();
    assertEquals(1, allCatalogUrls.size());

    IUrlDao urlDao = new UrlDao(transactionService);
    List<Url> all = urlDao.getAll();
    assertEquals(4, all.size());
    
    transactionService.commitTransaction();
    transactionService.close();
  }

  

}
