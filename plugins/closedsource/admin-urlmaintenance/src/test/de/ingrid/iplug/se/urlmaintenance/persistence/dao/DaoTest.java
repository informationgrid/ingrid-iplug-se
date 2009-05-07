package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class DaoTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    IUrlDao urlDao = new UrlDao(transactionService);
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

    IPartnerDao partnerDao = new PartnerDao(transactionService);
    List<Partner> allPartners = partnerDao.getAll();
    for (Partner partner : allPartners) {
      partnerDao.makeTransient(partner);
    }

    transactionService.commitTransaction();
    transactionService.close();
  }

  protected Provider createProvider() throws Exception {
    Partner partner = createPartner();

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    PartnerDao partnerDao = new PartnerDao(transactionService);
    Partner byName = partnerDao.getByName(partner.getName());

    Provider provider = new Provider();
    provider.setName("foo");
    provider.setPartner(byName);
    IProviderDao dao = new ProviderDao(transactionService);
    dao.makePersistent(provider);
    transactionService.commitTransaction();
    transactionService.close();

    return provider;
  }

  protected Partner createPartner() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Partner partner = new Partner();
    partner.setName("partner");
    IPartnerDao dao = new PartnerDao(transactionService);
    dao.makePersistent(partner);
    transactionService.commitTransaction();
    transactionService.close();

    return partner;
  }
}
