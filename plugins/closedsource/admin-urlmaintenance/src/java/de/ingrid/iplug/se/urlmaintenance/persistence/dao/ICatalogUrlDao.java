package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao.OrderBy;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public interface ICatalogUrlDao extends IDao<CatalogUrl> {

  List<CatalogUrl> getByProviderAndMetadatas(Provider provider,
      List<Metadata> metadatas, int firstResult, int maxResult, OrderBy orderBy);

  Long countByProviderAndMetadatas(Provider provider, List<Metadata> metadatas);

}
