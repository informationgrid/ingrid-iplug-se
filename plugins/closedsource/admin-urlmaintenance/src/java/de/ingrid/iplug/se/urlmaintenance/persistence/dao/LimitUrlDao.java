package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class LimitUrlDao extends Dao<LimitUrl> implements ILimitUrlDao {

  public LimitUrlDao(TransactionService transactionService) {
    super(LimitUrl.class, transactionService);
  }

}
