package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class MetadataDao extends Dao<Metadata> implements IMetadataDao {

  @Autowired
  public MetadataDao(TransactionService transactionService) {
    super(Metadata.class, transactionService);
  }

  public Metadata getByKeyAndValue(String key, String value) {
    Query query = transactionService
        .createNamedQuery("getMetadataByKeyAndValue");
    query.setParameter("key", key);
    query.setParameter("value", value);
    return (Metadata) query.getSingleResult();
  }

  @Override
  public boolean exists(String key, String value) {
    Query query = transactionService
        .createNamedQuery("getMetadataByKeyAndValue");
    query.setParameter("key", key);
    query.setParameter("value", value);
    return !query.getResultList().isEmpty();
  }

  
  @SuppressWarnings("unchecked")
  @Override
  public List<Metadata> getByKey(String key) {
    Query query = transactionService
        .createNamedQuery("getMetadatasByKey");
    query.setParameter("key", key);
    return query.getResultList();
  }

}
