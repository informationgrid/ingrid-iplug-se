package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;

public interface IMetadataDao extends IDao<Metadata> {

  Metadata getByKeyAndValue(String key, String value);

  List<Metadata> getByKey(String key);

  boolean exists(String key, String value);

}
