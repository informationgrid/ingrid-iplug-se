package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public interface IProviderDao extends IDao<Provider> {

  Provider getByName(String string);

  Provider getByNameAndPartner(String string, Partner partner);

  Provider getByShortName(String string);

  Provider getByShortNameAndPartner(String string, Partner partner);
  
  boolean exists(String name);

  boolean exists(String name, Partner partner);

  boolean existsByShortName(String name);

  boolean existsByShortName(String name, Partner partner);

}
