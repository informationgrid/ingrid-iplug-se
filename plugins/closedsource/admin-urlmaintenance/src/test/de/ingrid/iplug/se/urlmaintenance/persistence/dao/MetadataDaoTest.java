package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class MetadataDaoTest extends DaoTest {

  public void testCreate() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Metadata metadata = new Metadata();
    metadata.setMetadataKey("foo");
    metadata.setMetadataValue("bar");

    MetadataDao metadataDao = new MetadataDao(transactionService);
    metadataDao.makePersistent(metadata);
    transactionService.commitTransaction();
    transactionService.close();

    List<Metadata> all = metadataDao.getAll();
    assertEquals(1, all.size());
    assertEquals("foo", all.get(0).getMetadataKey());
    assertEquals("bar", all.get(0).getMetadataValue());

    try {
      transactionService.beginTransaction();
      Metadata anotherMetadata = new Metadata();
      anotherMetadata.setMetadataKey("foo");
      anotherMetadata.setMetadataValue("bar");
      metadataDao.makePersistent(anotherMetadata);
      transactionService.commitTransaction();
      transactionService.close();
      fail();
    } catch (Exception e) {
    }
  }

  public void testGetByKeyAndValue() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Metadata metadata = new Metadata();
    metadata.setMetadataKey("foo");
    metadata.setMetadataValue("bar");

    MetadataDao metadataDao = new MetadataDao(transactionService);
    metadataDao.makePersistent(metadata);
    transactionService.commitTransaction();
    transactionService.close();

    Metadata metadata2 = metadataDao.getByKeyAndValue("foo", "bar");
    assertEquals("foo", metadata2.getMetadataKey());
    assertEquals("bar", metadata2.getMetadataValue());
  }
}
