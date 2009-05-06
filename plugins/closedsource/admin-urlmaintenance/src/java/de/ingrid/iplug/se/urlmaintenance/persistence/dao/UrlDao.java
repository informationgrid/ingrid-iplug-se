package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

@Service
public class UrlDao extends Dao<Url> implements IUrlDao {

  public UrlDao(TransactionService transactionService) {
    super(Url.class, transactionService);
  }

}
