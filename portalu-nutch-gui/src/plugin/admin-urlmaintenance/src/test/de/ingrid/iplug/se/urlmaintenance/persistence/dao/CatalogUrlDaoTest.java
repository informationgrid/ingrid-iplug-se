package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class CatalogUrlDaoTest extends DaoTest {

  public void testGetByProviderAndMetadata() throws Exception {
    createProviderInSeparateTransaction("pa", "partner", new String[] { "pr" }, new String[] { "provider" });
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

    ICatalogUrlDao catalogUrlDao = new CatalogUrlDao(transactionService);

    CatalogUrl foo = new CatalogUrl();
    foo.setUrl("http://www.foo.com");
    foo.setProvider(byName);
    foo.addMetadata(metadata1);

    CatalogUrl anotherFoo = new CatalogUrl();
    anotherFoo.setUrl("http://www.anotherFoo.com");
    anotherFoo.setProvider(byName);
    anotherFoo.addMetadata(metadata1);

    CatalogUrl bar = new CatalogUrl();
    bar.setUrl("http://www.bar.com");
    bar.setProvider(byName);
    bar.addMetadata(metadata2);

    CatalogUrl foobar = new CatalogUrl();
    foobar.setUrl("http://www.foobar.com");
    foobar.setProvider(byName);
    foobar.addMetadata(metadata3);

    catalogUrlDao.makePersistent(foo);
    catalogUrlDao.makePersistent(anotherFoo);
    catalogUrlDao.makePersistent(bar);
    catalogUrlDao.makePersistent(foobar);

    transactionService.commitTransaction();
    transactionService.close();

    // should return foo and foobar
    List<Metadata> metadataList = new ArrayList<Metadata>();
    metadataList.add(metadata1);
    List<CatalogUrl> catalogUrls = catalogUrlDao.getByProviderAndMetadatas(
        byName, metadataList, 0, 10, OrderBy.CREATED_ASC);
    assertEquals(2, catalogUrls.size());
    assertTrue(catalogUrls.contains(foo));
    assertTrue(catalogUrls.contains(anotherFoo));
    Long count = catalogUrlDao
        .countByProviderAndMetadatas(byName, metadataList);
    assertEquals(new Long(2), count);

    // should return bar and foobar
    metadataList.clear();
    metadataList.add(metadata2);
    catalogUrls = catalogUrlDao.getByProviderAndMetadatas(byName, metadataList,
        0, 10, OrderBy.CREATED_ASC);
    assertEquals(1, catalogUrls.size());
    assertTrue(catalogUrls.contains(bar));
    count = catalogUrlDao.countByProviderAndMetadatas(byName, metadataList);
    assertEquals(new Long(1), count);

    // should return foobar
    metadataList.clear();
    metadataList.add(metadata3);
    catalogUrls = catalogUrlDao.getByProviderAndMetadatas(byName, metadataList,
        0, 10, OrderBy.CREATED_ASC);
    assertEquals(1, catalogUrls.size());
    assertTrue(catalogUrls.contains(foobar));
    count = catalogUrlDao.countByProviderAndMetadatas(byName, metadataList);
    assertEquals(new Long(1), count);
  }
}
