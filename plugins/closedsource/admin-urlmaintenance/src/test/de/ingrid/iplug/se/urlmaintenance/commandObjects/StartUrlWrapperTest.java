package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.DaoTest;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.StartUrl;

public class StartUrlWrapperTest extends DaoTest {

  @Mock
  private IStartUrlDao _startUrlDao;

  @Mock
  private ILimitUrlDao _limitUrlDao;

  @Mock
  private IExcludeUrlDao _excludeUrlDao;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
  }

  public void testRead() throws Exception {
    StartUrlCommand startUrlCommand = new StartUrlCommand(_startUrlDao,
        _limitUrlDao, _excludeUrlDao);

    Provider provider = createProvider("partner", "provider");
    StartUrl model = new StartUrl();
    model.setUrl("http://www.101tec.com");
    model.setProvider(provider);

    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setUrl("http://www.101tec.com/limit");
    limitUrl.setProvider(provider);
    model.addLimitUrl(limitUrl);

    startUrlCommand.read(model);
    assertEquals(model.getUrl(), startUrlCommand.getUrl());
  }

  public void testWrite() throws Exception {
    Metadata lang = createMetadata("lang", "de");
    Provider provider = createProvider("partner", "provider");

    // limit url command
    LimitUrlCommand command = new LimitUrlCommand(_limitUrlDao);
    command.setUrl("http://www.101tec.com");
    command.setId(23L);
    command.setProvider(provider);
    command.addMetadata(lang);

    StartUrlCommand startUrlCommand = new StartUrlCommand(_startUrlDao,
        _limitUrlDao, _excludeUrlDao);
    startUrlCommand.setUrl("http://www.101tec.com/index");
    startUrlCommand.setId(23L);
    startUrlCommand.addLimitUrlCommand(command);

    Mockito.when(_startUrlDao.getById(23L)).thenReturn(new StartUrl());
    Mockito.when(_limitUrlDao.getById(23L)).thenReturn(new LimitUrl());

    StartUrl startUrl = startUrlCommand.write();
    assertEquals(startUrlCommand.getUrl(), startUrl.getUrl());
    assertTrue(startUrl.getLimitUrls().size() == 1);

  }
}
