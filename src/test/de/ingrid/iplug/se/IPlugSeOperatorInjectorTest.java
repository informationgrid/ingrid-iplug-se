package de.ingrid.iplug.se;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import junit.framework.TestCase;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.dsc.Record;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector;
import de.ingrid.utils.metadata.Metadata;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector.IPlugOperator;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector.Partner;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector.Provider;
import de.ingrid.utils.query.IngridQuery;

public class IPlugSeOperatorInjectorTest extends TestCase {

    private class BusProxy implements IBus {

        private static final long serialVersionUID = 1L;

        @Override
        public void addPlugDescription(PlugDescription arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean containsPlugDescription(String arg0, String arg1) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public PlugDescription[] getAllIPlugs() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlugDescription[] getAllIPlugsWithoutTimeLimitation() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public PlugDescription getIPlug(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Metadata getMetadata() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Metadata getMetadata(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Serializable getMetadata(String arg0, String arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void removePlugDescription(PlugDescription arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public IngridHits search(IngridQuery arg0, int arg1, int arg2, int arg3, int arg4) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IngridHits searchAndDetail(IngridQuery arg0, int arg1, int arg2, int arg3, int arg4, String[] arg5) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void close() throws Exception {
            // TODO Auto-generated method stub

        }

        @Override
        public IngridHitDetail getDetail(IngridHit arg0, IngridQuery arg1, String[] arg2) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public IngridHitDetail[] getDetails(IngridHit[] arg0, IngridQuery arg1, String[] arg2) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Record getRecord(IngridHit arg0) throws Exception {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public void testMetadata() throws Exception {
        IPlugSeOperatorInjector injector = new IPlugSeOperatorInjector();
        injector.setIBus(new BusProxy());
        PlugDescription plugdescription = new PlugDescription();
        plugdescription.setWorkinDirectory(new File("test-resources", "instances"));
        injector.configure(plugdescription);

        Metadata metadata = new Metadata();
        injector.injectMetaDatas(metadata);

        IPlugOperator plugOperator = (IPlugOperator) metadata.getMetadata(AbstractIPlugOperatorInjector.IPLUG_OPERATOR);

        List<Partner> partners = plugOperator.getPartners();
        assertEquals(6, partners.size());

        Partner partner = plugOperator.getPartner("bw");
        assertEquals("bw", partner.getShortName());
        assertNull(partner.getDisplayName());
        assertTrue(partner.getProviders().isEmpty());

        partner = plugOperator.getPartner("bund");
        assertEquals("bund", partner.getShortName());
        assertNull(partner.getDisplayName());
        assertTrue(partner.getProviders().isEmpty());

        partner = plugOperator.getPartner("mv");
        assertEquals("mv", partner.getShortName());
        assertNull(partner.getDisplayName());
        assertTrue(partner.getProviders().isEmpty());

        partner = plugOperator.getPartner("hh");
        assertEquals("hh", partner.getShortName());
        assertNull(partner.getDisplayName());
        assertTrue(partner.getProviders().isEmpty());

        partner = plugOperator.getPartner("sl");
        assertEquals("sl", partner.getShortName());
        assertNull(partner.getDisplayName());
        assertTrue(partner.getProviders().isEmpty());

        partner = plugOperator.getPartner("unknown");
        assertEquals("unknown", partner.getShortName());
        assertNull(partner.getDisplayName());
        assertEquals(1, partner.getProviders().size());
        Provider provider = partner.getProvider("bw_lu");
        assertEquals("bw_lu", provider.getShortName());
        assertNull(provider.getDisplayName());

    }
}
