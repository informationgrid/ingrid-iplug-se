package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public abstract class DaoTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    System.setProperty("db.mode", "hsql-mem");

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    IUrlDao urlDao = new UrlDao(transactionService, null);
    List<Url> all = urlDao.getAll();
    for (Url url : all) {
      urlDao.makeTransient(url);
    }

    IMetadataDao metadataDao = new MetadataDao(transactionService);
    List<Metadata> allMetadatas = metadataDao.getAll();
    for (Metadata metadata : allMetadatas) {
      metadataDao.makeTransient(metadata);
    }

    IProviderDao providerDao = new ProviderDao(transactionService);
    List<Provider> allProviders = providerDao.getAll();
    for (Provider provider : allProviders) {
      providerDao.makeTransient(provider);
    }

    IPartnerDao partnerDao = new PartnerDao(transactionService, providerDao);
    List<Partner> allPartners = partnerDao.getAll();
    for (Partner partner : allPartners) {
      partnerDao.makeTransient(partner);
    }

    transactionService.commitTransaction();
    transactionService.close();
  }

  protected Provider createProviderForExistingPartner(Partner existingPartner, String providerName) {
    Provider provider = new Provider();
    provider.setName(providerName);
    provider.setPartner(existingPartner);
    existingPartner.addProvider(provider);

    return provider;
  }

  protected Provider createProviderInSeparateTransaction(String partnerName, String providerName) throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    IProviderDao providerDao = new ProviderDao(transactionService);
    PartnerDao partnerDao = new PartnerDao(transactionService, providerDao);
    Partner partner = createPartner(partnerName);
    partnerDao.makePersistent(partner);
    Partner byName = partnerDao.getByName(partner.getName());

    Provider provider = new Provider();
    provider.setName(providerName);
    provider.setPartner(byName);
    providerDao.makePersistent(provider);
    transactionService.commitTransaction();
    transactionService.close();

    return provider;
  }

  protected Partner createPartner(String name) throws Exception {
    Partner partner = new Partner();
    partner.setName(name);

    return partner;
  }

  protected Metadata createMetadata(String key, String value) {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Metadata metadata = new Metadata();
    metadata.setMetadataKey(key);
    metadata.setMetadataValue(value);
    IMetadataDao metadataDao = new MetadataDao(transactionService);
    metadataDao.makePersistent(metadata);

    transactionService.commitTransaction();
    transactionService.close();

    return metadata;
  }

  protected Metadata createMetadata(TransactionService transactionService, String key, String value) {
    Metadata metadata = new Metadata();
    metadata.setMetadataKey(key);
    metadata.setMetadataValue(value);
    IMetadataDao metadataDao = new MetadataDao(transactionService);
    metadataDao.makePersistent(metadata);
    metadataDao.flipTransaction();
    
    return metadata;
  }
}
