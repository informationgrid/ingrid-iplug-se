/*-
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.junit.Test;

public class UVPDataImporterTest {

    @Test
    public void testGetDomain() throws MalformedURLException {

        assertEquals( "http://www.testdomain.net", UVPDataImporter.getDomain( "http://www.testdomain.net/somePath?someParameter=1" ) );
        assertEquals( "http://www.testdomain.net", UVPDataImporter.getDomain( "http://www.testdomain.net" ) );
        try {
            String d = UVPDataImporter.getDomain( "://www.testdomain" );
            fail( "Invalid URL should raise exception but deliveres instead: " + d );
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetLimitUrls() throws MalformedURLException {

        List<String> result = UVPDataImporter.getLimitUrls( "http://www.testdomain.net/somePath?someParameter=1" );
        assertTrue( result.contains( "http://www.testdomain.net/somePath?someParameter=1" ) );

        try {
            String d = UVPDataImporter.getDomain( "://www.testdomain" );
            fail( "Invalid URL should raise exception but deliveres instead: " + d );
        } catch (Exception e) {

        }
    }

    @Test
    public void testReadData() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File( classLoader.getResource( "blp-urls-test.xlsx" ).getFile() );

        List<UVPDataImporter.BlpModel> l = UVPDataImporter.readData( file.getAbsolutePath() );
        assertEquals( true, l.size() > 0 );

        for (UVPDataImporter.BlpModel m : l) {
            try {
                if (m.urlFinished != null) {
                    UVPDataImporter.getLimitUrls( m.urlFinished );
                }
            } catch (Exception e) {
                fail( "Invalid LIMIT URL extracted from: " + m.urlFinished );
            }
            try {
                if (m.urlFinished != null) {
                    UVPDataImporter.getActualUrl( m.urlFinished, m );
                }
            } catch (Exception e) {
                System.out.println( "\nInvalid actual URL extracted from: " + m.urlFinished );
            }
            try {
                if (m.urlInProgress != null) {
                    UVPDataImporter.getLimitUrls( m.urlInProgress );
                }
            } catch (Exception e) {
                fail( "Invalid LIMIT URL extracted from: " + m.urlInProgress );
            }
            try {
                if (m.urlInProgress != null) {
                    UVPDataImporter.getActualUrl( m.urlInProgress, m );
                }
            } catch (Exception e) {
                System.out.println( "\nInvalid actual URL extracted from: " + m.urlInProgress );
            }

        }
    }
    
    @Test
    public void testStripLastPath() throws MalformedURLException {
        assertEquals( "http://test.domain.de/",  UVPDataImporter.stripLastPath( "http://test.domain.de/" ));
        assertEquals( "http://test.domain.de/path1/path2/",  UVPDataImporter.stripLastPath( "http://test.domain.de/path1/path2/path3/" ));
        assertEquals( "http://test.domain.de/",  UVPDataImporter.stripLastPath( "http://test.domain.de/path1/" ));
        assertEquals( "http://test.domain.de/",  UVPDataImporter.stripLastPath( "http://test.domain.de/index.html" ));
    }

}
