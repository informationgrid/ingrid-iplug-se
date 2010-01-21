package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class PartnerDaoTest extends DaoTest {

  public void testCreatePartner() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    IProviderDao providerDao = new ProviderDao(transactionService);
    IPartnerDao partnerDao = new PartnerDao(transactionService, providerDao);
    Partner partner = new Partner();
    partner.setShortName("f");
    partner.setName("foo");
    Provider provider1 = new Provider();
    provider1.setShortName("p1");
    provider1.setName("provider1");
    Provider provider2 = new Provider();
    provider2.setShortName("p2");
    provider2.setName("provider2");
    partner.addProvider(provider1);
    partner.addProvider(provider2);
    partnerDao.makePersistent(partner);
    transactionService.commitTransaction();
    transactionService.close();

    transactionService.beginTransaction();
    List<Partner> all = partnerDao.getAll();
    assertEquals(1, all.size());
    assertEquals(partner.getName(), all.get(0).getName());
    assertEquals(2, all.get(0).getProviders().size());
  }

  public void testDeletePartner() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Partner partner = createPartner("paA", "partnerA");
    createProviderForExistingPartner(partner, "prA", "providerA");

    ProviderDao providerDao = new ProviderDao(transactionService);
    IPartnerDao partnerDao = new PartnerDao(transactionService, providerDao);
    partnerDao.makePersistent(partner);
    partnerDao.flipTransaction();

    // verify everthing is stored well
    List<Partner> all = partnerDao.getAll();
    assertEquals(1, all.size());
    assertEquals(1, all.get(0).getProviders().size());
    assertEquals(1, providerDao.getAll().size());
    assertEquals(partner, providerDao.getAll().get(0).getPartner());

    // delete only the partner...
    partnerDao.makeTransient(all.get(0));
    transactionService.flipTransaction();

    // ... and verify that partner and its providers are deleted too
    all = partnerDao.getAll();
    assertEquals(0, all.size());
    assertEquals(0, providerDao.getAll().size());
    transactionService.commitTransaction();
    transactionService.close();
  }

  public void testDeleteAndAddAProviderFromPartner() throws Exception {

    // setup :
    // partner1 -> providerA
    Partner partner1 = createPartner("pa1", "partner1");
    Provider providerA = createProviderForExistingPartner(partner1, "prA", "providerA");
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    ProviderDao providerDao = new ProviderDao(transactionService);
    PartnerDao partnerDao = new PartnerDao(transactionService, providerDao);
    partnerDao.makePersistent(partner1);
    partnerDao.flipTransaction();

    // verify partner1 -> providerA
    List<Partner> partnersFormDb = partnerDao.getAll();
    List<Provider> providersFromDb = providerDao.getAll();
    System.out.println("1. Partners: " + partnersFormDb + ", providers: " + providersFromDb);
    assertEquals(1, partnersFormDb.size());
    assertEquals(partner1, partnersFormDb.get(0));
    assertEquals(1, partnersFormDb.get(0).getProviders().size());
    assertEquals(1, providersFromDb.size());
    transactionService.flipTransaction();

    // remove providerA from partner1 and add new Provider providerB
    System.out.println("2. Partners: " + partnerDao.getAll() + ", providers: " + providerDao.getAll());
    partnerDao.removeProvider(partner1, providerA);
    transactionService.flipTransaction(); // Important: Need this
                                          // flipTransaction() because remove
                                          // and insert does'nt work within same
                                          // transaction!
    System.out.println("3. Partners: " + partnerDao.getAll() + ", providers: " + providerDao.getAll());
    createProviderForExistingPartner(partner1, "prB", "providerB");
    transactionService.flipTransaction();
    System.out.println("4. Partners: " + partnerDao.getAll() + ", providers: " + providerDao.getAll());

    // verify partner1 has only the new providerB
    partnersFormDb = partnerDao.getAll();
    providersFromDb = providerDao.getAll();
    assertEquals(1, partnersFormDb.get(0).getProviders().size());
    assertEquals(1, providersFromDb.size());
    assertEquals("providerB", partnersFormDb.get(0).getProviders().get(0).getName());

    transactionService.commitTransaction();
    transactionService.close();
  }

  public void testGetByName() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Partner partner = new Partner();
    partner.setShortName("f");
    partner.setName("foo");
    IPartnerDao dao = new PartnerDao(transactionService, new ProviderDao(transactionService));
    dao.makePersistent(partner);
    transactionService.commitTransaction();
    transactionService.close();

    transactionService.beginTransaction();
    Partner partner2 = dao.getByName("foo");
    assertEquals(partner.getId(), partner2.getId());
  }
}
