package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class CatalogUrlDao extends Dao<CatalogUrl> implements ICatalogUrlDao {

  @Autowired
  public CatalogUrlDao(TransactionService transactionService) {
    super(CatalogUrl.class, transactionService);
  }

}
