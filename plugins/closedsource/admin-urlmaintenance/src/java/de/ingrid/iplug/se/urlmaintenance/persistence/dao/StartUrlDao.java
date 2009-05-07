package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import javax.persistence.Query;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class StartUrlDao extends Dao<StartUrl> implements IStartUrlDao {

  public StartUrlDao(TransactionService transactionService) {
    super(StartUrl.class, transactionService);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StartUrl> getByProvider(Provider provider, int start, int length) {
    Query query = _transactionService.createNamedQuery("getAllUrlsByProvider");
    query.setParameter("id", provider.getId());
    query.setFirstResult(start);
    query.setMaxResults(length);
    return query.getResultList();
  }

}
