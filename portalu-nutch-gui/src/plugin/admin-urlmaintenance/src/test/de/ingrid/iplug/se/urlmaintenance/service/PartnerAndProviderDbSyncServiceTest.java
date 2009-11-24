package de.ingrid.iplug.se.urlmaintenance.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.DaoTest;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.PartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class PartnerAndProviderDbSyncServiceTest extends DaoTest {

  private PartnerAndProviderDbSyncService service;
  private TransactionService transactionService;
  private PartnerDao partnerDao;
  private ProviderDao providerDao;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    transactionService = new TransactionService();
    transactionService.beginTransaction();
    providerDao = new ProviderDao(transactionService);
    partnerDao = new PartnerDao(transactionService, providerDao);

    service = new PartnerAndProviderDbSyncService(partnerDao, providerDao);
  }

  public void testSyncDb() {
    List<Map<String, Serializable>> allPartnerWithProvider = new ArrayList<Map<String, Serializable>>();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA"));

    service.syncDb(allPartnerWithProvider);

    // verify partnerA with providerA are in db
    List<Partner> partnersFromDb = partnerDao.getAll();
    List<Provider> providersFromDb = providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(1, providersFromDb.size());

    // modify partnerA and add an additional partnerB
    allPartnerWithProvider.clear();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA-A"));
    allPartnerWithProvider.add(createPartnerWithProviders("partnerB", "providerA", "providerB"));
    service.syncDb(allPartnerWithProvider);
    transactionService.flipTransaction();

    // verify partnerA with new provider, and partnerB with two providers
    partnersFromDb = partnerDao.getAll();
    providersFromDb = providerDao.getAll();
    assertEquals(2, partnersFromDb.size());
    assertEquals(3, providersFromDb.size());
    assertEquals("partnerA", partnersFromDb.get(0).getName());
    assertEquals(1, partnersFromDb.get(0).getProviders().size());
    assertEquals("providerA-A", partnersFromDb.get(0).getProviders().get(0).getName());
    assertEquals("partnerB", partnersFromDb.get(1).getName());
    List<Provider> providersFromPartner = partnersFromDb.get(1).getProviders();
    assertEquals(2, providersFromPartner.size());
    Collections.sort(providersFromPartner, new Comparator<Provider>() {

      @Override
      public int compare(Provider o1, Provider o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    assertEquals("providerA", providersFromPartner.get(0).getName());
    assertEquals("providerB", providersFromPartner.get(1).getName());

    // delete the providerB completely and append new provider to partnerA
    allPartnerWithProvider.clear();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA-A", "providerA-B"));
    service.syncDb(allPartnerWithProvider);
    transactionService.flipTransaction();

    partnersFromDb = partnerDao.getAll();
    providersFromDb = providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(providersFromDb.toString(), 2, providersFromDb.size());
    assertEquals("partnerA", partnersFromDb.get(0).getName());
    assertEquals(2, partnersFromDb.get(0).getProviders().size());
    assertEquals("providerA-A", partnersFromDb.get(0).getProviders().get(0).getName());
    assertEquals("providerA-B", partnersFromDb.get(0).getProviders().get(1).getName());

    transactionService.commitTransaction();
    transactionService.close();
  }

  public void testSyncDb_DeleteAllProvidesFromPartner() {
    List<Map<String, Serializable>> allPartnerWithProvider = new ArrayList<Map<String, Serializable>>();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA1", "providerA2"));

    service.syncDb(allPartnerWithProvider);

    // verify partnerA with providerA are in db
    List<Partner> partnersFromDb = partnerDao.getAll();
    List<Provider> providersFromDb = providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(2, providersFromDb.size());

    // remove all providers from partnerA
    allPartnerWithProvider.clear();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA"));
    service.syncDb(allPartnerWithProvider);

    // verify partnerA with new provider, and partnerB with two providers
    partnersFromDb = partnerDao.getAll();
    providersFromDb = providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(0, providersFromDb.size());
    assertEquals("partnerA", partnersFromDb.get(0).getName());
    assertEquals(0, partnersFromDb.get(0).getProviders().size());
    
    // delete the last partner too
    allPartnerWithProvider.clear();
    service.syncDb(allPartnerWithProvider);

    partnersFromDb = partnerDao.getAll();
    providersFromDb = providerDao.getAll();
    assertEquals(0, partnersFromDb.size());
    assertEquals(0, providersFromDb.size());

    transactionService.commitTransaction();
    transactionService.close();
  }

  private Map<String, Serializable> createPartnerWithProviders(String partnerName, String... providerNames) {
    Map<String, Serializable> ret = new HashMap<String, Serializable>();

    ret.put("partnerid", "id-" + partnerName);
    ret.put("name", partnerName);
    List<Serializable> providerInformation = new ArrayList<Serializable>();
    for (String providerName : providerNames) {
      providerInformation.add(createProviderInformation(providerName));
    }
    ret.put("providers", (Serializable) providerInformation);
    return ret;
  }

  private Serializable createProviderInformation(String providerName) {
    Map<String, String> ret = new HashMap<String, String>();

    ret.put("providerid", "id-" + providerName);
    ret.put("name", providerName);
    ret.put("url", "http://my.dummypage.com");
    return (Serializable) ret;
  }
}
