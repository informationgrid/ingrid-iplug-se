package de.ingrid.iplug.se.urlmaintenance;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IMetadataDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IPartnerDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;
import de.ingrid.iplug.se.urlmaintenance.service.MetadataService;

@Service
public class DataInjector {

  private final MetadataService _metadataService;
  private final IMetadataDao _metadataDao;
  private final IPartnerDao _partnerDao;
  private final IProviderDao _providerDao;
  private final TransactionService _transactionService;

  @Autowired
  public DataInjector(MetadataService metadataService,
      IMetadataDao metadataDao, IPartnerDao partnerDao,
      IProviderDao providerDao, TransactionService transactionService) {
    _metadataService = metadataService;
    _metadataDao = metadataDao;
    _partnerDao = partnerDao;
    _providerDao = providerDao;
    _transactionService = transactionService;

    createTestData();
    // lang
    List<Metadata> langs = _metadataService.getLang();
    createMetadatasIfNotExists(langs);

    // datatypes
    List<Metadata> datatypes = _metadataService.getDatatypes();
    createMetadatasIfNotExists(datatypes);

    // funct category
    List<Metadata> functCategory = _metadataService.getFunctCategory();
    createMetadatasIfNotExists(functCategory);

    // measure
    List<Metadata> measure = _metadataService.getMeasure();
    createMetadatasIfNotExists(measure);

    // service
    List<Metadata> service = _metadataService.getService();
    createMetadatasIfNotExists(service);

    // topics
    List<Metadata> topics = _metadataService.getTopics();
    createMetadatasIfNotExists(topics);
  }

  private void createTestData() {
    if ("dev".equals(System.getProperty("deployMode"))) {
      _transactionService.beginTransaction();
      if (!_partnerDao.exists("devPartner")) {
        Partner partner = new Partner();
        partner.setName("devPartner");
        _partnerDao.makePersistent(partner);
      }

      Partner partner = _partnerDao.getByName("devPartner");
      if (!_providerDao.exists("devProvider")) {
        Provider provider = new Provider();
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
