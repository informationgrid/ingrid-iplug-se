package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public interface IPartnerDao extends IDao<Partner> {

  Partner getByName(String string);

  Partner getByShortName(String string);

  boolean exists(String name);

  boolean existsByShortName(String name);
  
  void removeProvider(Partner partner, Provider provider);
}
