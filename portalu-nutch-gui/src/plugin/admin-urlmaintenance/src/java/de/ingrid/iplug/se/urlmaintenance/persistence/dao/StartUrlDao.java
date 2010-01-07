package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.io.Serializable;
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
    final Query query = _transactionService.createNamedQuery(namedQuery);
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
      orderQuery = "ORDER BY su._created asc";
      break;
    case CREATED_DESC:
      orderQuery = "ORDER BY su._created desc";
      break;
    case UPDATED_ASC:
      orderQuery = "ORDER BY su._updated asc";
      break;
    case UPDATED_DESC:
      orderQuery = "ORDER BY su._updated desc";
      break;
    case URL_ASC:
      orderQuery = "ORDER BY su._url asc";
      break;
    case URL_DESC:
      orderQuery = "ORDER BY su._url desc";
      break;
    default:
      break;
    }

    // init query
    String q = "SELECT DISTINCT su FROM StartUrl su JOIN su._limitUrls lu ";

    // join metadatas in separately variables
    for (final Metadata metadata : metadatas) {
      q += " JOIN lu._metadatas md" + metadata.getId();
    }

    // set parameter to every variable
    q += " WHERE ";
    for (final Metadata metadata : metadatas) {
      q += " md" + metadata.getId() + "._id = :md" + metadata.getId() + " AND ";
    }

    // end query with provider
    q += " su._provider._id = :providerId  " + orderQuery;

    final Query query = _transactionService.createQuery(q);

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
    final Query query = _transactionService.createNamedQuery("countByProvider");
    query.setParameter("id", provider.getId());
    return (Long) query.getSingleResult();
  }

  @Override
  public Long countByProviderAndMetadatas(final Provider provider,
      final List<Metadata> metadatas) {

    // init query
    String q = "SELECT COUNT(DISTINCT su) FROM StartUrl su JOIN su._limitUrls lu ";

    // join metadatas in separately variables
    for (final Metadata metadata : metadatas) {
      q += " JOIN lu._metadatas md" + metadata.getId();
    }

    // set parameter to every variable
    q += " WHERE ";
    for (final Metadata metadata : metadatas) {
      q += " md" + metadata.getId() + "._id = :md" + metadata.getId() + " AND ";
    }

    // end query with provider
    q += " su._provider._id = :providerId";

    final Query query = _transactionService.createQuery(q);

    // fill query with metadata id's
    for (final Metadata metadata : metadatas) {
      query.setParameter("md" + metadata.getId(), metadata.getId());
    }

    query.setParameter("providerId", provider.getId());
    return (Long) query.getSingleResult();
  }

  @SuppressWarnings("unchecked")
  public List<StartUrl> getByUrl(final String url, final Serializable providerId) {
    final String q = "SELECT DISTINCT su FROM StartUrl su WHERE su._url = :url and su._provider._id = :providerId";
    final Query query = _transactionService.createQuery(q);
    query.setParameter("url", url);
    query.setParameter("providerId", providerId);
    return query.getResultList();
  }
}
