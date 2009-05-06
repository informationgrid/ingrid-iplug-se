package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class StartUrlDao extends Dao<StartUrl> implements IStartUrlDao {

  public StartUrlDao(TransactionService transactionService) {
    super(StartUrl.class, transactionService);
  }

}
