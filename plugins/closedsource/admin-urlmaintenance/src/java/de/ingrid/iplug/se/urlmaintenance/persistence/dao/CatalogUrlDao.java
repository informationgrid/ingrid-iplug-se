package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class CatalogUrlDao extends Dao<CatalogUrl> implements ICatalogUrlDao {

  public CatalogUrlDao(TransactionService transactionService) {
    super(CatalogUrl.class, transactionService);
  }

}
