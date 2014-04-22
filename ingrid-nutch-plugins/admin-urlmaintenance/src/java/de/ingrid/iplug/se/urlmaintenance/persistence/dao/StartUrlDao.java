package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
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
  public StartUrlDao(final TransactionService transactionService) {
    super(StartUrl.class, transactionService);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StartUrl> getByProvider(final Provider provider, final int start, final int length,
      final OrderBy orderBy) {
    String namedQuery = null;
    switch (orderBy) {
    case CREATED_ASC:
      namedQuery = "getAllUrlsByProviderOrderByCreatedAsc";
      break;
    case CREATED_DESC:
      namedQuery = "getAllUrlsByProviderOrderByCreatedDesc";
      break;
    case UPDATED_ASC:
      namedQuery = "getAllUrlsByProviderOrderByUpdatedAsc";
      break;
    case UPDATED_DESC:
      namedQuery = "getAllUrlsByProviderOrderByUpdatedDesc";
      break;
    case URL_ASC:
      namedQuery = "getAllUrlsByProviderOrderByUrlAsc";
      break;
    case URL_DESC:
      namedQuery = "getAllUrlsByProviderOrderByUrlDesc";
      break;
    default:
      break;
    }
    final Query query = transactionService.createNamedQuery(namedQuery);
    query.setParameter("id", provider.getId());
    query.setFirstResult(start);
    query.setMaxResults(length);
    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<StartUrl> getByProviderAndMetadatas(final Provider provider,
      final List<Metadata> metadatas, final int start, final int length, final OrderBy orderBy) {
    // we cant use namedqueries with an 'in expression' because jpa does not
    // support query.setParameterList. so we have to implement the query inside
    // the dao
    String orderQuery = null;
    switch (orderBy) {
    case CREATED_ASC:
      orderQuery = "ORDER BY su.created asc";
      break;
    case CREATED_DESC:
      orderQuery = "ORDER BY su.created desc";
      break;
    case UPDATED_ASC:
      orderQuery = "ORDER BY su.updated asc";
      break;
    case UPDATED_DESC:
      orderQuery = "ORDER BY su.updated desc";
      break;
    case URL_ASC:
      orderQuery = "ORDER BY su.url asc";
      break;
    case URL_DESC:
      orderQuery = "ORDER BY su.url desc";
      break;
    default:
      break;
    }

    // init query
    String q = "SELECT DISTINCT su FROM StartUrl su JOIN su.limitUrls lu ";

    // join metadatas in separately variables
    for (final Metadata metadata : metadatas) {
      q += " JOIN lu.metadatas md" + metadata.getId();
    }

    // set parameter to every variable
    // separate use of language since it makes more sense to use OR-query
    q += " WHERE ";
    List<Long> languages = new ArrayList<Long>();
    for (final Metadata metadata : metadatas) {
      // collect languages
      if (metadata.getMetadataKey().equals("lang"))
        languages.add(metadata.getId());
      else
        q += " md" + metadata.getId() + ".id = :md" + metadata.getId() + " AND ";
    }

    // put the languages into the query connected with OR
    if (languages.size() > 0 ) {
        q += "(";
        for (int i = 0; i < languages.size(); i++) {
            q += " md" + languages.get(i) + ".id = :md" + languages.get(i);
            // add OR-connection if there's another language 
            if (i < (languages.size()-1))
              q += " OR ";
        }
        q += ") AND";
    }
    
    // end query with provider
    q += " su.provider.id = :providerId";
    // do not display deleted urls
    q += " AND su.deleted is NULL " + orderQuery;
    
    final Query query = transactionService.createQuery(q);

    // fill query with metadata id's
    for (final Metadata metadata : metadatas) {
      query.setParameter("md" + metadata.getId(), metadata.getId());
    }

    query.setParameter("providerId", provider.getId());
    query.setFirstResult(start);
    query.setMaxResults(length);
    return query.getResultList();

  }

  @Override
  public Long countByProvider(final Provider provider) {
    final Query query = transactionService.createNamedQuery("countByProvider");
    query.setParameter("id", provider.getId());
    return (Long) query.getSingleResult();
  }

  @Override
  public Long countByProviderAndMetadatas(final Provider provider,
      final List<Metadata> metadatas) {

    // init query
    String q = "SELECT COUNT(DISTINCT su) FROM StartUrl su JOIN su.limitUrls lu ";

    // join metadatas in separately variables
    for (final Metadata metadata : metadatas) {
      q += " JOIN lu.metadatas md" + metadata.getId();
    }

    // set parameter to every variable
    q += " WHERE ";
    for (final Metadata metadata : metadatas) {
      q += " md" + metadata.getId() + ".id = :md" + metadata.getId() + " AND ";
    }

    // end query with provider
    q += " su.provider.id = :providerId";
    // do not display deleted urls
    q += " AND su.deleted is NULL";

    final Query query = transactionService.createQuery(q);

    // fill query with metadata id's
    for (final Metadata metadata : metadatas) {
      query.setParameter("md" + metadata.getId(), metadata.getId());
    }

    query.setParameter("providerId", provider.getId());
    return (Long) query.getSingleResult();
  }

  @SuppressWarnings("unchecked")
  public List<StartUrl> getByUrl(final String url, final Serializable providerId) {
    final String q = "SELECT DISTINCT su FROM StartUrl su WHERE su.url = :url and su.provider.id = :providerId and su.deleted is NULL";
    final Query query = transactionService.createQuery(q);
    query.setParameter("url", url);
    query.setParameter("providerId", providerId);
    return query.getResultList();
  }
}
