package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class PartnerDao extends Dao<Partner> implements IPartnerDao {

  @Autowired
  public PartnerDao(TransactionService transactionService) {
    super(Partner.class, transactionService);
  }

  @Override
  public Partner getByName(String name) {
    Query query = _transactionService.createNamedQuery("getPartnerByName");
    query.setParameter("name", name);
    return (Partner) query.getSingleResult();
  }

  @Override
  public boolean exists(String name) {
    Query query = _transactionService.createNamedQuery("getPartnerByName");
    query.setParameter("name", name);
    return !query.getResultList().isEmpty();
  }

}
