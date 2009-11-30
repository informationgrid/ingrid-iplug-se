package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.DaoTest;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public class LimitUrlWrapperTest extends DaoTest {

  @Mock
  private ILimitUrlDao _limitUrlDao;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    MockitoAnnotations.initMocks(this);
  }

  public void testRead() throws Exception {

    LimitUrlCommand limitUrlCommand = new LimitUrlCommand(_limitUrlDao);
    Provider provider = createProviderInSeparateTransaction("partner", "provider");
    Metadata lang = createMetadata("lang", "de");
    LimitUrl model = new LimitUrl();
    model.setUrl("http://www.101tec.com");
    model.addMetadata(lang);
    model.setProvider(provider);

    limitUrlCommand.read(model);
    assertEquals(model.getUrl(), limitUrlCommand.getUrl());
    assertEquals(model.getMetadatas(), limitUrlCommand.getMetadatas());
  }

  public void testWrite() throws Exception {
    Metadata lang = createMetadata("lang", "de");
    Provider provider = createProviderInSeparateTransaction("partner", "provider");

    LimitUrlCommand command = new LimitUrlCommand(_limitUrlDao);
    command.setUrl("http://www.101tec.com");
//    command.setId(23L);
    command.setProvider(provider);
    command.addMetadata(lang);

    LimitUrl limitUrl = new LimitUrl();
    limitUrl.setProvider(provider);

    Mockito.when(_limitUrlDao.getById(23L)).thenReturn(limitUrl);
    LimitUrl limitUrl2 = command.write();
    assertEquals(command.getUrl(), limitUrl2.getUrl());
    assertEquals(command.getMetadatas(), limitUrl2.getMetadatas());
    assertEquals(command.getProvider(), limitUrl2.getProvider());

  }
}
