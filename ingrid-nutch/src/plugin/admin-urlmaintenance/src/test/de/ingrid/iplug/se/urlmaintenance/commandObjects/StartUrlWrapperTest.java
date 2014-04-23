package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import java.util.List;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.DaoTest;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.LimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.StartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class StartUrlWrapperTest extends DaoTest {

  private TransactionService _transactionService;

  private IStartUrlDao _startUrlDao;
  private ILimitUrlDao _limitUrlDao;
  private IExcludeUrlDao _excludeUrlDao;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    _transactionService = new TransactionService();
    _transactionService.beginTransaction();

    _startUrlDao = new StartUrlDao(_transactionService);
    _limitUrlDao = new LimitUrlDao(_transactionService);
    _excludeUrlDao = new ExcludeUrlDao(_transactionService);
  }

  public void testRead() throws Exception {
    _transactionService.close();
    StartUrlCommand startUrlCommand = new StartUrlCommand(_startUrlDao,
        _limitUrlDao, _excludeUrlDao);

    List<Provider> providers = createProviderInSeparateTransaction("pa", "partner", new String[] { "pr" }, new String[] { "provider" });
    StartUrl model = new StartUrl();
    model.setUrl("http://www.101tec.com");
    model.setProvider(providers.get(0));

    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setUrl("http://www.101tec.com/limit");
    limitUrl.setProvider(providers.get(0));
    model.addLimitUrl(limitUrl);
    _startUrlDao.makePersistent(model);
    _startUrlDao.flipTransaction();

    startUrlCommand.read(model);
    assertEquals(model.getUrl(), startUrlCommand.getUrl());
    assertEquals(1, startUrlCommand.getLimitUrlCommands().size());
    _startUrlDao.flipTransaction();
    
    // re-read the startUrlCommand and verify the limit url
    StartUrl startUrlFromDb = _startUrlDao.getById(model.getId());
    startUrlCommand.read(startUrlFromDb);
    assertEquals(1, startUrlCommand.getLimitUrlCommands().size());

    _transactionService.commitTransaction();
    _transactionService.close();
  }

  public void testWrite_OverwriteExistingStartAndLimitUrls() throws Exception {
    _transactionService.close();
    Metadata lang = createMetadata("lang", "de");
    List<Provider> providers = createProviderInSeparateTransaction("pa", "partner", new String[] { "pr" }, new String[] { "provider" });

    _transactionService.beginTransaction();
    StartUrl startUrl = new StartUrl();
    startUrl.setUrl("http://www.101tec.com/old");
    startUrl.setProvider(providers.get(0));
    _startUrlDao.makePersistent(startUrl);
    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setUrl("http://www.101tec.com/blah");
    limitUrl.setProvider(providers.get(0));
    limitUrl.addMetadata(lang);
    _limitUrlDao.makePersistent(limitUrl);
    _limitUrlDao.flipTransaction();

    // limit url command
    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    limitUrlCommand.setUrl("http://www.101tec.com");
//    limitUrlCommand.setCommandId(limitUrl.getId());
    limitUrlCommand.setProvider(providers.get(0));
    limitUrlCommand.addMetadata(lang);
    
    StartUrlCommand startUrlCommand = new StartUrlCommand(_startUrlDao,
        _limitUrlDao, _excludeUrlDao);
    startUrlCommand.setUrl("http://www.101tec.com/index");
    startUrlCommand.setId(startUrl.getId());
    startUrlCommand.addLimitUrlCommand(limitUrlCommand);

    StartUrl startUrlWritten = startUrlCommand.write();
    assertEquals(startUrlCommand.getUrl(), startUrlWritten.getUrl());
    assertEquals(1, startUrlWritten.getLimitUrls().size());
    assertEquals(limitUrlCommand.getUrl(), startUrlWritten.getLimitUrls().get(0).getUrl());
    
    _transactionService.commitTransaction();
    _transactionService.close();
  }
  
  public void testWrite_StartAndLinitUrlsAlreadyExists() throws Exception{
    _transactionService.close();
    Metadata lang = createMetadata("lang", "de");
    List<Provider> providers = createProviderInSeparateTransaction("pa", "partner", new String[] { "pr" }, new String[] { "provider" });

    _transactionService.beginTransaction();
    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setUrl("http://www.101tec.com");
    limitUrl.setProvider(providers.get(0));
    limitUrl.addMetadata(lang);
    _limitUrlDao.makePersistent(limitUrl);
    StartUrl startUrl = new StartUrl();
    startUrl.setUrl("http://www.101tec.com/index");
    startUrl.addLimitUrl(limitUrl);
    startUrl.setProvider(providers.get(0));
    _startUrlDao.makePersistent(startUrl);
    _limitUrlDao.flipTransaction();
    
    // limit url and start url commands
    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    limitUrlCommand.setUrl(limitUrl.getUrl());
//    limitUrlCommand.setCommandId(limitUrl.getId());
    limitUrlCommand.setProvider(providers.get(0));
    limitUrlCommand.addMetadata(lang);
    StartUrlCommand startUrlCommand = new StartUrlCommand(_startUrlDao, _limitUrlDao, _excludeUrlDao);
    startUrlCommand.setUrl(startUrl.getUrl());
    startUrlCommand.setId(startUrl.getId());
    startUrlCommand.addLimitUrlCommand(limitUrlCommand);

    _transactionService.beginTransaction();
    assertEquals(1, _limitUrlDao.getAll().size());
    _limitUrlDao.flipTransaction();
    
    StartUrl startUrlWritten = startUrlCommand.write();

//    _transactionService.beginTransaction();
    assertEquals(1, _limitUrlDao.getAll().size());
//    _limitUrlDao.flipTransaction();

    assertEquals(startUrlCommand.getUrl(), startUrlWritten.getUrl());
    assertEquals(1, startUrlWritten.getLimitUrls().size());

    _transactionService.commitTransaction();
    _transactionService.close();
  }
}
