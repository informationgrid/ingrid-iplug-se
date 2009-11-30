package de.ingrid.iplug.se.urlmaintenance;

import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.service.MetadataService;

public class MetadataServiceTest extends TestCase {

  public void testGetLang() throws Exception {
    MetadataService metadataService = new MetadataService();
    List<Metadata> langs = metadataService.getLang();
    assertEquals(2, langs.size());
  }
  
  public void testGetDatatypes() throws Exception {
    MetadataService metadataService = new MetadataService();
    List<Metadata> datatypes = metadataService.getDatatypes();
    assertEquals(4, datatypes.size());
  }
}
