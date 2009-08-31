package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class UrlDaoTest extends DaoTest {

  public void testCreate() throws Exception {
    createProvider("partner", "provider");

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    ProviderDao providerDao = new ProviderDao(transactionService);
    Provider byName = providerDao.getByName("provider");

    IStartUrlDao startUrlDao = new StartUrlDao(transactionService);
    ILimitUrlDao limitUrlDao = new LimitUrlDao(transactionService);
    IExcludeUrlDao excludeUrlDao = new ExcludeUrlDao(transactionService);
    ICatalogUrlDao catalogUrlDao = new CatalogUrlDao(transactionService);

    long start = System.currentTimeMillis();
    StartUrl startUrl = new StartUrl();
    startUrl.setUrl("http://www.start.com");
    startUrl.setCreated(new Date(start));
    startUrl.setProvider(byName);

    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setUrl("http://www.limit.com");
    limitUrl.setCreated(new Date(start - (1000 * 60 * 60 * 24)));
    limitUrl.setProvider(byName);

    ExcludeUrl excludeUrl = new ExcludeUrl();
    excludeUrl.setUrl("http://www.exclude.com");
    excludeUrl.setCreated(new Date(start - ((1000 * 60 * 60 * 24) * 2)));
    excludeUrl.setProvider(byName);

    CatalogUrl catalogUrl = new CatalogUrl();
    catalogUrl.setUrl("http://www.catalog.com");
    catalogUrl.setCreated(new Date(start - ((1000 * 60 * 60 * 24) * 3)));
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

  public void testLimitUrl() throws Exception {
    createProvider("partner", "provider");
    createMetadata("foo", "bar");
    createMetadata("bar", "foo");
    createMetadata("foobar", "foobar");

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    ProviderDao providerDao = new ProviderDao(transactionService);
    Provider byName = providerDao.getByName("provider");

    MetadataDao metadataDao = new MetadataDao(transactionService);
    Metadata metadata1 = metadataDao.getByKeyAndValue("foo", "bar");
    Metadata metadata2 = metadataDao.getByKeyAndValue("bar", "foo");
    Metadata metadata3 = metadataDao.getByKeyAndValue("foobar", "foobar");

    IStartUrlDao startUrlDao = new StartUrlDao(transactionService);
    ILimitUrlDao limitUrlDao = new LimitUrlDao(transactionService);

    StartUrl foo = new StartUrl();
    foo.setUrl("http://www.foo.com");
    foo.setProvider(byName);

    StartUrl bar = new StartUrl();
    bar.setUrl("http://www.bar.com");
    bar.setProvider(byName);

    StartUrl foobar = new StartUrl();
    foobar.setUrl("http://www.foobar.com");
    foobar.setProvider(byName);

    LimitUrl limitUrl1 = new LimitUrl();
    limitUrl1.setUrl("http://www.limit1.com");
    limitUrl1.setProvider(byName);
    limitUrl1.addMetadata(metadata1);

    LimitUrl limitUrl2 = new LimitUrl();
    limitUrl2.setUrl("http://www.limit2.com");
    limitUrl2.setProvider(byName);
    limitUrl2.addMetadata(metadata2);

    LimitUrl limitUrl3 = new LimitUrl();
    limitUrl3.setUrl("http://www.limit3.com");
    limitUrl3.setProvider(byName);
    limitUrl3.addMetadata(metadata1);
    limitUrl3.addMetadata(metadata2);
    limitUrl3.addMetadata(metadata3);

    foo.addLimitUrl(limitUrl1);
    bar.addLimitUrl(limitUrl2);
    foobar.addLimitUrl(limitUrl3);

    startUrlDao.makePersistent(foo);
    startUrlDao.makePersistent(bar);
    startUrlDao.makePersistent(foobar);
    limitUrlDao.makePersistent(limitUrl1);
    limitUrlDao.makePersistent(limitUrl2);
    limitUrlDao.makePersistent(limitUrl3);

    transactionService.commitTransaction();
    transactionService.close();

    // should return foo and foobar
    List<Metadata> metadataList = new ArrayList<Metadata>();
    metadataList.add(metadata1);
    List<StartUrl> startUrls = startUrlDao.getByProviderAndMetadatas(byName,
        metadataList, 0, 10, OrderBy.CREATED_ASC);
    assertEquals(2, startUrls.size());
    assertTrue(startUrls.contains(foo));
    assertTrue(startUrls.contains(foobar));
    Long count = startUrlDao.countByProviderAndMetadatas(byName, metadataList);
    assertEquals(new Long(2), count);

    // should return bar and foobar
    metadataList.clear();
    metadataList.add(metadata2);
    startUrls = startUrlDao.getByProviderAndMetadatas(byName, metadataList, 0,
        10, OrderBy.CREATED_ASC);
    assertEquals(2, startUrls.size());
    assertTrue(startUrls.contains(bar));
    assertTrue(startUrls.contains(foobar));
    count = startUrlDao.countByProviderAndMetadatas(byName, metadataList);
    assertEquals(new Long(2), count);

    // should return foobar
    metadataList.clear();
    metadataList.add(metadata3);
    startUrls = startUrlDao.getByProviderAndMetadatas(byName, metadataList, 0,
        10, OrderBy.CREATED_ASC);
    assertEquals(1, startUrls.size());
    assertTrue(startUrls.contains(foobar));
    count = startUrlDao.countByProviderAndMetadatas(byName, metadataList);
    assertEquals(new Long(1), count);
  }

}
