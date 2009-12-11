package de.ingrid.iplug.se.urlmaintenance;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IExcludeUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ILimitUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Partner;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Provider;

public class DatabaseExportTest extends TestCase {

  @Mock
  private IStartUrlDao _startUrlDao;
  @Mock
  private ICatalogUrlDao _catalogUrlDao;
  @Mock
  private ILimitUrlDao _limitUrlDao;
  @Mock
  private IExcludeUrlDao _excludeUrlDao;
  private Partner _partner;
  
  private DatabaseExport _databaseExport;
  
  public void setUp(){
    MockitoAnnotations.initMocks(this);
    
    _databaseExport = new DatabaseExport(_startUrlDao, _catalogUrlDao, _limitUrlDao, _excludeUrlDao);
    _partner = new Partner();
    _partner.setName("Partner-Test");
    Provider provider = new Provider();
    provider.setName("Provider-Test");
    _partner.addProvider(provider);
  }
  
  public void testMetadataAsStringList(){
    // setup a LimitUrl
    LimitUrl limitUrlA = mock(LimitUrl.class);
    when(limitUrlA.getUrl()).thenReturn("http://www.mytestA.com");
    when(limitUrlA.getMetadatas()).thenReturn(null);
    when(limitUrlA.getMetadatas()).thenReturn(Arrays.asList(new Metadata("key1", "value1"), new Metadata("key2", "value2")));
    when(limitUrlA.getProvider()).thenReturn(_partner.getProviders().get(0));
    
    // setup another LimitUrl
    LimitUrl limitUrlB = mock(LimitUrl.class);
    when(limitUrlB.getUrl()).thenReturn("http://www.mytestB.com");
    when(limitUrlB.getMetadatas()).thenReturn(Arrays.asList(new Metadata("key1", "value1"), new Metadata("key1", "value2")));
    when(limitUrlB.getProvider()).thenReturn(_partner.getProviders().get(0));

    // setup dao for our two urls
    when(_limitUrlDao.getAll()).thenReturn(Arrays.asList(limitUrlA, limitUrlB));
    
    List<String> metadataAsStringList = _databaseExport.metadataAsStringList(_limitUrlDao);
    
    assertEquals(2, metadataAsStringList.size());
    assertEquals("http://www.mytestA.com\tpartner:\tPartner-Test\tprovider:\tProvider-Test\tkey2:\tvalue2\tkey1:\tvalue1\t", metadataAsStringList.get(0));
    assertEquals("http://www.mytestB.com\tpartner:\tPartner-Test\tprovider:\tProvider-Test\tkey1:\tvalue1\tvalue2\t", metadataAsStringList.get(1));
  }
}
