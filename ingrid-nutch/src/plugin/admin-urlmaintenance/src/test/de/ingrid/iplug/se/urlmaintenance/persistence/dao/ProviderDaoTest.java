package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class ProviderDaoTest extends DaoTest {

  public void testCreateProvider() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    IProviderDao providerDao = new ProviderDao(transactionService);
    PartnerDao partnerDao = new PartnerDao(transactionService, providerDao);
    Partner partner = createPartner("pa", "partner");
    partnerDao.makePersistent(partner);
    Partner byName = partnerDao.getByName(partner.getName());

    Provider provider = new Provider();
    provider.setShortName("f");
    provider.setName("foo");
    provider.setPartner(byName);
    providerDao.makePersistent(provider);
    transactionService.commitTransaction();
    transactionService.close();

    transactionService.beginTransaction();
    List<Provider> all = providerDao.getAll();
    assertEquals(1, all.size());
    Provider Provider2 = all.get(0);
    assertEquals(provider.getName(), Provider2.getName());
  }

  public void testDeleteProvider() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    IProviderDao dao = new ProviderDao(transactionService);
    PartnerDao partnerDao = new PartnerDao(transactionService, dao);
    Partner partner = createPartner("pa", "partner");
    partnerDao.makePersistent(partner);
    Partner byName = partnerDao.getByName(partner.getName());

    Provider provider = new Provider();
    provider.setShortName("f");
    provider.setName("foo");
    provider.setPartner(byName);
    dao.makePersistent(provider);
    transactionService.commitTransaction();
    transactionService.close();
    transactionService.beginTransaction();
    List<Provider> all = dao.getAll();
    assertEquals(1, all.size());
    dao.makeTransient(all.get(0));
    transactionService.commitTransaction();
    transactionService.close();
    transactionService.beginTransaction();
    all = dao.getAll();
    assertEquals(0, all.size());
    transactionService.commitTransaction();
    transactionService.close();
  }

  public void testGetByName() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    IProviderDao dao = new ProviderDao(transactionService);
    PartnerDao partnerDao = new PartnerDao(transactionService,dao);
    Partner partner = createPartner("pa", "partner");
    partnerDao.makePersistent(partner);
    Partner byName = partnerDao.getByName(partner.getName());

    Provider provider = new Provider();
    provider.setShortName("f");
    provider.setName("foo");
    provider.setPartner(byName);
    dao.makePersistent(provider);
    transactionService.commitTransaction();
    transactionService.close();

    transactionService.beginTransaction();
    Provider provider2 = dao.getByName("foo");
    assertEquals(provider.getId(), provider2.getId());
  }
  
}
