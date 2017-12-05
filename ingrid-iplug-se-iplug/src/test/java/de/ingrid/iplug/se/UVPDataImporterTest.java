package de.ingrid.iplug.se;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.junit.Test;

public class UVPDataImporterTest {

    @Test
    public void testGetDomain() throws MalformedURLException {
        
        assertEquals( "http://www.testdomain.net/", UVPDataImporter.getDomain( "http://www.testdomain.net/somePath?someParameter=1" ) );
        assertEquals( "http://www.testdomain.net/", UVPDataImporter.getDomain( "http://www.testdomain.net" ) );
        try {
            String d = UVPDataImporter.getDomain( "://www.testdomain" ) ;
            fail( "Invalid URL should raise exception but deliveres instead: " + d );
        } catch (Exception e) {
            
        }
    }

    @Test
    public void testReadData() throws IOException {
        
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("blp-urls-test.xlsx").getFile());
        
        List<UVPDataImporter.BlpModel> l = UVPDataImporter.readData( file.getAbsolutePath() );
        assertEquals( true , l.size() > 0 );
    }

}
