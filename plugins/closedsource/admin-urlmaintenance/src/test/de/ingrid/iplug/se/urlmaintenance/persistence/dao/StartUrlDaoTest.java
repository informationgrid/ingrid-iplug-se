package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class StartUrlDaoTest extends TestCase {

  public void testCreate() throws Exception {

    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();
    IStartUrlDao startUrlDao = new StartUrlDao(transactionService);

    List<StartUrl> all = startUrlDao.getAll();
    for (StartUrl startUrl : all) {
      System.out.println(startUrl.getUrl());
    }

    transactionService.commitTransaction();
    transactionService.close();

    // List<Url> all = urlDao.getAll();
    // System.out.println(all);
  }
}
