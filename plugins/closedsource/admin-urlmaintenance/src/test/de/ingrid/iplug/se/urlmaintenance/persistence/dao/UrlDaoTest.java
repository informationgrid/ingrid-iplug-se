package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class UrlDaoTest extends TestCase {

  public void testCreate() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    IUrlDao urlDao = new UrlDao(transactionService);

    StartUrl startUrl = new StartUrl();
    startUrl.setUrl("http://www.start.com");

    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setUrl("http://www.limit.com");

    ExcludeUrl excludeUrl = new ExcludeUrl();
    excludeUrl.setUrl("http://www.exclude.com");

    CatalogUrl catalogUrl = new CatalogUrl();
    catalogUrl.setUrl("http://www.catalog.com");

    urlDao.makePersistent(startUrl);
    urlDao.makePersistent(catalogUrl);
    urlDao.makePersistent(limitUrl);
    urlDao.makePersistent(excludeUrl);
    
    startUrl.addLimitUrl(limitUrl);
    startUrl.addExcludeUrl(excludeUrl);

    transactionService.commitTransaction();
    transactionService.close();

    // List<Url> all = urlDao.getAll();
    // System.out.println(all);
  }

}
