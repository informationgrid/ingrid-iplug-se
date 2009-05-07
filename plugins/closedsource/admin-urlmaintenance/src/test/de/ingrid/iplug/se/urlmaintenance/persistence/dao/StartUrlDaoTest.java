package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class StartUrlDaoTest extends DaoTest {

  public void testPaging() throws Exception {
    Provider provider = createProvider();
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    ProviderDao providerDao = new ProviderDao(transactionService);
    Provider byName = providerDao.getByName(provider.getName());
    IStartUrlDao startUrlDao = new StartUrlDao(transactionService);
    ICatalogUrlDao catalogUrlDao = new CatalogUrlDao(transactionService);

    for (int i = 0; i < 23; i++) {
      StartUrl url = new StartUrl();
      url.setUrl("http://www.url" + i + ".com");
      url.setProvider(byName);
      startUrlDao.makePersistent(url);
    }

    for (int i = 0; i < 7; i++) {
      CatalogUrl url = new CatalogUrl();
      url.setUrl("http://www.url" + i + ".com");
      url.setProvider(byName);
      catalogUrlDao.makePersistent(url);
    }
    transactionService.commitTransaction();
    transactionService.close();

    transactionService.beginTransaction();

    byName = providerDao.getByName(provider.getName());
    List<StartUrl> startUrls = startUrlDao.getByProvider(byName, 0, 11);
    assertEquals(11, startUrls.size());
    startUrls = startUrlDao.getByProvider(byName, 11, 100);
    assertEquals(12, startUrls.size());

    transactionService.commitTransaction();
    transactionService.close();

  }
}
