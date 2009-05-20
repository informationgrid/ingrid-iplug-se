package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

public interface IStartUrlDao extends IDao<StartUrl> {

  public static enum OrderBy {
    TIMESTAMP_ASC, TIMESTAMP_DESC, URL_ASC, URL_DESC
  }

  Long countByProvider(Provider provider);

  Long countByProviderAndMetadatas(Provider provider, List<Metadata> metadatas);

  List<StartUrl> getByProvider(Provider provider, int start, int length,
      OrderBy orderBy);

  List<StartUrl> getByProviderAndMetadatas(Provider provider,
      List<Metadata> metadatas, int start, int length, OrderBy orderBy);

}
