package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
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

    // init query
    String q = "SELECT count(cu) FROM CatalogUrl cu ";

    // join metadatas in separately variables
    for (Metadata metadata : metadatas) {
      q += " JOIN cu._metadatas md" + metadata.getId();
    }

    // set parameter to every variable
    q += " WHERE ";
    for (Metadata metadata : metadatas) {
      q += " md" + metadata.getId() + "._id = :md" + metadata.getId() + " AND ";
    }

    // end query with provider
    q += " cu._provider._id = :providerId";
    // do not display deleted urls
    q += " AND cu._deleted is NULL";

    Query query = _transactionService.createQuery(q);

    // fill query with metadata id's
    for (Metadata metadata : metadatas) {
      query.setParameter("md" + metadata.getId(), metadata.getId());
    }

    query.setParameter("providerId", provider.getId());
    return (Long) query.getSingleResult();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<CatalogUrl> getByProviderAndMetadatas(Provider provider,
      List<Metadata> metadatas, int firstResult, int maxResult, OrderBy orderBy) {
    // we cant use namedqueries with an 'in expression' because jpa does not
    // support query.setParameterList. so we have to implement the query inside
    // the dao
    String orderQuery = null;
    switch (orderBy) {
    case CREATED_ASC:
      orderQuery = "ORDER BY cu._created asc";
      break;
    case CREATED_DESC:
      orderQuery = "ORDER BY cu._created desc";
      break;
    case UPDATED_ASC:
      orderQuery = "ORDER BY cu._updated asc";
      break;
    case UPDATED_DESC:
      orderQuery = "ORDER BY cu._updated desc";
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

    // init query
    String q = "SELECT DISTINCT cu FROM CatalogUrl cu ";

    // join metadatas in separately variables
    for (Metadata metadata : metadatas) {
      q += " JOIN cu._metadatas md" + metadata.getId();
    }

    // set parameter to every variable
    q += " WHERE ";
    List<Long> languages = new ArrayList<Long>();
    for (Metadata metadata : metadatas) {
      // collect languages
      if (metadata.getMetadataKey().equals("lang"))
        languages.add(metadata.getId());
      else
        q += " md" + metadata.getId() + "._id = :md" + metadata.getId() + " AND ";
    }
    
    // put the languages into the query connected with OR
    if (languages.size() > 0 ) {
        q += "(";
        for (int i = 0; i < languages.size(); i++) {
            q += " md" + languages.get(i) + "._id = :md" + languages.get(i);
            // add OR-connection if there's another language 
            if (i < (languages.size()-1))
              q += " OR ";
        }
        q += ") AND";
    }

    // end query with provider
    q += " cu._provider._id = :providerId";
    // do not display deleted urls
    q += " AND cu._deleted is NULL " + orderQuery;

    Query query = _transactionService.createQuery(q);

    // fill query with metadata id's
    for (Metadata metadata : metadatas) {
      query.setParameter("md" + metadata.getId(), metadata.getId());
    }
    
    query.setParameter("providerId", provider.getId());
    query.setFirstResult(firstResult);
    query.setMaxResults(maxResult);
    return query.getResultList();
  }
  
  @SuppressWarnings("unchecked")
  public List<CatalogUrl> getByUrl(final String url, final Serializable providerId) {
    final String q = "SELECT DISTINCT cu FROM CatalogUrl cu WHERE cu._deleted IS NULL and cu._url = :url and cu._provider._id = :providerId";
    final Query query = _transactionService.createQuery(q);
    query.setParameter("url", url);
    query.setParameter("providerId", providerId);
    return query.getResultList();
  }
}
