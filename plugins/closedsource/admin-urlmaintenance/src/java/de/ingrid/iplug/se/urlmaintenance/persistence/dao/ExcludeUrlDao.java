package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class ExcludeUrlDao extends Dao<ExcludeUrl> implements IExcludeDao {

  @Autowired
  public ExcludeUrlDao(TransactionService transactionService) {
    super(ExcludeUrl.class, transactionService);
  }

}
