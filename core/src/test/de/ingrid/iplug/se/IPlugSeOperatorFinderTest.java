package de.ingrid.iplug.se;

import java.io.File;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocalFileSystem;

public class IPlugSeOperatorFinderTest extends TestCase {

    private File _folder = new File(System.getProperty("java.io.tmpdir"), "" + System.currentTimeMillis() + IPlugSeOperatorFinder.class.getName());

    @Override
    protected void setUp() throws Exception {
        _folder.mkdir();
        new File(_folder, "Web-Urls/segments/20090101/index/part-00000").mkdirs();
        new File(_folder, "Web-Urls/segments/20090101/search.done").createNewFile();
        new File(_folder, "Katalog-Urls/segments/20090102/index/part-00000").mkdirs();
        new File(_folder, "Katalog-Urls/segments/20090102/search.done").createNewFile();
        new File(_folder, "Katalog-Urls/segments/20090103/index/part-00000").mkdirs();

    }

    @Override
    protected void tearDown() throws Exception {
        assertTrue(FileUtil.fullyDelete(new LocalFileSystem(new Configuration()), _folder));
    }

    public void testFindIndex() throws Exception {
        IPlugSeOperatorFinder finder = new IPlugSeOperatorFinder();
        List<File> indices = finder.findIndices(_folder);
        assertNotNull(indices);
        assertEquals(2, indices.size());
    }

    public void testFindIndexValues() throws Exception {
        IPlugSeOperatorFinder finder = new IPlugSeOperatorFinder();
        Set<String> providerSet = finder.findIndexValues(new File("test-resources/instances"), "provider");
        Set<String> partnerSet = finder.findIndexValues(new File("test-resources/instances"), "partner");
        assertEquals(1, providerSet.size());
        assertTrue(providerSet.contains("bw_lu"));
        assertEquals(5, partnerSet.size());
        assertTrue(providerSet.contains("bw_lu"));
    }

}
