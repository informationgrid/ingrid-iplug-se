package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class ExcludeUrlDao extends Dao<ExcludeUrl> implements IExcludeDao {

  public ExcludeUrlDao(TransactionService transactionService) {
    super(ExcludeUrl.class, transactionService);
  }

}
