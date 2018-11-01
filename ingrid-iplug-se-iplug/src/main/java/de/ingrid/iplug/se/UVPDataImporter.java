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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.nutchController.StatusProvider;
import de.ingrid.iplug.se.nutchController.StatusProvider.Classification;
import de.ingrid.iplug.se.utils.TrustModifier;
import de.ingrid.iplug.se.webapp.container.Instance;

/**
 * UVP data importer. Imports data from an excel file directly into the url
 * database.
 * 
 * All urls from instance are deleted.
 * 
 * The limit urls are set to the domain of the start url. Make sure the depth of
 * the crawl is set to 1 and no outlinks should be extracted.
 * 
 * The excel table must have a specific layout. The first row must contain the
 * column names.
 * 
 * <ul>
 * <li>NAME: BLP name that appears on map popup as title.</li>
 * <li>LAT: LAT of map marker coordinate.</li>
 * <li>LON: LON of map marker coordinate.</li>
 * <li>URL_VERFAHREN_OFFEN: Url to BLPs in progress.</li>
 * <li>URL_VERFAHREN_ABGESCHLOSSEN: Url to finished BLPs.</li>
 * <li>MITGLIEDSGEMEINDEN: BLP description that appears on map popup.</li>
 * </ul>
 * 
 * Columns can be mixed. The excel file can contain other columns, as long as
 * the specified columns exist.
 * 
 * @author joachim@wemove.com
 */
@Service
public class UVPDataImporter extends Thread {

    /**
     * The logging object
     */
    private static Logger log = Logger.getLogger( UVPDataImporter.class );

    public static Configuration conf;

    private static EntityManager em;

    private static Map<String, List<StatusEntry>> status = new LinkedHashMap<String, List<StatusEntry>>();

    private static List<String> markerUrls = new ArrayList<String>();

    private static StatusProviderService sps;

    private String statusFilename;
    private String logdir;
    private String partner;
    private Instance instance;
    private String excelFileName;
    private InputStream excelFileInputStream;
    StatusProvider sp;

    @Override
    public void run() {
        try {
            this.startImport();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void startImport() throws Exception {
        sp = sps.getStatusProvider( logdir, statusFilename );
        sp.clear();

        String partner = this.partner;

        conf = new ConfigBuilder<Configuration>( Configuration.class ).build();

        Path instancePath = Paths.get( instance.getWorkingDirectory() );

        if (!Files.exists( instancePath )) {
            log.error( "Instance '" + instance.getName() + "' does not exist. Please create and configure instance for use for UVP BLP data." );
            throw new FileNotFoundException();
        }

        // set the directory of the database to the configured one
        Map<String, String> properties = new HashMap<String, String>();
        Path dbDir = Paths.get( conf.databaseDir );
        properties.put( "javax.persistence.jdbc.url", "jdbc:h2:" + dbDir.toFile().getAbsolutePath() + "/urls;MVCC=true;AUTO_SERVER=TRUE" );

        em = null;
        try {
            em = DBManager.INSTANCE.getEntityManager();
        } catch (PersistenceException e) {
            log.error( "Database seems to be corrupt." );
            sp.addState( "PersistenceException", "Database seems to be corrupt.", Classification.ERROR );
            throw e;
        }

        EntityTransaction tx = null;
        CriteriaBuilder cb = null;
        CriteriaQuery<Url> criteria = null;
        Root<Url> urlTable = null;
        try {
            tx = em.getTransaction();
            tx.begin();

            // remove existing URLs
            cb = em.getCriteriaBuilder();
            criteria = cb.createQuery( Url.class );
            urlTable = criteria.from( Url.class );
            Predicate instanceCriteria = cb.equal( urlTable.get( "instance" ), instance.getName() );

            criteria.from( Url.class );
            criteria.where( instanceCriteria );

            List<Url> existingUrls = em.createQuery( criteria ).getResultList();

            sp.addState( "ParsingData", "Parsing and validating data from '" + excelFileName + "'..." );
            List<BlpModel> blpModels;
            if (excelFileInputStream != null) {
                blpModels = readData( excelFileInputStream, excelFileName );
            }else {
                blpModels = readData( excelFileName );
            }

            for (int i = 0; i < existingUrls.size(); i++) {
                Url url = existingUrls.get( i );
                sp.addState( "DeleteExisting", "Deleting existing urls [" + (i + 1) + "/" + existingUrls.size() + "]" );
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

            sp.addState( "AddEntries", "Adding entries" );
            for (int i = 0; i < blpModels.size(); i++) {
                BlpModel bm = blpModels.get( i );

                cntRecords++;

                // for display in map we need ONE marker per blp dataset
                // therefore the map marker data is pushed to index only for one
                // of the URLs
                boolean pushMarkerDataToIndex = true;

                System.out.println( "Add entry '" + bm.name + "'." );
                sp.addState( "AddEntries", "Adding entries [" + (i + 1) + "/" + blpModels.size() + "]" );
                if (bm.urlInProgress != null && bm.urlInProgress.length() > 0) {

                    // add BLP meta data
                    Url url = null;
                    try {
                        // do add the marker meta data only to the longer url of FINISHED or IN_PROGRESS
                        // since the crawler will generate 2 marker (The metadata of url A will be applied to
                        // all urls by the crawler that match the url A.).
                        //
                        // Also check if the URL has already marker data. Do not add marker data if the
                        // url has already been used.
                        if (isFinishedUrlLongerThanInProgressUrl( bm ) || markerUrls.contains( bm.urlInProgress ) || hasInvalidMarkerUrlIntersection( bm.urlInProgress )) {
                            pushMarkerDataToIndex = false;
                        }

                        url = createUrl( instance.getName(), partner, bm.urlInProgress, bm, pushMarkerDataToIndex );

                        // make sure that the next url will be marked as a map marker
                        // in case the other url starts with this url
                        // see comment above for an explanation
                        if (!pushMarkerDataToIndex) {
                            pushMarkerDataToIndex = true;
                        } else {
                            markerUrls.add( bm.urlInProgress );
                            pushMarkerDataToIndex = false;
                            cntMarker++;
                        }

                        em.persist( url );
                        cntUrls++;
                    } catch (Exception e) {
                        // ignore record
                    }
                }
                if (bm.urlFinished != null && bm.urlFinished.length() > 0 && !bm.urlFinished.equals( bm.urlInProgress )) {

                    if (pushMarkerDataToIndex && hasInvalidMarkerUrlIntersection( bm.urlFinished )) {
                        addLog( bm.name, "Invalid marker url intersection: " + bm.urlFinished, "IGNORED" );
                        // do not import this marker URL
                        continue;
                    }
                    // add BLP meta data, if not already set, since we only need
                    // the meta data once.
                    Url url = null;
                    try {
                        url = createUrl( instance.getWorkingDirectory(), partner, bm.urlFinished, bm, pushMarkerDataToIndex );
                        em.persist( url );
                        cntUrls++;
                        if (pushMarkerDataToIndex) {
                            markerUrls.add( bm.urlFinished );
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
            System.out.println( "\nFinish. Records: " + cntRecords + ", Ignored records or Urls: " + cntIgnoredRecords + ", Urls added: " + cntUrls + " urls to instance '" + instance.getName()
                    + "', mark " + cntMarker + " records as marker to be displayed on map." );
            sp.addState( "FINISHED", "\nFinished importing. Records: " + cntRecords + ", Ignored records or Urls: " + cntIgnoredRecords + ", Urls added: " + cntUrls + " urls to instance '"
                    + instance.getName() + "', mark " + cntMarker + " records as marker to be displayed on map." );

            tx.commit();
        } catch (Exception e) {
            System.out.println( "Error: '" + e.getMessage() + "'." );
            if (sp != null)
                sp.addState( "ERROR", "Error: '" + e.getMessage() + "'.", Classification.ERROR );
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e; // or display error message
        } finally {
            em.close();
        }

    }

    public static void main(String[] args) throws Exception {
        UVPDataImporter uvpDataImporter = new UVPDataImporter();
        StatusProviderService sps = new StatusProviderService();
        uvpDataImporter.setStatusProviderService( sps );

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        @SuppressWarnings("static-access")
        Option instanceOption = OptionBuilder.withArgName( "instance name" ).hasArg().withDescription( "an existing instance name" ).create( "instance" );
        options.addOption( instanceOption );
        @SuppressWarnings("static-access")
        Option excelfileOption = OptionBuilder.withArgName( "excel file name" )
                .hasArg()
                .withDescription( "path to excel file with columns: NAME; LAT; LON; URL_VERFAHREN_OFFEN; URL_VERFAHREN_ABGESCHLOSSEN; MITGLIEDSGEMEINDEN (column names in first row)" )
                .create( "excelfile" );
        options.addOption( excelfileOption );
        @SuppressWarnings("static-access")
        Option partnerOption = OptionBuilder.withArgName( "partner short cut" ).hasArg().withDescription( "a partner shortcut. i.e. ni" ).create( "partner" );
        options.addOption( partnerOption );

        CommandLine cmd = parser.parse( options, args );

        String instanceName = null;
        if (cmd.hasOption( "instance" )) {
            instanceName = cmd.getOptionValue( "instance" );
        } else {
            System.out.println( "Missing parameter 'instance'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );

            System.exit( 0 );
        }

        if (cmd.hasOption( "excelfile" )) {
            uvpDataImporter.excelFileName = cmd.getOptionValue( "excelfile" );
        } else {
            System.out.println( "Missing parameter 'excelfile'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );
            System.exit( 0 );
        }

        if (cmd.hasOption( "partner" )) {
            uvpDataImporter.partner = cmd.getOptionValue( "partner" );
        } else {
            System.out.println( "Missing parameter 'partner'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );
            System.exit( 0 );
        }

        conf = new ConfigBuilder<Configuration>( Configuration.class ).build();

        instanceName = instanceName.replaceAll( "[:\\\\/*?|<>\\W]", "_" );
        Path instancePath = Paths.get( conf.getInstancesDir() + "/" + instanceName );

        Instance instance = new Instance();
        instance.setName( instanceName );
        instance.setWorkingDirectory( instancePath.toString() );
        uvpDataImporter.setInstance( instance );

        if (!Files.exists( instancePath )) {
            System.out.println( "Instance '" + instancePath + "' does not exist. Please create and configure instance for use for UVP BLP data." );
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

        uvpDataImporter.startImport();
    }

    private static boolean hasInvalidMarkerUrlIntersection(String url) {
        long entriesContainedInUrl = markerUrls.stream().filter( entry -> url.startsWith( entry ) ).count();
        if (entriesContainedInUrl > 0) {
            System.out.println( "One of the marker urls is contained in " + url + "." );
        }

        long entriesContainingUrl = markerUrls.stream().filter( entry -> entry.contains( url ) ).count();
        if (entriesContainingUrl > 0) {
            System.out.println( "The url is contained in at least one marker url " + url + "." );
        }

        return (entriesContainedInUrl > 0 || entriesContainingUrl > 0);
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
     * Get the parent of the given URL. If the URL contains only of an domain,
     * return the domain.
     * 
     * <p>
     * http://test.domain.de/ -> http://test.domain.de<br>
     * http://test.domain.de -> http://test.domain.de<br>
     * http://test.domain.de/a -> http://test.domain.de<br>
     * http://test.domain.de/a/ -> http://test.domain.de/a<br>
     * http://test.domain.de/a/b.de -> http://test.domain.de/a<br>
     * </p>
     * 
     * @param urlStr
     * @return
     * @throws MalformedURLException
     */
    public static String getParent(String urlStr) throws MalformedURLException {
        String d = getDomain( urlStr );
        if (urlStr.equals( d ) || urlStr.equals( d.concat( "/" ) )) {
            return d;
        } else {
            return FilenameUtils.getPath( urlStr ).substring( 0, FilenameUtils.getPath( urlStr ).length() - 1 );
        }
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
    public List<BlpModel> readData(String excelFile) throws IOException {
        FileInputStream inputStream = new FileInputStream( new File( excelFile ) );
        return readData( inputStream, excelFile );
    }

    /**
     * Scan Excel workbook and gather all infos. Requires a specific excel table
     * layout
     * 
     * @param workbook
     * @return
     * @throws IOException
     */
    public List<BlpModel> readData(InputStream inputStream, String excelFileName) throws IOException {
        List<BlpModel> blpModels = new ArrayList<BlpModel>();
        Workbook workbook = null;

        try {

            if (excelFileName.endsWith( "xlsx" )) {
                workbook = new XSSFWorkbook( inputStream );
            } else if (excelFileName.endsWith( "xls" )) {
                workbook = new HSSFWorkbook( inputStream );
            } else {
                throw new IllegalArgumentException( "The specified file is not an Excel file" );
            }
            Sheet sheet = workbook.getSheetAt( 0 );
            Iterator<Row> it = sheet.iterator();
            boolean gotHeader = false;
            Map<Integer, String> columnNames = new HashMap<Integer, String>();
            if (it.hasNext()) {
                while (it.hasNext()) {
                    Iterator<Cell> ci = it.next().cellIterator();
                    // handle header
                    if (!gotHeader) {
                        while (ci.hasNext()) {
                            Cell cell = ci.next();
                            int columnIndex = cell.getColumnIndex();
                            String columnName = cell.getStringCellValue();
                            if (columnName == null || columnName.length() == 0) {
                                throw new IllegalArgumentException( "No column name specified for column " + columnIndex + "." );
                            }
                            columnNames.put( columnIndex, columnName );
                        }
                        gotHeader = true;
                    } else {

                        BlpModel bm = new UVPDataImporter().new BlpModel();
                        while (ci.hasNext()) {
                            Cell cell = ci.next();
                            int columnIndex = cell.getColumnIndex();

                            if (columnIndex < columnNames.size()) {
                                switch (columnNames.get( columnIndex )) {
                                case "NAME":
                                    bm.name = cell.getStringCellValue();
                                    break;
                                case "LAT":
                                    bm.lat = cell.getNumericCellValue();
                                    break;
                                case "LON":
                                    bm.lon = cell.getNumericCellValue();
                                    break;
                                case "URL_VERFAHREN_OFFEN":
                                    bm.urlInProgress = cell.getStringCellValue();
                                    break;
                                case "URL_VERFAHREN_ABGESCHLOSSEN":
                                    bm.urlFinished = cell.getStringCellValue();
                                    break;
                                case "MITGLIEDSGEMEINDEN":
                                    bm.descr = cell.getStringCellValue();
                                    break;
                                }
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
    public Url createUrl(String instance, String partner, String urlStr, BlpModel bm, boolean pushBlpDataToIndex) throws Exception {
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
    private boolean validate(BlpModel bm) {
        boolean isValid = true;

        if (bm.name == null || bm.name.length() <= 3) {
            isValid = false;
            addLog( bm.name, "Name is null or too short.", "IGNORED" );
        }

        if (bm.lat == null || bm.lat < 47 || bm.lat > 56) {
            isValid = false;
            addLog( bm.name, "Lat not between 47 and 56.", "IGNORED" );
        }

        if (bm.lon == null || bm.lon < 5 || bm.lon > 15) {
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
                TrustModifier.relaxHostChecking( (HttpURLConnection) conn );
                conn.connect();
            } catch (Exception e) {
                isValid = false;
                addLog( bm.name, "Problems accessing '" + url, "IGNORED" );
            }
        }
        if ((bm.urlInProgress == null || bm.urlInProgress.length() == 0) && (bm.urlFinished == null || bm.urlFinished.length() == 0)) {
            isValid = false;
            addLog( bm.name, "No url set in 'URL_VERFAHREN_OFFEN' or 'URL_VERFAHREN_ABGESCHLOSSEN'.", "IGNORED" );
        }

        return isValid;
    }

    /**
     * Checks if the FINISH url is longer than the IN_PROGRESS url. Ignores the protocol.
     * 
     * @param bm
     * @return True if FINISH url is longer than the IN_PROGRESS url. False otherwise (FINISH=NULL; FINISH == IN_PROGRESS)
     * @throws MalformedURLException
     */
    public static boolean isFinishedUrlLongerThanInProgressUrl(BlpModel bm) throws MalformedURLException {
        if (bm.urlFinished == null || bm.urlFinished.trim().length() == 0 || bm.urlInProgress == null || bm.urlInProgress.trim().length() == 0) {
            return false;
        }

        URL urlFinished = new URL( bm.urlFinished );
        String finished = bm.urlFinished.substring( bm.urlFinished.indexOf( urlFinished.getProtocol() ) + urlFinished.getProtocol().length(), bm.urlFinished.length() );
        URL urlInProgress = new URL( bm.urlInProgress );
        String inProgress = bm.urlInProgress.substring( bm.urlInProgress.indexOf( urlInProgress.getProtocol() ) + urlInProgress.getProtocol().length(), bm.urlInProgress.length() );

        if (!finished.equals( inProgress ) && finished.startsWith( inProgress )) {
            return true;
        } else {
            return false;
        }
    }

    public String getActualUrl(String url, BlpModel bm) throws Exception {

        int termination = 10;
        while (termination-- > 0) {
            String actualUrl = null;
            actualUrl = getRedirect( url, bm );
            if (actualUrl.equals( url )) {
                return url;
            }
            addLog( bm.name, "Redirect detected: '" + url + "' -> '" + actualUrl + "'.", "REDIRECTS" );
            if (actualUrl.startsWith( "/" )) {
                // redirect to local absolute url
                url = getDomain( url ).concat( actualUrl );
            } else if (actualUrl.indexOf( "://" ) < 0 || actualUrl.indexOf( "://" ) > 10) {
                // redirect to local relative url
                url = getParent( url ).concat( "/" ).concat( actualUrl );
            } else if (actualUrl.startsWith( "../" )) {
                // redirect to local parent directory based url
                while (actualUrl.startsWith( "../" )) {
                    url = stripLastPath( url );
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

    private String getRedirect(String urlstring, BlpModel bm) throws Exception {
        HttpURLConnection con = null;

        int responseCode = -1;
        try {
            con = (HttpURLConnection) (new URL( urlstring ).openConnection());
            TrustModifier.relaxHostChecking( con );
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
                if (content.toLowerCase().contains( "</head" ) || content.matches( "(?i)<meta.*?http-equiv=.*?refresh.*?>" )) {
                    break;
                }
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
            int indexURLStart = html.toLowerCase().indexOf( "url=" );
            if (indexURLStart < 0) {
                return null;
            }
            html = html.substring( indexURLStart + 4 );
            int indexURLEnd = html.toLowerCase().indexOf( "\"" );
            if (indexURLEnd < 0) {
                return null;
            }
            return html.substring( 0, indexURLEnd );
        }

    }

    private void addLog(String key, String message, String type) {
        if (!status.containsKey( key )) {
            status.put( key, new ArrayList<StatusEntry>() );
        }
        status.get( key ).add( new UVPDataImporter().new StatusEntry( message, type ) );
        if (this.sp != null) {
            sp.addState( key, key + ":  " + type + " " + message, StatusProvider.Classification.WARN );
        }

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

    public class BlpModel {

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

    public StatusProviderService getStatusProviderService() {
        return sps;
    }

    @Autowired
    public void setStatusProviderService(StatusProviderService statusProviderService) {
        UVPDataImporter.sps = statusProviderService;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
        this.logdir = instance.getWorkingDirectory();
        this.statusFilename = "import_status.xml";
    }

    public String getExcelFileName() {
        return excelFileName;
    }

    public void setExcelFileName(String excelFileName) {
        this.excelFileName = excelFileName;
    }

    public InputStream getExcelFileInputStream() {
        return excelFileInputStream;
    }

    public void setExcelFileInputStream(InputStream excelFileInputStream) {
        this.excelFileInputStream = excelFileInputStream;
    }

}
