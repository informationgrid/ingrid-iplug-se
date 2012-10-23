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
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.StartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.UrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;
import de.ingrid.iplug.util.TimeProvider;

public class PartnerAndProviderDbSyncServiceTest extends DaoTest {

  private PartnerAndProviderDbSyncService _service;
  private TransactionService _transactionService;
  private PartnerDao _partnerDao;
  private ProviderDao _providerDao;
  private UrlDao _urlDao;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    _transactionService = new TransactionService();
    _transactionService.beginTransaction();
    _providerDao = new ProviderDao(_transactionService);
    _partnerDao = new PartnerDao(_transactionService, _providerDao);
    _urlDao = new UrlDao(_transactionService, new TimeProvider());

    _service = new PartnerAndProviderDbSyncService(_partnerDao, _urlDao);
  }

  public void testSyncDb() {
    List<Map<String, Serializable>> allPartnerWithProvider = new ArrayList<Map<String, Serializable>>();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA"));

    _service.syncDb(allPartnerWithProvider);

    // verify partnerA with providerA are in db
    List<Partner> partnersFromDb = _partnerDao.getAll();
    List<Provider> providersFromDb = _providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(1, providersFromDb.size());

    // modify partnerA and add an additional partnerB
    allPartnerWithProvider.clear();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA-A"));
    allPartnerWithProvider.add(createPartnerWithProviders("partnerB", "providerA", "providerB"));
    _service.syncDb(allPartnerWithProvider);
    _transactionService.flipTransaction();

    // verify partnerA with new provider, and partnerB with two providers
    partnersFromDb = _partnerDao.getAll();
    providersFromDb = _providerDao.getAll();
    assertEquals(2, partnersFromDb.size());
    assertEquals(3, providersFromDb.size());
    assertEquals("partnerA", partnersFromDb.get(0).getShortName());
    assertEquals(1, partnersFromDb.get(0).getProviders().size());
    assertEquals("providerA-A", partnersFromDb.get(0).getProviders().get(0).getShortName());
    assertEquals("partnerB", partnersFromDb.get(1).getShortName());
    List<Provider> providersFromPartner = partnersFromDb.get(1).getProviders();
    assertEquals(2, providersFromPartner.size());
    Collections.sort(providersFromPartner, new Comparator<Provider>() {

      @Override
      public int compare(Provider o1, Provider o2) {
        return o1.getShortName().compareTo(o2.getShortName());
      }
    });
    assertEquals("providerA", providersFromPartner.get(0).getShortName());
    assertEquals("providerB", providersFromPartner.get(1).getShortName());

    // delete the providerB completely and append new provider to partnerA
    allPartnerWithProvider.clear();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA-A", "providerA-B"));
    // change names of partner, provider
    allPartnerWithProvider.get(0).put("name", "another-partner-name");
    ((Map)((List)allPartnerWithProvider.get(0).get("providers")).get(0)).put("name", "another-provider-name");
    
    _service.syncDb(allPartnerWithProvider);
    _transactionService.flipTransaction();

    partnersFromDb = _partnerDao.getAll();
    providersFromDb = _providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(providersFromDb.toString(), 2, providersFromDb.size());
    assertEquals("partnerA", partnersFromDb.get(0).getShortName());
    assertEquals("another-partner-name", partnersFromDb.get(0).getName());
    assertEquals(2, partnersFromDb.get(0).getProviders().size());
    assertEquals("providerA-A", partnersFromDb.get(0).getProviders().get(0).getShortName());
    assertEquals("another-provider-name", partnersFromDb.get(0).getProviders().get(0).getName());
    assertEquals("providerA-B", partnersFromDb.get(0).getProviders().get(1).getShortName());

    _transactionService.commitTransaction();
    _transactionService.close();
  }

  public void testSyncDb_DeleteAllProvidesFromPartner() {
    List<Map<String, Serializable>> allPartnerWithProvider = new ArrayList<Map<String, Serializable>>();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA1", "providerA2"));

    _service.syncDb(allPartnerWithProvider);

    // verify partnerA with providerA are in db
    List<Partner> partnersFromDb = _partnerDao.getAll();
    List<Provider> providersFromDb = _providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(2, providersFromDb.size());

    // remove all providers from partnerA
    allPartnerWithProvider.clear();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA"));
    _service.syncDb(allPartnerWithProvider);

    // verify partnerA with new provider, and partnerB with two providers
    partnersFromDb = _partnerDao.getAll();
    providersFromDb = _providerDao.getAll();
    assertEquals(1, partnersFromDb.size());
    assertEquals(0, providersFromDb.size());
    assertEquals("partnerA", partnersFromDb.get(0).getShortName());
    assertEquals(0, partnersFromDb.get(0).getProviders().size());

    // delete the last partner too
    allPartnerWithProvider.clear();
    _service.syncDb(allPartnerWithProvider);

    partnersFromDb = _partnerDao.getAll();
    providersFromDb = _providerDao.getAll();
    assertEquals(0, partnersFromDb.size());
    assertEquals(0, providersFromDb.size());

    _transactionService.commitTransaction();
    _transactionService.close();
  }

  public void testSyncDb_DontDeleteProvidersThatReferToAnUrl() {
    // Create a partner with providers
    List<Map<String, Serializable>> allPartnerWithProvider = new ArrayList<Map<String, Serializable>>();
    allPartnerWithProvider.add(createPartnerWithProviders("partnerA", "providerA1", "providerA2"));

    _service.syncDb(allPartnerWithProvider);

    // Add a start url to one provider
    StartUrlDao startUrlDao = new StartUrlDao(_transactionService);
    StartUrl startUrl = new StartUrl();
    startUrl.setUrl("http://www.start.com");
    startUrl.setProvider(_providerDao.getByShortName("providerA1"));
    startUrlDao.makePersistent(startUrl);
    startUrlDao.flipTransaction();

    // verify url with provider exists
    assertEquals(1, startUrlDao.getAll().size());
    assertNotNull(startUrlDao.getAll().get(0).getProvider());

    // remove all partner and providers in startUrlDao.getAll()
    allPartnerWithProvider.clear();
    _service.syncDb(allPartnerWithProvider);

    // verify, that the partner and provider still exists due to the reference
    // of the url
    assertEquals(1, _partnerDao.getAll().size());
    assertEquals(2, _providerDao.getAll().size());

    _transactionService.commitTransaction();
    _transactionService.close();
  }

  private Map<String, Serializable> createPartnerWithProviders(String partnerName, String... providerNames) {
    Map<String, Serializable> ret = new HashMap<String, Serializable>();

    ret.put("partnerid", partnerName);
    ret.put("name", "name-" + partnerName);
    List<Serializable> providerInformation = new ArrayList<Serializable>();
    for (String providerName : providerNames) {
      providerInformation.add(createProviderInformation(providerName));
    }
    ret.put("providers", (Serializable) providerInformation);
    return ret;
  }

  private Serializable createProviderInformation(String providerName) {
    Map<String, String> ret = new HashMap<String, String>();

    ret.put("providerid", providerName);
    ret.put("name", "name-"+providerName);
    ret.put("url", "http://my.dummypage.com");
    return (Serializable) ret;
  }
}
