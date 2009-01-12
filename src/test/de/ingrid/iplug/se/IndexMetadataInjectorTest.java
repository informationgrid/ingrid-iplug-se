package de.ingrid.iplug.se;

import java.io.File;
import java.util.HashSet;

import junit.framework.TestCase;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.metadata.Metadata;

public class IndexMetadataInjectorTest extends TestCase {

    public void testMetadata() throws Exception {
        IMetadataInjector injector = new IndexMetadataInjector();
        PlugDescription plugdescription = new PlugDescription();
        plugdescription.setWorkinDirectory(new File("test-resources", "instances"));
        injector.configure(plugdescription);

        Metadata metadata = new Metadata();
        injector.injectMetaDatas(metadata);

        HashSet<String> provider = (HashSet<String>) metadata.getMetadata("provider");
        assertEquals(1, provider.size());
        assertEquals("bw_lu", provider.iterator().next());

        HashSet<String> partner = (HashSet<String>) metadata.getMetadata("partner");
        assertEquals(5, partner.size());
        assertTrue(partner.contains("bw"));
        assertTrue(partner.contains("bund"));
        assertTrue(partner.contains("mv"));
        assertTrue(partner.contains("hh"));
        assertTrue(partner.contains("sl"));

    }
}
