package de.ingrid.iplug.se;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocalFileSystem;

public class SearchableIndexFinderTest extends TestCase {

	private File _folder = new File(System.getProperty("java.io.tmpdir"), ""
			+ System.currentTimeMillis()
			+ SearchableIndexFinder.class.getName());

	@Override
	protected void setUp() throws Exception {
		_folder.mkdir();
		new File(_folder, "Web-Urls/segments/20090101/index/part-00000")
				.mkdirs();
		new File(_folder, "Web-Urls/segments/20090101/search.done")
				.createNewFile();
		new File(_folder, "Katalog-Urls/segments/20090102/index/part-00000")
				.mkdirs();
		new File(_folder, "Katalog-Urls/segments/20090102/search.done")
				.createNewFile();
		new File(_folder, "Katalog-Urls/segments/20090103/index/part-00000").mkdirs();
		
	}

	@Override
	protected void tearDown() throws Exception {
		assertTrue(FileUtil.fullyDelete(
				new LocalFileSystem(new Configuration()), _folder));
	}

	public void testFindIndex() throws Exception {
		 SearchableIndexFinder finder = new SearchableIndexFinder();
        List<File> indices = finder.findIndices(new LocalFileSystem(new Configuration()), _folder);
        assertNotNull(indices);
        assertEquals(2, indices.size());
	}

}
