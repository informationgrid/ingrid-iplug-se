package de.ingrid.iplug.se.urlmaintenance;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IPartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;
import de.ingrid.iplug.se.urlmaintenance.service.MetadataService;

@Service
public class DataInjector {

  private final MetadataService _metadataService;
  private final IMetadataDao _metadataDao;
  private final IPartnerDao _partnerDao;
  private final IProviderDao _providerDao;
  private final TransactionService _transactionService;
  private final IStartUrlDao _startUrlDao;
  private final ILimitUrlDao _limitUrlDao;
  private final IExcludeUrlDao _excludeUrlDao;

  @Autowired
  public DataInjector(MetadataService metadataService,
      IMetadataDao metadataDao, IPartnerDao partnerDao,
      IProviderDao providerDao, IStartUrlDao startUrlDao,
      ILimitUrlDao limitUrlDao, IExcludeUrlDao excludeUrlDao,
      TransactionService transactionService, DatabaseExport databaseExport) throws InterruptedException {
    _metadataService = metadataService;
    _metadataDao = metadataDao;
    _partnerDao = partnerDao;
    _providerDao = providerDao;
    _startUrlDao = startUrlDao;
    _limitUrlDao = limitUrlDao;
    _excludeUrlDao = excludeUrlDao;
    _transactionService = transactionService;

    createTestData();
    // lang
    List<Metadata> langs = _metadataService.getLang();
    createMetadatasIfNotExists(langs);

    // datatypes
    List<Metadata> datatypes = _metadataService.getDatatypes();
    createMetadatasIfNotExists(datatypes);

    // measure
    List<Metadata> measure = _metadataService.getMeasure();
    createMetadatasIfNotExists(measure);

    // service
    List<Metadata> service = _metadataService.getService();
    createMetadatasIfNotExists(service);

    // topics
    List<Metadata> topics = _metadataService.getTopics();
    createMetadatasIfNotExists(topics);

    createUrls();
    
    databaseExport.exportWebUrls();
    databaseExport.exportCatalogUrls();
  }

  private void createUrls() throws InterruptedException {
    if ("dev".equals(System.getProperty("deployMode"))) {
      _transactionService.beginTransaction();
      Provider provider = _providerDao.getByName("devProvider");
      List<StartUrl> urls = _startUrlDao.getByProvider(provider, 0, 23,
          OrderBy.URL_ASC);

      for (int i = 0; urls.isEmpty() && i < 0; i++) {
        List<Metadata> metadatas = new ArrayList<Metadata>();
        metadatas.add(_metadataDao.getByKeyAndValue("datatype", "www"));
        if (i % 3 == 0) {
          metadatas.add(_metadataDao.getByKeyAndValue("lang", "en"));
          metadatas.add(_metadataDao.getByKeyAndValue("lang", "de"));
        } else if (i % 5 == 0) {
          metadatas.add(_metadataDao.getByKeyAndValue("datatype", "research"));
          metadatas.add(_metadataDao.getByKeyAndValue("lang", "en"));
        } else if (i % 7 == 0) {
          metadatas.add(_metadataDao.getByKeyAndValue("datatype", "law"));
          metadatas.add(_metadataDao.getByKeyAndValue("lang", "de"));
        }

        StartUrl startUrl = new StartUrl();
        startUrl.setProvider(provider);
        startUrl.setUrl("http://www." + i + ".com/index.html");

        LimitUrl limitUrl = new LimitUrl();
        limitUrl.setProvider(provider);
        limitUrl.setUrl("http://www." + i + ".com/limit");
        limitUrl.setMetadatas(metadatas);

        ExcludeUrl excludeUrl = new ExcludeUrl();
        excludeUrl.setProvider(provider);
        excludeUrl.setUrl("http://www." + i + ".com/exclude");

        startUrl.addLimitUrl(limitUrl);
        startUrl.addExcludeUrl(excludeUrl);

        _limitUrlDao.makePersistent(limitUrl);
        _excludeUrlDao.makePersistent(excludeUrl);
        _startUrlDao.makePersistent(startUrl);
        Thread.sleep(500);

      }
      _transactionService.commitTransaction();
      _transactionService.close();

    }

  }

  private void createTestData() {
    if ("dev".equals(System.getProperty("deployMode"))) {
      _transactionService.beginTransaction();
      if (!_partnerDao.exists("devPartner")) {
        Partner partner = new Partner();
        partner.setShortName("devPa");
        partner.setName("devPartner");
        _partnerDao.makePersistent(partner);
      }

      Partner partner = _partnerDao.getByName("devPartner");
      if (!_providerDao.exists("devProvider")) {
        Provider provider = new Provider();
        provider.setShortName("devPr");
        provider.setName("devProvider");
        provider.setPartner(partner);
        _providerDao.makePersistent(provider);
      }

      _transactionService.commitTransaction();
      _transactionService.close();
    }
  }

  private void createMetadatasIfNotExists(List<Metadata> metadatas) {
    _transactionService.beginTransaction();
    for (Metadata metadata : metadatas) {
      if (!_metadataDao.exists(metadata.getMetadataKey(), metadata
          .getMetadataValue())) {
        _metadataDao.makePersistent(metadata);
      }
    }
    _transactionService.commitTransaction();
    _transactionService.close();
  }
}
