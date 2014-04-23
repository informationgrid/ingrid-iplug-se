package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.ArrayList;
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

  protected Provider createProviderForExistingPartner(Partner existingPartner, String shortName, String name) {
    Provider provider = new Provider();
    provider.setShortName(shortName);
    provider.setName(name);
    provider.setPartner(existingPartner);
    existingPartner.addProvider(provider);

    return provider;
  }

  protected List<Provider> createProviderInSeparateTransaction(String partnerShortName, String partnerName, String[] providerShortNames, String[] providerNames) throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    IProviderDao providerDao = new ProviderDao(transactionService);
    PartnerDao partnerDao = new PartnerDao(transactionService, providerDao);
    Partner partner = createPartner(partnerShortName, partnerName);
    partnerDao.makePersistent(partner);
    Partner byName = partnerDao.getByName(partner.getName());
    List<Provider> ret = new ArrayList<Provider>();
    
    int max = Math.min(providerShortNames.length, providerNames.length);
    for(int i = 0; i < max; i++) {
        Provider provider = new Provider();
        provider.setShortName(providerShortNames[i]);
        provider.setName(providerNames[i]);
        provider.setPartner(byName);
        providerDao.makePersistent(provider);
        ret.add(provider);
    }
    transactionService.commitTransaction();
    transactionService.close();

    return ret;
  }

  protected Partner createPartner(String name, String shortName) throws Exception {
    Partner partner = new Partner();
    partner.setShortName(shortName);
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
