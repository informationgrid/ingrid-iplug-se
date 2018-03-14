/*
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
/*
 * Copyright (c) 1997-2006 by wemove GmbH
 */
package de.ingrid.iplug.se;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.utils.TrustModifier;

/**
 * UVP data importer. Imports data from an excel file directly into the url
 * database.
 * 
 * All urls from instance are deleted.
 * 
 * The limit urls are set to the domain of the start url. Make sure the depth of
 * the crawl is set to 1 and no outlinks should be extracted.
 * 
 * The excel table must have a specific layout:
 * <ul>
 * <li>First line is the header line and will be ignored.</li>
 * <li>1. column: BLP name that appears on map popup as title.</li>
 * <li>2. column: LAT of map marker coordinate.</li>
 * <li>3. column: LON of map marker coordinate.</li>
 * <li>4. column: Url to BLPs in progress.</li>
 * <li>5. column: Url to finished BLPs.</li>
 * <li>6. column: BLP description that appears on map popup.</li>
 * </ul>
 * 
 * @author joachim@wemove.com
 */
@Service
public class UVPDataImporter {

    /**
     * The logging object
     */
    private static Logger log = Logger.getLogger( UVPDataImporter.class );

    public static Configuration conf;

    private static EntityManager em;

    private static Map<String, List<StatusEntry>> status = new LinkedHashMap<String, List<StatusEntry>>();

    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        @SuppressWarnings("static-access")
        Option instanceOption = OptionBuilder.withArgName( "instance name" ).hasArg().withDescription( "an existing instance name" ).create( "instance" );
        options.addOption( instanceOption );
        @SuppressWarnings("static-access")
        Option excelfileOption = OptionBuilder
                .withArgName( "excel file name" )
                .hasArg()
                .withDescription(
                        "path to excel file with columns: BLP name; LAT; LON; Url to BLPs in progress; Url to finished BLPs; BLP description (first header line is ignored)" )
                .create( "excelfile" );
        options.addOption( excelfileOption );
        @SuppressWarnings("static-access")
        Option partnerOption = OptionBuilder.withArgName( "partner short cut" ).hasArg().withDescription( "a partner shortcut. i.e. ni" ).create( "partner" );
        options.addOption( partnerOption );

        CommandLine cmd = parser.parse( options, args );

        String instance = null;
        if (cmd.hasOption( "instance" )) {
            instance = cmd.getOptionValue( "instance" );
        } else {
            System.out.println( "Missing patameter 'instance'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );

            System.exit( 0 );
        }

        String excelfile = null;
        if (cmd.hasOption( "excelfile" )) {
            excelfile = cmd.getOptionValue( "excelfile" );
        } else {
            System.out.println( "Missing patameter 'excelfile'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );
            System.exit( 0 );
        }

        String partner = null;
        if (cmd.hasOption( "partner" )) {
            partner = cmd.getOptionValue( "partner" );
        } else {
            System.out.println( "Missing patameter 'partner'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );
            System.exit( 0 );
        }

        conf = new ConfigBuilder<Configuration>( Configuration.class ).build();

        instance = instance.replaceAll( "[:\\\\/*?|<>\\W]", "_" );
        Path instancePath = Paths.get( conf.getInstancesDir() + "/" + instance );

        if (!Files.exists( instancePath )) {
            System.out.println( "Instance '" + instance + "' does not exist. Please create and configure instance for use for UVP BLP data." );
            System.exit( 0 );
        }

        // set the directory of the database to the configured one
        Map<String, String> properties = new HashMap<String, String>();
        Path dbDir = Paths.get( conf.databaseDir );
        properties.put( "javax.persistence.jdbc.url", "jdbc:h2:" + dbDir.toFile().getAbsolutePath() + "/urls;MVCC=true;AUTO_SERVER=TRUE" );

        // get an entity manager instance (initializes properties in the
        // DBManager)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( conf.databaseID, properties );

        DBManager.INSTANCE.intialize( emf );

        em = null;
        try {
            em = DBManager.INSTANCE.getEntityManager();
        } catch (PersistenceException e) {
            log.error( "Database seems to be corrupt." );
            System.exit( -1 );
        }

        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();

            // remove existing URLs
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Url> criteria = cb.createQuery( Url.class );
            Root<Url> urlTable = criteria.from( Url.class );
            Predicate instanceCriteria = cb.equal( urlTable.get( "instance" ), instance );

            criteria.from( Url.class );
            criteria.where( instanceCriteria );

            List<Url> existingUrls = em.createQuery( criteria ).getResultList();

            System.out.println( "Parsing and validating data from '" + excelfile + "'..." );
            List<BlpModel> blpModels = readData( excelfile );
            System.out.println( "" );

            for (Url url : existingUrls) {
                em.remove( url );
            }

            int cntRecords = 0;

            // initialize with ignored records
            for (String key : status.keySet()) {
                for (StatusEntry entry : status.get( key )) {
                    if (entry.type.equals( "IGNORED" )) {
                        cntRecords++;
                    }
                }
            }

            int cntIgnoredRecords = 0;
            int cntUrls = 0;
            int cntMarker = 0;

            for (BlpModel bm : blpModels) {

                cntRecords++;

                // for display in map we need ONE marker per blp dataset
                // therefore the map marker data is pushed to index only for one
                // of the URLs
                boolean pushBlpDataToIndex = true;

                System.out.println( "Add entry '" + bm.name + "'." );
                if (bm.urlInProgress != null && bm.urlInProgress.length() > 0) {
                    // add BLP meta data
                    Url url = null;
                    try {
                        url = createUrl( instance, partner, bm.urlInProgress, bm, pushBlpDataToIndex );
                        pushBlpDataToIndex = false;
                        em.persist( url );
                        cntUrls++;
                        cntMarker++;
                    } catch (Exception e) {
                        // ignore record
                    }
                }
                if (bm.urlFinished != null && bm.urlFinished.length() > 0 && bm.urlFinished != bm.urlInProgress) {
                    // add BLP meta data, if not already set, since we only need
                    // the meta data once.
                    Url url = null;
                    try {
                        url = createUrl( instance, partner, bm.urlFinished, bm, pushBlpDataToIndex );
                        em.persist( url );
                        cntUrls++;
                        if (pushBlpDataToIndex) {
                            cntMarker++;
                        }
                    } catch (Exception e) {
                        // ignore record
                    }
                }
            }
            System.out.println( "\n\nIgnored Entries by name:\n" );

            for (String k : status.keySet()) {

                if (status.get( k ).stream().filter( entry -> entry.type.equals( "IGNORED" ) ).count() > 0) {
                    cntIgnoredRecords++;
                    System.out.println( k );
                    for (StatusEntry entry : status.get( k )) {
                        System.out.println( "  " + entry.message );
                    }
                }
            }
            System.out.println( "\nFinish. Records: " + cntRecords + ", Ignored records or Urls: " + cntIgnoredRecords + ", Urls added: " + cntUrls + " urls to instance '" + instance + "', mark "
                    + cntMarker + " records as marker to be displayed on map." );

            tx.commit();
        } catch (Exception e) {
            System.out.println( "Error: '" + e.getMessage() + "'." );
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e; // or display error message
        } finally {
            em.close();
        }

    }

    /**
     * Get Limit URL from URL.
     * 
     * @param urlStr
     * @return
     * @throws MalformedURLException
     */
    public static String getDomain(String urlStr) throws MalformedURLException {
        URL url = new URL( urlStr );
        String host = url.getHost();
        return urlStr.substring( 0, urlStr.indexOf( host ) + host.length() );
    }

    /**
     * Derive limit urls from an url. Currently only the original URL is
     * returned because all redirects are already resolved before.
     * 
     * @param urlStr
     * @return
     * @throws MalformedURLException
     */
    public static List<String> getLimitUrls(String urlStr) throws MalformedURLException {
        List<String> result = new ArrayList<String>();
        result.add( urlStr );

        return result;

    }

    /**
     * Scan Excel file and gather all infos. Requires a specific excel table
     * layout
     * 
     * @param excelFile
     * @return
     * @throws IOException
     */
    public static List<BlpModel> readData(String excelFile) throws IOException {
        List<BlpModel> blpModels = new ArrayList<BlpModel>();

        FileInputStream inputStream = new FileInputStream( new File( excelFile ) );
        Workbook workbook = null;

        try {

            if (excelFile.endsWith( "xlsx" )) {
                workbook = new XSSFWorkbook( inputStream );
            } else if (excelFile.endsWith( "xls" )) {
                workbook = new HSSFWorkbook( inputStream );
            } else {
                throw new IllegalArgumentException( "The specified file is not Excel file" );
            }
            Sheet sheet = workbook.getSheetAt( 0 );
            Iterator<Row> it = sheet.iterator();
            if (it.hasNext()) {
                // skip first row (header)
                it.next();
                while (it.hasNext()) {
                    Iterator<Cell> ci = it.next().cellIterator();
                    BlpModel bm = new UVPDataImporter().new BlpModel();
                    while (ci.hasNext()) {
                        Cell cell = ci.next();
                        int columnIndex = cell.getColumnIndex();

                        switch (columnIndex) {
                        case 1:
                            bm.name = cell.getStringCellValue();
                            break;
                        case 2:
                            bm.lat = cell.getNumericCellValue();
                            break;
                        case 3:
                            bm.lon = cell.getNumericCellValue();
                            break;
                        case 4:
                            bm.urlInProgress = cell.getStringCellValue();
                            break;
                        case 5:
                            bm.urlFinished = cell.getStringCellValue();
                            break;
                        case 6:
                            bm.descr = cell.getStringCellValue();
                            break;
                        }
                    }

                    System.out.print( "." );

                    if (bm.name != null && bm.name.length() > 0) {
                        boolean isValid = validate( bm );
                        if (isValid) {
                            blpModels.add( bm );
                        }
                    }
                }
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return blpModels;

    }

    /**
     * Create an Url Entry. Add BLP (Bauleitplanung) meta data (like bounding
     * box, BLP name, BLP description, etc.) if pushBlpDataToIndex == true
     * 
     * @param instance
     * @param partner
     * @param urlStr
     *            The given URL ist checked for redirects. Redirects are
     *            resolved.
     * @param bm
     * @param pushBlpDataToIndex
     * @return
     * @throws Exception
     */
    public static Url createUrl(String instance, String partner, String urlStr, BlpModel bm, boolean pushBlpDataToIndex) throws Exception {
        Url idxUrl = new Url( instance );

        idxUrl.setStatus( "200" );

        String actualUrl = getActualUrl( urlStr, bm );

        idxUrl.setUrl( actualUrl );

        List<String> limitUrls = getLimitUrls( actualUrl );
        idxUrl.setLimitUrls( limitUrls );

        List<Metadata> metadata = new ArrayList<Metadata>();
        Metadata md = new Metadata();
        md.setMetaKey( "lang" );
        md.setMetaValue( "de" );
        metadata.add( md );

        md = new Metadata();
        md.setMetaKey( "procedure" );
        md.setMetaValue( "dev_plan" );
        metadata.add( md );

        md = new Metadata();
        md.setMetaKey( "datatype" );
        md.setMetaValue( "default" );
        metadata.add( md );

        md = new Metadata();
        md.setMetaKey( "datatype" );
        md.setMetaValue( "www" );
        metadata.add( md );

        md = new Metadata();
        md.setMetaKey( "partner" );
        md.setMetaValue( partner );
        metadata.add( md );

        md = new Metadata();
        md.setMetaKey( "x1" );
        md.setMetaValue( bm.lon.toString() );
        metadata.add( md );
        md = new Metadata();
        md.setMetaKey( "x2" );
        md.setMetaValue( bm.lon.toString() );
        metadata.add( md );

        md = new Metadata();
        md.setMetaKey( "y1" );
        md.setMetaValue( bm.lat.toString() );
        metadata.add( md );
        md = new Metadata();
        md.setMetaKey( "y2" );
        md.setMetaValue( bm.lat.toString() );
        metadata.add( md );

        if (pushBlpDataToIndex) {

            md = new Metadata();
            md.setMetaKey( "blp_marker" );
            md.setMetaValue( "blp_marker" );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "blp_name" );
            md.setMetaValue( bm.name );
            metadata.add( md );

            if (bm.descr != null && !bm.descr.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "blp_description" );
                md.setMetaValue( bm.descr );
                metadata.add( md );
            }

            if (bm.urlFinished != null && !bm.urlFinished.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "blp_url_finished" );
                md.setMetaValue( bm.urlFinished );
                metadata.add( md );
            }

            if (bm.urlInProgress != null && !bm.urlInProgress.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "blp_url_in_progress" );
                md.setMetaValue( bm.urlInProgress );
                metadata.add( md );
            }

        }

        idxUrl.setMetadata( metadata );

        return idxUrl;

    }

    /**
     * Validates a BLP model entry.
     * 
     * @param bm
     * @return True if BLP model is valid. False if not.
     */
    private static boolean validate(BlpModel bm) {
        boolean isValid = true;

        if (bm.name == null || bm.name.length() <= 3) {
            isValid = false;
            addLog( bm.name, "Name is null or too short.", "IGNORED" );
        }

        if (bm.lat < 47 || bm.lat > 56) {
            isValid = false;
            addLog( bm.name, "Lat not between 47 and 56.", "IGNORED" );
        }

        if (bm.lon < 5 || bm.lon > 15) {
            isValid = false;
            addLog( bm.name, "Lon not between 5 and 15.", "IGNORED" );
        }

        String url = bm.urlInProgress;
        if (url != null && url.length() > 0) {
            try {
                URLConnection conn = new URL( url ).openConnection();
                conn.connect();
            } catch (Exception e) {
                isValid = false;
                addLog( bm.name, "Problems accessing '" + url, "IGNORED" );
            }
        }

        url = bm.urlFinished;
        if (url != null && url.length() > 0) {
            try {
                URLConnection conn = new URL( url ).openConnection();
                TrustModifier.relaxHostChecking((HttpURLConnection) conn);
                conn.connect();
            } catch (Exception e) {
                isValid = false;
                addLog( bm.name, "Problems accessing '" + url, "IGNORED" );
            }
        }
        if ((bm.urlInProgress == null || bm.urlInProgress.length() == 0) && (bm.urlFinished == null || bm.urlFinished.length() == 0)) {
            isValid = false;
            addLog( bm.name, "No url set in 'progress' or 'finished'.", "IGNORED" );
        }

        return isValid;
    }

    public static String getActualUrl(String url, BlpModel bm) throws Exception {

        int termination = 10;
        while (termination-- > 0) {
            String actualUrl = null;
            actualUrl = getRedirect( url, bm );
            if (actualUrl.equals( url )) {
                return url;
            }
            addLog( bm.name, "Redirect detected: '" + url + "' -> '" + actualUrl + "'.", "REDIRECTS" );
            if (actualUrl.startsWith( "/" )) {
                url = getDomain( url ).concat( actualUrl );
            } else if (actualUrl.startsWith( "../" )) {
                while (actualUrl.startsWith( "../" )) {
                    url = stripLastPath(url);
                    actualUrl = actualUrl.substring( 3 );
                }
                url = url.concat( actualUrl );
            } else {
                url = actualUrl;
            }
        }
        addLog( bm.name, "Too many redirects.", "IGNORED" );
        throw new Exception( "Too many Redirects: 10" );
    }
    
    static String stripLastPath(String urlString) throws MalformedURLException {
        String domain = getDomain( urlString );
        String part = urlString.substring( 0, urlString.lastIndexOf( "/" ) );
        if (part.equals( domain )) {
            return domain.concat( "/" );
        } else {
            return urlString.substring( 0, urlString.lastIndexOf( "/", urlString.lastIndexOf( "/" ) - 1 ) ).concat( "/" );
        }
    }

    private static String getRedirect(String urlstring, BlpModel bm) throws Exception {
        HttpURLConnection con = null;

        int responseCode = -1;
        try {

            con = (HttpURLConnection) (new URL( urlstring ).openConnection());
            TrustModifier.relaxHostChecking(con);
            con.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0" );
            con.setInstanceFollowRedirects( false );
            con.connect();
            responseCode = con.getResponseCode();
            if (300 <= responseCode && responseCode <= 308) {
                Map<String, List<String>> headers = con.getHeaderFields();
                for (String header : headers.keySet()) {
                    if (header != null && header.equalsIgnoreCase( "location" )) {
                        return con.getHeaderField( header );
                    }
                }
            } else {
                String metaURL = getMetaRedirectURL( con );
                if (metaURL != null) {
                    if (!metaURL.startsWith( "http" )) {
                        URL u = new URL( new URL( urlstring ), metaURL );
                        return u.toString();
                    }
                    return metaURL;
                }
            }

        } catch (Throwable e) {
            if (responseCode == -1) {
                addLog( bm.name, "Problems accessing '" + urlstring + " (HTTP_ERROR: " + responseCode + ") (" + e + ")", "IGNORED" );
            } else {
                addLog( bm.name, "Problems accessing '" + urlstring + " (HTTP_ERROR: " + responseCode + ")", "IGNORED" );
            }
            throw e;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
        return urlstring;

    }

    private static String getMetaRedirectURL(HttpURLConnection con) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader( new InputStreamReader( con.getInputStream() ) )) {

            String content = null;
            while ((content = reader.readLine()) != null) {
                sb.append( content );
            }
            String html = sb.toString();
            html = html.replace( "\n", "" );
            if (html.length() == 0)
                return null;
            int indexHttpEquiv = html.toLowerCase().indexOf( "http-equiv=\"refresh\"" );
            if (indexHttpEquiv < 0) {
                return null;
            }
            html = html.substring( indexHttpEquiv );
            int indexContent = html.toLowerCase().indexOf( "content=" );
            if (indexContent < 0) {
                return null;
            }
            html = html.substring( indexContent );
            int indexURLStart = html.toLowerCase().indexOf( ";url=" );
            if (indexURLStart < 0) {
                return null;
            }
            html = html.substring( indexURLStart + 5 );
            int indexURLEnd = html.toLowerCase().indexOf( "\"" );
            if (indexURLEnd < 0) {
                return null;
            }
            return html.substring( 0, indexURLEnd );
        }

    }

    private static void addLog(String key, String message, String type) {
        if (!status.containsKey( key )) {
            status.put( key, new ArrayList<StatusEntry>() );
        }
        status.get( key ).add( new UVPDataImporter().new StatusEntry( message, type ) );

    }

    class StatusEntry {
        String message;
        String type;

        StatusEntry(String message, String type) {
            super();
            this.message = message;
            this.type = type;
        }

    }

    class BlpModel {

        String name;
        Double lat;
        Double lon;
        String urlInProgress;
        String urlFinished;
        String descr;

        @Override
        public String toString() {
            return "[name: " + name + "; lat:" + lat + "; lon:" + lon + "; urlInProgress:" + urlInProgress + "; urlFinished:" + urlFinished + "; descr:" + descr + "]";
        }

    }

}
