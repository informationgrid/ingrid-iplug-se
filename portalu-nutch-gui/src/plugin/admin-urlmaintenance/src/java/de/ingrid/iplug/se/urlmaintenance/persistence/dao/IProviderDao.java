package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public interface IProviderDao extends IDao<Provider> {

  Provider getByName(String string);

  boolean exists(String name);

}
