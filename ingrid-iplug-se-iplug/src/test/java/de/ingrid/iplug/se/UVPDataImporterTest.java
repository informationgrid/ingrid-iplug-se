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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.Test;

import de.ingrid.iplug.se.UVPDataImporter.BlpModel;

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
        File file = new File( classLoader.getResource( "blp_daten_template.xlsx" ).getFile() );

        List<UVPDataImporter.BlpModel> l = UVPDataImporter.readData( file.getAbsolutePath() );
        assertEquals( true, l.size() > 0 );

        for (UVPDataImporter.BlpModel m : l) {
                try {
                    UVPDataImporter.getLimitUrls( m.urlBlpFinished );
                    UVPDataImporter.getActualUrl( m.urlBlpFinished, m );
                } catch (Exception e) {
                    fail( "Invalid LIMIT or ACTUAL URL  extracted from: " + m.urlBlpFinished + " in " + m.name  );
                }
            
                try {
                    UVPDataImporter.getLimitUrls( m.urlBlpInProgress );
                    UVPDataImporter.getActualUrl( m.urlBlpInProgress, m );
                } catch (Exception e) {
                    fail( "Invalid LIMIT or ACTUAL URL  extracted from: " + m.urlBlpInProgress + " in " + m.name  );
                }
            
                try {
                    UVPDataImporter.getLimitUrls( m.urlFnpInProgress );
                    UVPDataImporter.getActualUrl( m.urlFnpInProgress, m );
                } catch (Exception e) {
                    fail( "Invalid LIMIT or ACTUAL URL  extracted from: " + m.urlFnpInProgress + " in " + m.name  );
                }

                try {
                    UVPDataImporter.getLimitUrls( m.urlFnpFinished );
                    UVPDataImporter.getActualUrl( m.urlFnpFinished, m );
                } catch (Exception e) {
                    fail( "Invalid LIMIT or ACTUAL URL  extracted from: " + m.urlFnpFinished + " in " + m.name  );
                }

                try {
                    UVPDataImporter.getLimitUrls( m.urlBpFinished );
                    UVPDataImporter.getActualUrl( m.urlBpFinished, m );
                } catch (Exception e) {
                    fail( "Invalid LIMIT or ACTUAL URL  extracted from: " + m.urlBpFinished + " in " + m.name  );
                }

                try {
                    UVPDataImporter.getLimitUrls( m.urlBpInProgress );
                    UVPDataImporter.getActualUrl( m.urlBpInProgress, m );
                } catch (Exception e) {
                    fail( "Invalid LIMIT or ACTUAL URL  extracted from: " + m.urlBpInProgress + " in " + m.name  );
                }
                
                assertTrue(m.descr != null && m.descr.length() > 0); 
        }
    }

    @Test
    public void testStripLastPath() throws MalformedURLException {
        assertEquals( "http://test.domain.de/", UVPDataImporter.stripLastPath( "http://test.domain.de/" ) );
        assertEquals( "http://test.domain.de/path1/path2/", UVPDataImporter.stripLastPath( "http://test.domain.de/path1/path2/path3/" ) );
        assertEquals( "http://test.domain.de/", UVPDataImporter.stripLastPath( "http://test.domain.de/path1/" ) );
        assertEquals( "http://test.domain.de/", UVPDataImporter.stripLastPath( "http://test.domain.de/index.html" ) );
    }

    @Test
    public void testGetParent() throws MalformedURLException {
        assertEquals( "http://test.domain.de", UVPDataImporter.getParent( "http://test.domain.de/" ) );
        assertEquals( "http://test.domain.de", UVPDataImporter.getParent( "http://test.domain.de" ) );
        assertEquals( "http://test.domain.de", UVPDataImporter.getParent( "http://test.domain.de/a" ) );
        assertEquals( "http://test.domain.de/a", UVPDataImporter.getParent( "http://test.domain.de/a/" ) );
        assertEquals( "http://test.domain.de/a", UVPDataImporter.getParent( "http://test.domain.de/a/b.de" ) );
    }
    
    @Test
    public void testIsUrlShorterThan() throws MalformedURLException {
        assertTrue(UVPDataImporter.isUrlShorterThan( "http://test.domain.de", "http://test.domain.de/path/" ));
        assertTrue(!UVPDataImporter.isUrlShorterThan( "http://test.domain.de/path", "http://test.domain.de/" ));
        assertTrue(!UVPDataImporter.isUrlShorterThan( "http://test.domain.de/path/", "http://test.domain.de/path/" ));
        assertTrue(!UVPDataImporter.isUrlShorterThan( null, "http://test.domain.de/path/" ));
        assertTrue(!UVPDataImporter.isUrlShorterThan( "http://test.domain.de/path/", null ));
        assertTrue(!UVPDataImporter.isUrlShorterThan( "https://test.domain.de/path/", "http://test.domain.de/" ));
        assertTrue(!UVPDataImporter.isUrlShorterThan( "http://test.domain.de/path/", "https://test.domain.de/" ));
        assertTrue(!UVPDataImporter.isUrlShorterThan( "http://test.domain.de/path/", "https://test.domain.de/path/" ));
    }
    

    // @Test // activate as needed
    public void testGetActualUrl() throws Exception {

        BlpModel bm = new UVPDataImporter().new BlpModel();
        bm.name = "test";

        assertEquals( "http://www.merchweiler.de/p/dlhome.asp?artikel_id=&liste=491&tmpl_typ=Liste&lp=3691&area=100",
                UVPDataImporter.getActualUrl( "http://www.merchweiler.de/", bm ) );
    }
    
    @Test
    public void testExcludeUrlParsing() throws ParseException {
        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        @SuppressWarnings("static-access")
        Option exludeMarkerUrlsOption = OptionBuilder.withArgName( "exclude urls from marker urls" ).hasArgs().withDescription( "list of url regex patterns that define urls that should be excluded from possible marker urls." ).create( "exludeMarkerUrls" );
        exludeMarkerUrlsOption.setValueSeparator( '|' );
        options.addOption( exludeMarkerUrlsOption );
        CommandLine cmd = parser.parse( options, new String[] {"", "-exludeMarkerUrls", ".*minden-luebbecke.de/atlasfx/js/.*|.*wemove.com.*"} );

        String[] exludeMarkerUrls = null;
        if (cmd.hasOption( "exludeMarkerUrls" )) {
            exludeMarkerUrls = cmd.getOptionValues( exludeMarkerUrlsOption.getOpt() );
        }
        
        assertNotNull( exludeMarkerUrls );
        assertEquals( exludeMarkerUrls[0], ".*minden-luebbecke.de/atlasfx/js/.*" );
        assertEquals( exludeMarkerUrls[1], ".*wemove.com.*" );
        
    }

}
