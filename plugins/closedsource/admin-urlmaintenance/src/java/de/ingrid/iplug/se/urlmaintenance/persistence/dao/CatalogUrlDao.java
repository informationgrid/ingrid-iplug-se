package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class CatalogUrlDao extends Dao<CatalogUrl> implements ICatalogUrlDao {

  @Autowired
  public CatalogUrlDao(TransactionService transactionService) {
    super(CatalogUrl.class, transactionService);
  }

  @Override
  public Long countByProviderAndMetadatas(Provider provider,
      List<Metadata> metadatas) {
    Collection<Long> idList = new ArrayList<Long>();
    for (Metadata metadata : metadatas) {
      idList.add(metadata.getId());
    }

    Query query = _transactionService
        .createQuery("SELECT count(cu) FROM CatalogUrl cu JOIN cu._metadatas md WHERE cu._provider._id = :providerId AND md._id in ("
            + idList + ")");
    query.setParameter("providerId", provider.getId());
    return (Long) query.getSingleResult();
  }

  @Override
  public List<CatalogUrl> getByProviderAndMetadatas(Provider provider,
      List<Metadata> metadatas, int firstResult, int maxResult, OrderBy orderBy) {
    // we cant use namedqueries with an 'in expression' because jpa does not
    // support query.setParameterList. so we have to implement the query inside
    // the dao
    String orderQuery = null;
    switch (orderBy) {
    case TIMESTAMP_ASC:
      orderQuery = "ORDER BY cu._timeStamp asc";
      break;
    case TIMESTAMP_DESC:
      orderQuery = "ORDER BY cu._timeStamp desc";
      break;
    case URL_ASC:
      orderQuery = "ORDER BY cu._url asc";
      break;
    case URL_DESC:
      orderQuery = "ORDER BY cu._url desc";
      break;
    default:
      break;
    }

    Collection<Long> idList = new ArrayList<Long>();
    for (Metadata metadata : metadatas) {
      idList.add(metadata.getId());
    }

    Query query = _transactionService
        .createQuery("SELECT cu FROM CatalogUrl cu JOIN cu._metadatas md WHERE cu._provider._id = :providerId AND md._id in ("
            + idList + ") " + orderQuery);
    query.setParameter("providerId", provider.getId());
    query.setFirstResult(firstResult);
    query.setMaxResults(maxResult);
    return query.getResultList();
  }

}
