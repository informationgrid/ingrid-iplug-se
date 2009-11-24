package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public interface IPartnerDao extends IDao<Partner> {

  Partner getByName(String string);

  boolean exists(String name);

  void removeProvider(Partner partner, Provider provider);
}
