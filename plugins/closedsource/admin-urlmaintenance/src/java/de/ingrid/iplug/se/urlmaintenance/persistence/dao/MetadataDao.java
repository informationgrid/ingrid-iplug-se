package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import javax.persistence.Query;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class MetadataDao extends Dao<Metadata> implements IMetadataDao {

  public MetadataDao(TransactionService transactionService) {
    super(Metadata.class, transactionService);
  }

  public Metadata getByKeyAndValue(String key, String value) {
    Query query = _transactionService
        .createNamedQuery("getMetadataByKeyAndValue");
    query.setParameter("key", key);
    query.setParameter("value", value);
    return (Metadata) query.getSingleResult();
  }

}
