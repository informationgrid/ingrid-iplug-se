package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class DaoTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

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
    
    
    IPartnerDao partnerDao = new PartnerDao(transactionService);
    List<Partner> allPartners = partnerDao.getAll();
    for (Partner partner : allPartners) {
      partnerDao.makeTransient(partner);
    }

    transactionService.commitTransaction();
    transactionService.close();

  }
}
