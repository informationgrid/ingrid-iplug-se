package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import java.util.List;

import org.mockito.MockitoAnnotations;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.DaoTest;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.LimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.StartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class TestStartUrlCommand extends DaoTest {

  private TransactionService _transactionService;
//  private ProviderDao _providerDao;
//  private IPartnerDao _partnerDao;
  private IStartUrlDao _startUrlDao;
  private ILimitUrlDao _limitUrlDao;
  private IExcludeUrlDao _excludeUrlDao;
  private StartUrlCommand _command;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
    
    _transactionService = new TransactionService();
    _transactionService.beginTransaction();

//    _providerDao = new ProviderDao(_transactionService);
//    _partnerDao = new PartnerDao(_transactionService, _providerDao);
    _startUrlDao = new StartUrlDao(_transactionService);
    _limitUrlDao = new LimitUrlDao(_transactionService);
    _excludeUrlDao = new ExcludeUrlDao(_transactionService);
    _command = new StartUrlCommand(_startUrlDao, _limitUrlDao, _excludeUrlDao);
  }

  public void testWrite_StartUrlAndLimitUrl() throws Exception{
    _transactionService.close();
    
    Provider provider = createProviderInSeparateTransaction("partner", "provider");
    
    _command.setProvider(provider);
    _command.setUrl("www.starturl.de");
    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    limitUrlCommand.setUrl("www.limiturl.de");
    limitUrlCommand.setProvider(provider);
    _command.addLimitUrlCommand(limitUrlCommand);
    
    _transactionService.beginTransaction();
    
    _command.write();
    _startUrlDao.flipTransaction();
    
    List<StartUrl> startUrlsFromDb = _startUrlDao.getAll();
    List<LimitUrl> limitUrlsFromDb = _limitUrlDao.getAll();
    
    assertEquals(1, startUrlsFromDb.size());
    assertEquals(1, limitUrlsFromDb.size());
    assertEquals(_command.getLimitUrlCommands().get(0).getUrl(), startUrlsFromDb.get(0).getLimitUrls().get(0).getUrl());
    
    _transactionService.commitTransaction();
    _transactionService.close();
  }
}