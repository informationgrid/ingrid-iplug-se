package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

public interface IStartUrlDao extends IDao<StartUrl> {

  public static enum OrderBy {
    TIMESTAMP, URL
  }

  Long countByProvider(Provider byName);

  List<StartUrl> getByProvider(Provider provider, int start, int length,
      OrderBy orderBy);

}
