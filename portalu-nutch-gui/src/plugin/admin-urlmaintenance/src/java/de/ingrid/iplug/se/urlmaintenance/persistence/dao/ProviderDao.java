package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class ProviderDao extends Dao<Provider> implements IProviderDao {

  @Autowired
  public ProviderDao(TransactionService transactionService) {
    super(Provider.class, transactionService);
  }

  @Override
  public Provider getByName(String name) {
    Query query = _transactionService.createNamedQuery("getProviderByName");
    query.setParameter("name", name);
    return (Provider) query.getSingleResult();
  }

  @Override
  public Provider getByNameAndPartner(String name, Partner partner) {
    Query query = _transactionService.createNamedQuery("getProviderByNameAndPartner");
    query.setParameter("name", name);
    query.setParameter("partner", partner);
    return (Provider) query.getSingleResult();
  }

  @Override
  public boolean exists(String name) {
    Query query = _transactionService.createNamedQuery("getProviderByName");
    query.setParameter("name", name);
    return !query.getResultList().isEmpty();
  }

  @Override
  public boolean exists(String name, Partner partner) {
    Query query = _transactionService.createNamedQuery("getProviderByNameAndPartner");
    query.setParameter("name", name);
    query.setParameter("partner", partner);
    return !query.getResultList().isEmpty();
  }

}
