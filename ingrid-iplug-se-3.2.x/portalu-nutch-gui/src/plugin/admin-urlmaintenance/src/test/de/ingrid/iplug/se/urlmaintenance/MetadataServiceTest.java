package de.ingrid.iplug.se.urlmaintenance;

import java.util.List;

import junit.framework.TestCase;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Metadata;
import de.ingrid.iplug.se.urlmaintenance.service.MetadataService;

public class MetadataServiceTest extends TestCase {

  public void testGetLang() throws Exception {
    final MetadataService metadataService = new MetadataService();
    final List<Metadata> langs = metadataService.getLang();
    assertEquals(2, langs.size());
  }

  public void testGetDatatypes() throws Exception {
    final MetadataService metadataService = new MetadataService();
    final List<Metadata> datatypes = metadataService.getDatatypes();
    assertEquals(5, datatypes.size());
  }
}
