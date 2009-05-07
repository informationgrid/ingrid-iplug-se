package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class PartnerDaoTest extends DaoTest {

  public void testCreatePartner() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Partner partner = new Partner();
    partner.setName("foo");
    IPartnerDao dao = new PartnerDao(transactionService);
    dao.makePersistent(partner);
    transactionService.commitTransaction();
    transactionService.close();

    transactionService.beginTransaction();
    List<Partner> all = dao.getAll();
    assertEquals(1, all.size());
    Partner partner2 = all.get(0);
    assertEquals(partner.getName(), partner2.getName());
  }
  
  public void testDeletePartner() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Partner partner = new Partner();
    partner.setName("foo");
    IPartnerDao dao = new PartnerDao(transactionService);
    dao.makePersistent(partner);
    transactionService.commitTransaction();
    transactionService.close();
    transactionService.beginTransaction();
    List<Partner> all = dao.getAll();
    assertEquals(1, all.size());
    dao.makeTransient(all.get(0));
    transactionService.commitTransaction();
    transactionService.close();
    transactionService.beginTransaction();
    all = dao.getAll();
    assertEquals(0, all.size());
    transactionService.commitTransaction();
    transactionService.close();
  }
  
  
  public void testGetByName() throws Exception {
    TransactionService transactionService = new TransactionService();
    transactionService.beginTransaction();

    Partner partner = new Partner();
    partner.setName("foo");
    IPartnerDao dao = new PartnerDao(transactionService);
    dao.makePersistent(partner);
    transactionService.commitTransaction();
    transactionService.close();
    
    transactionService.beginTransaction();
    Partner partner2 = dao.getByName("foo");
    assertEquals(partner.getId(), partner2.getId());
  }
}
