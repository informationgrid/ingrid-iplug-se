package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class UrlDao extends Dao<Url> implements IUrlDao {

  public UrlDao(TransactionService transactionService) {
    super(Url.class, transactionService);
  }

}
