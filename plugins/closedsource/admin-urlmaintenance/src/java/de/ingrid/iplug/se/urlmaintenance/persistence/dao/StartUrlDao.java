package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class StartUrlDao extends Dao<StartUrl> implements IStartUrlDao {

  @Autowired
  public StartUrlDao(TransactionService transactionService) {
    super(StartUrl.class, transactionService);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StartUrl> getByProvider(Provider provider, int start, int length,
      OrderBy orderBy) {
    String namedQuery = null;
    switch (orderBy) {
    case TIMESTAMP:
      namedQuery = "getAllUrlsByProviderOrderByTimeStamp";
      break;
    case URL:
      namedQuery = "getAllUrlsByProviderOrderByUrl";
      break;
    default:
      break;
    }
    Query query = _transactionService.createNamedQuery(namedQuery);
    query.setParameter("id", provider.getId());
    query.setFirstResult(start);
    query.setMaxResults(length);
    return query.getResultList();
  }

  @Override
  public Long countByProvider(Provider provider) {
    Query query = _transactionService.createNamedQuery("countByProvider");
    query.setParameter("id", provider.getId());
    return (Long) query.getSingleResult();
  }

}
