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
    _partner.setShortName("paTest");
    _partner.setName("Partner-Test");
    Provider provider = new Provider();
    provider.setShortName("prTest");
    provider.setShortName("prTest");
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
    
    List<String> metadataAsStringList = _databaseExport.metadataAsStringList(_limitUrlDao, true);
    
    assertEquals(2, metadataAsStringList.size());
    assertEquals("http://www.mytestA.com\tpartner:\tpaTest\tprovider:\tprTest\tkey2:\tvalue2\tkey1:\tvalue1\t", metadataAsStringList.get(0));
    assertEquals("http://www.mytestB.com\tpartner:\tpaTest\tprovider:\tprTest\tkey1:\tvalue1\tvalue2\t", metadataAsStringList.get(1));
  }

  public void testRegExpHandling(){
      // setup a LimitUrl
      LimitUrl limitUrlA = mock(LimitUrl.class);
      when(limitUrlA.getUrl()).thenReturn("http://www.mytestA.com");
      when(limitUrlA.getMetadatas()).thenReturn(Arrays.asList(new Metadata("key1", "value1"), new Metadata("key2", "value2")));
      when(limitUrlA.getProvider()).thenReturn(_partner.getProviders().get(0));
      
      // setup another LimitUrl
      LimitUrl limitUrlB = mock(LimitUrl.class);
      when(limitUrlB.getUrl()).thenReturn("http://www.mytestB.com/path/");
      when(limitUrlB.getMetadatas()).thenReturn(Arrays.asList(new Metadata("key1", "value1"), new Metadata("key1", "value2")));
      when(limitUrlB.getProvider()).thenReturn(_partner.getProviders().get(0));

      // setup another LimitUrl
      LimitUrl limitUrlC = mock(LimitUrl.class);
      when(limitUrlC.getUrl()).thenReturn("http://www.mytestC.com/path?PARAM=1");
      when(limitUrlC.getMetadatas()).thenReturn(Arrays.asList(new Metadata("key1", "value1"), new Metadata("key1", "value2")));
      when(limitUrlC.getProvider()).thenReturn(_partner.getProviders().get(0));

      // setup another LimitUrl
      LimitUrl limitUrlD = mock(LimitUrl.class);
      when(limitUrlD.getUrl()).thenReturn("http://www.mytestD.com?PARAM=1&PARAM2=.\\+");
      when(limitUrlD.getMetadatas()).thenReturn(Arrays.asList(new Metadata("key1", "value1"), new Metadata("key1", "value2")));
      when(limitUrlD.getProvider()).thenReturn(_partner.getProviders().get(0));

      // setup another LimitUrl with regexp
      LimitUrl limitUrlE = mock(LimitUrl.class);
      when(limitUrlE.getUrl()).thenReturn("/http://www.mytestE.com?PARAM=1*/");
      when(limitUrlE.getMetadatas()).thenReturn(Arrays.asList(new Metadata("key1", "value1"), new Metadata("key1", "value2")));
      when(limitUrlE.getProvider()).thenReturn(_partner.getProviders().get(0));
      
      
      // setup dao for our two urls
      when(_limitUrlDao.getAll()).thenReturn(Arrays.asList(limitUrlA, limitUrlB, limitUrlC, limitUrlD, limitUrlE));
      
      List<String> metadataAsStringList = _databaseExport.metadataAsStringList(_limitUrlDao, true);
      
      assertEquals(5, metadataAsStringList.size());
      assertEquals("http://www.mytestA.com\tpartner:\tpaTest\tprovider:\tprTest\tkey2:\tvalue2\tkey1:\tvalue1\t", metadataAsStringList.get(0));
      assertEquals("http://www.mytestB.com/path/\tpartner:\tpaTest\tprovider:\tprTest\tkey1:\tvalue1\tvalue2\t", metadataAsStringList.get(1));
      assertEquals("http://www.mytestC.com/path\\?PARAM=1\tpartner:\tpaTest\tprovider:\tprTest\tkey1:\tvalue1\tvalue2\t", metadataAsStringList.get(2));
      assertEquals("http://www.mytestD.com\\?PARAM=1&PARAM2=\\.\\\\\\+\tpartner:\tpaTest\tprovider:\tprTest\tkey1:\tvalue1\tvalue2\t", metadataAsStringList.get(3));
      assertEquals("http://www.mytestE.com?PARAM=1*\tpartner:\tpaTest\tprovider:\tprTest\tkey1:\tvalue1\tvalue2\t", metadataAsStringList.get(4));
    }



}
