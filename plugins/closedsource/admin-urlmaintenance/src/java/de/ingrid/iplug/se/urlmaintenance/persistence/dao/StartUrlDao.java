package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
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

  @SuppressWarnings("unchecked")
  @Override
  public List<StartUrl> getByProviderAndMetadatas(Provider provider,
      List<Metadata> metadatas, int start, int length, OrderBy orderBy) {
    // we cant use namedqueries with an 'in expression' because jpa does not
    // support query.setParameterList. so we have to implement the query inside
    // the dao
    String orderQuery = null;
    switch (orderBy) {
    case TIMESTAMP:
      orderQuery = "ORDER BY su._timeStamp";
      break;
    case URL:
      orderQuery = "ORDER BY su._url";
      break;
    default:
      break;
    }

    Collection<Long> idList = new ArrayList<Long>();
    for (Metadata metadata : metadatas) {
      idList.add(metadata.getId());
    }

    Query query = _transactionService
        .createQuery("SELECT DISTINCT su FROM StartUrl su JOIN su._limitUrls lu JOIN lu._metadatas md WHERE su._provider._id = :providerId AND md._id in ("
            + idList + ") " + orderQuery);
    query.setParameter("providerId", provider.getId());
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

  @Override
  public Long countByProviderAndMetadatas(Provider provider,
      List<Metadata> metadatas) {
    Collection<Long> idList = new ArrayList<Long>();
    for (Metadata metadata : metadatas) {
      idList.add(metadata.getId());
    }

    Query query = _transactionService
        .createQuery("SELECT count(DISTINCT su) FROM StartUrl su JOIN su._limitUrls lu JOIN lu._metadatas md WHERE su._provider._id = :providerId AND md._id in ("
            + idList + ")");
    query.setParameter("providerId", provider.getId());
    return (Long) query.getSingleResult();
  }

}
