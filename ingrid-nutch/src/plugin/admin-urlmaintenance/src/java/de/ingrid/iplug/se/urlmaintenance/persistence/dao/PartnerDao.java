package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class PartnerDao extends Dao<Partner> implements IPartnerDao {

  private final IProviderDao providerDao;

  @Autowired
  public PartnerDao(TransactionService transactionService, IProviderDao providerDao) {
    super(Partner.class, transactionService);
    this.providerDao = providerDao;
  }

  @Override
  public Partner getByName(String name) {
    Query query = transactionService.createNamedQuery("getPartnerByName");
    query.setParameter("name", name);
    return (Partner) query.getSingleResult();
  }

  @Override
  public boolean exists(String name) {
    Query query = transactionService.createNamedQuery("getPartnerByName");
    query.setParameter("name", name);
    return !query.getResultList().isEmpty();
  }

  @Override
  public void removeProvider(Partner partner, Provider provider) {
    if (!partner.getProviders().contains(provider)) {
      throw new IllegalArgumentException("The partner '" + partner.getName() + "' does not contain the given provider '" + provider.getName()
          + "' to remove.");
    }

    partner.getProviders().remove(provider);
    makePersistent(partner);
    providerDao.makeTransient(provider);
  }

@Override
public boolean existsByShortName(String name) {
    Query query = transactionService.createNamedQuery("getPartnerByShortName");
    query.setParameter("shortName", name);
    return !query.getResultList().isEmpty();
}

@Override
public Partner getByShortName(String string) {
    Query query = transactionService.createNamedQuery("getPartnerByShortName");
    query.setParameter("shortName", string);
    return (Partner) query.getSingleResult();
}

}
