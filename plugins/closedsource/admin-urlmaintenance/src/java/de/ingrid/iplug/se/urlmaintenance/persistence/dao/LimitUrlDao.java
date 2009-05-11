package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class LimitUrlDao extends Dao<LimitUrl> implements ILimitUrlDao {

  @Autowired
  public LimitUrlDao(TransactionService transactionService) {
    super(LimitUrl.class, transactionService);
  }

}
