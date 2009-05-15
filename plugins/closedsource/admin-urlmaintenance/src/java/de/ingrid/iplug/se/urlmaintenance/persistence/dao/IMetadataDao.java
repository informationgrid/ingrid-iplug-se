package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;

public interface IMetadataDao extends IDao<Metadata> {

  Metadata getByKeyAndValue(String key, String value);

  boolean exists(String key, String value);

}
