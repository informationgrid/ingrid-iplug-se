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
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
 * <li>NAME (alternate: STADT/GEMEINDE): BLP name that appears on map popup as
 * title.</li>
 * <li>LAT: LAT of map marker coordinate.</li>
 * <li>LON: LON of map marker coordinate.</li>
 * <li>URL_VERFAHREN_OFFEN: Url to BLPs (Bauleitpläne) in progress.</li>
 * <li>URL_VERFAHREN_ABGESCHLOSSEN: Url to finished BLPs (Bauleitpläne).</li>
 * <li>URL_VERFAHREN_FNP_LAUFEND: Url to FNPs (Flächennutzungspläne) in
 * progress.</li>
 * <li>URL_VERFAHREN_FNP_ABGESCHLOSSEN: Url to finished FNPs
 * (Flächennutzungspläne).</li>
 * <li>URL_VERFAHREN_BEBAUUNGSPLAN_LAUFEND: Url to BPs (Bebauungspläne) in
 * progress.</li>
 * <li>URL_VERFAHREN_BEBAUUNGSPLAN_ABGESCHLOSSEN: Url to finished BPs
 * (Bebauungspläne).</li>
 * <li>MITGLIEDSGEMEINDEN: BLP description that appears on map popup.</li>
 * </ul>
 *
 * The column names are treated as prefixes. More descriptive column names could
 * be used (i.e. URL_VERFAHREN_BEBAUUUNGSPLAN_LAUFEND/ SATZUNGEN NACH § 34 Abs.
 * 4 und § 35 Abs. 6 BauGB).
 *
 * "Flächennutzungspläne" and "Bebauungspläne" are an alternative to
 * "Bauleitpläne".
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

    public Configuration conf;

    private EntityManager em;

    private List<String> markerUrls = new ArrayList<String>();

    private static StatusProviderService sps;
    private String statusFilename;
    private String logdir;
    private String partner;
    private Instance instance;
    private String excelFileName;
    private InputStream excelFileInputStream;
    private StatusProvider sp;

    private PrintWriter logfileWriter;

    private String[] excludeMarkerUrls;

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

        em = null;
        try {
            em = DBManager.INSTANCE.getEntityManager();
        } catch (PersistenceException e) {
            log.error( "Database seems to be corrupt." );
            System.exit( -1 );
        }

        Path importLogPath = Paths.get( instance.getWorkingDirectory(), "logs", "import.log" );
        Files.createDirectories( importLogPath.getParent() );
        logfileWriter = new PrintWriter( importLogPath.toString() );
        EntityTransaction tx = null;
        try {
            tx = em.getTransaction();
            tx.begin();

            // remove existing URLs
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Url> criteria = cb.createQuery( Url.class );
            Root<Url> urlTable = criteria.from( Url.class );
            Predicate instanceCriteria = cb.equal( urlTable.get( "instance" ), instance.getName() );

            criteria.from( Url.class );
            criteria.where( instanceCriteria );

            List<Url> existingUrls = em.createQuery( criteria ).getResultList();

            logAndPrint( "Parsing and validating data from '" + excelFileName + "'..." );
            sp.addState( "ParsingData", "Parsing and validating data from '" + excelFileName + "'..." );
            List<BlpModel> blpModels;
            if (excelFileInputStream != null) {
                blpModels = readData( excelFileInputStream, excelFileName );
            } else {
                blpModels = readData( excelFileName );
            }
            sp.addState( "ParsingData", "Parsing and validating data from '" + excelFileName + "'... done." );
            logAndPrint( "" );

            sp.addState( "DeleteExisting", "Deleting existing urls..." );
            for (int i = 0; i < existingUrls.size(); i++) {
                Url url = existingUrls.get( i );
                em.remove( url );
            }
            sp.addState( "DeleteExisting", "Deleting existing urls... done." );

            int cntRecords = blpModels.size();

            int cntUrls = 0;
            int cntMarker = 0;

            logAndPrint( "\nParsing and validating records ..." );
            sp.addState( "AddEntries", "Adding excel records" );
            for (int i = 0; i < blpModels.size(); i++) {
                BlpModel bm = blpModels.get( i );
                System.out.print( "." );
                sp.addState( "AddEntries", "Adding entries [" + (i + 1) + "/" + blpModels.size() + "]" );

                if (bm.errors.isEmpty()) {

                    // for display in map we need ONE marker per blp dataset
                    // therefore the map marker data is pushed only for one
                    // of the URLs
                    boolean markerAlreadyPushed = false;

                    /*
                     * The crawler passes the meta data of URL A to all URLs
                     * that start with URL A.
                     *
                     * We have to make sure that - The marker URL is the most
                     * complex URL of the BLP record - The marker URL is not
                     * contained in existing marker URLs - The marker URL does
                     * not contain an existing marker URL
                     */

                    List<String> blpUrls = Arrays.asList( new String[] { bm.urlBlpInProgress, bm.urlBlpFinished, bm.urlFnpInProgress, bm.urlFnpFinished, bm.urlBpInProgress, bm.urlBpFinished } );
                    blpUrls = blpUrls.stream().filter( Objects::nonNull ).distinct().sorted( Comparator.<String> comparingInt( s -> getUrlWithoutParameters( s ).length() ).reversed() ).collect(
                            Collectors.toList() );
                    List<String> ignoredBlpUrls = new ArrayList<>();

                    for (String blpUrl : blpUrls) {
                        if (blpUrl != null && blpUrl.length() > 0) {

                            if (excludeMarkerUrls != null) {
                                for (String regexp : excludeMarkerUrls) {
                                    if (blpUrl.matches( regexp )) {
                                        bm.errors.add( new UVPDataImporter().new StatusEntry( "Url explicitly excluded: " + blpUrl, "URL_IGNORED" ) );
                                        ignoredBlpUrls.add( blpUrl );
                                        // do not import this URL
                                        break;
                                    }
                                }
                                if (ignoredBlpUrls.contains( blpUrl )) {
                                    // do not import this URL
                                    continue;
                                }
                            }

                            long entriesContainedInUrl = markerUrls.stream().filter( entry -> getUrlWithoutParameters( blpUrl ).startsWith( entry ) ).count();
                            long entriesContainingUrl = markerUrls.stream().filter( entry -> entry.contains( getUrlWithoutParameters( blpUrl ) ) ).count();

                            boolean hasInvalidMarkerUrlIntersection = (entriesContainedInUrl > 0 || entriesContainingUrl > 0);

                            if (hasInvalidMarkerUrlIntersection) {
                                // URL has invalid marker intersection and must
                                // be ignored, otherwise
                                // the crawler transfers
                                // - marker meta data to this url, if the url is
                                // contained in a marker url
                                // - this meta data to a marker url, if a marker
                                // url contains this url

                                bm.errors.add( new UVPDataImporter().new StatusEntry( "Invalid url intersection: " + blpUrl, "URL_IGNORED" ) );
                                ignoredBlpUrls.add( blpUrl );
                                // do not import this URL
                                continue;
                            }

                            boolean pushMarker = false;
                            try {
                                if (!markerAlreadyPushed) {
                                    pushMarker = true;
                                }
                                Url url = createUrl( instance.getName(), partner, blpUrl, bm, pushMarker );
                                em.persist( url );
                                if (pushMarker) {
                                    cntMarker++;
                                    bm.hasMarker = true;
                                    markerAlreadyPushed = true;
                                    markerUrls.add( getUrlWithoutParameters( blpUrl ) );
                                }
                            } catch (Exception e) {
                                // ignore URL
                                if (pushMarker) {
                                    bm.hasMarker = false;

                                    ignoredBlpUrls.add( blpUrl );
                                }

                                bm.errors.add( new UVPDataImporter().new StatusEntry( e.getMessage(), "URL_IGNORED" ) );

                            }
                            cntUrls++;
                        }
                    }

                    if (!markerAlreadyPushed) {
                        bm.hasMarker = false;
                        bm.errors.add( new UVPDataImporter().new StatusEntry( "No marker could be added.", "IGNORED" ) );
                    }
                }
            }

            long noMarkersAdded = blpModels.stream().filter( bm -> !bm.hasMarker ).count();
            if (noMarkersAdded > 0) {

                logAndPrint( "\n\nNO MARKER IMPORTED:\n" );
                sp.addState( "NO_MARKERS", "No marker set for " + noMarkersAdded + " records!", Classification.WARN );

                for (BlpModel bm : blpModels) {
                    if (!bm.hasMarker) {
                        logAndPrint( "Entry '" + bm.name + "'." );
                        sp.appendToState( "NOMARKERS", "Entry '" + bm.name + "'." );
                        for (StatusEntry se : bm.errors) {
                            logAndPrint( "  " + se.message );
                        }
                    }
                }
            }

            long partialURLErrors = blpModels.stream().filter( bm -> bm.hasMarker && !bm.errors.isEmpty() ).count();
            if (partialURLErrors > 0) {

                sp.addState( "PARTIAL_URLS", "Partial URL errors found: " + partialURLErrors + "", Classification.WARN );
                logAndPrint( "\n\nPARTIAL URL ERRORS/PROBLEMS:\n" );

                for (BlpModel bm : blpModels) {
                    if (bm.hasMarker && !bm.errors.isEmpty()) {
                        logAndPrint( "Entry '" + bm.name + "'." );
                        for (StatusEntry se : bm.errors) {
                            logAndPrint( "  " + se.message );
                        }
                    }
                }
            }

            logAndPrint( "\nFinish. Excel Records: " + cntRecords + ", Urls added: " + cntUrls + " urls to instance '" + instance.getName() + "', mark " + cntMarker
                    + " records as marker to be displayed on map." );
            sp.addState( "FINISHED", "\nFinished importing. Excel Records: " + cntRecords + ", Urls added: " + cntUrls + " urls to instance '" + instance.getName() + "', mark " + cntMarker
                    + " records as marker to be displayed on map." );

            tx.commit();
        } catch (Exception e) {
            logAndPrint( "Error: '" + e.getMessage() + "'." );
            sp.addState( "ERROR", e.getMessage(), Classification.ERROR );
            if (tx != null && tx.isActive())
                tx.rollback();
            throw e; // or display error message
        } finally {
            em.close();
            logfileWriter.close();
        }

    }

    private void logAndPrint(String message) {
        System.out.println( message );
        logfileWriter.println( message );
    }

    /**
     * @param args
     * @throws Exception
     */
    /**
     * @param args
     * @throws Exception
     */
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        UVPDataImporter uvpDataImporter = new UVPDataImporter();
        uvpDataImporter.setStatusProviderService( sps = new StatusProviderService() );

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        @SuppressWarnings("static-access")
        Option instanceOption = OptionBuilder.withArgName( "instance name" ).hasArg().withDescription( "an existing instance name" ).create( "instance" );
        options.addOption( instanceOption );
        @SuppressWarnings("static-access")
        Option excelfileOption = OptionBuilder.withArgName( "excel file name" )
                .hasArg()
                .withDescription( "path to excel file with columns: NAME (alternate: STADT/GEMEINDE); LAT; LON; URL_VERFAHREN_OFFEN; "
                        + "URL_VERFAHREN_ABGESCHLOSSEN; URL_VERFAHREN_FNP_LAUFEND; URL_VERFAHREN_FNP_ABGESCHLOSSEN;"
                        + " URL_VERFAHREN_BEBAUUNGSPLAN_LAUFEND; URL_VERFAHREN_BEBAUUNGSPLAN_ABGESCHLOSSEN; MITGLIEDSGEMEINDEN (column names in first row)" )
                .create( "excelfile" );
        options.addOption( excelfileOption );
        @SuppressWarnings("static-access")
        Option partnerOption = OptionBuilder.withArgName( "partner short cut" ).hasArg().withDescription( "a partner shortcut. i.e. ni" ).create( "partner" );
        options.addOption( partnerOption );
        @SuppressWarnings("static-access")
        Option excludeMarkerUrlsOption = OptionBuilder.withArgName( "exclude urls from marker urls" )
                .hasArgs()
                .withDescription( "list of url regex patterns that define urls that should be excluded from possible marker urls, separated by '|'." )
                .create( "excludeMarkerUrls" );
        excludeMarkerUrlsOption.setValueSeparator( '|' );
        options.addOption( excludeMarkerUrlsOption );

        CommandLine cmd = parser.parse( options, args );

        String instanceName = null;
        if (cmd.hasOption( "instance" )) {
            instanceName = cmd.getOptionValue( "instance" );
        } else {
            System.out.println( "Missing patameter 'instance'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );

            System.exit( 0 );
        }

        if (cmd.hasOption( "excelfile" )) {
            uvpDataImporter.excelFileName = cmd.getOptionValue( "excelfile" );
        } else {
            System.out.println( "Missing patameter 'excelfile'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );
            System.exit( 0 );
        }

        if (cmd.hasOption( "partner" )) {
            uvpDataImporter.partner = cmd.getOptionValue( "partner" );
        } else {
            System.out.println( "Missing patameter 'partner'." );
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "UVPDataImporter", options );
            System.exit( 0 );
        }

        if (cmd.hasOption( "excludeMarkerUrls" )) {
            uvpDataImporter.excludeMarkerUrls = cmd.getOptionValues( "excludeMarkerUrls" );
        }

        uvpDataImporter.conf = new ConfigBuilder<Configuration>( Configuration.class ).build();

        instanceName = instanceName.replaceAll( "[:\\\\/*?|<>\\W]", "_" );
        Path instancePath = Paths.get( uvpDataImporter.conf.getInstancesDir() + "/" + instanceName );

        Instance instance = new Instance();
        instance.setName( instanceName );
        instance.setWorkingDirectory( instancePath.toString() );
        uvpDataImporter.setInstance( instance );

        if (!Files.exists( instancePath )) {
            System.out.println( "Instance '" + instanceName + "' does not exist. Please create and configure instance for use for UVP BLP data." );
            System.exit( 0 );
        }

        // set the directory of the database to the configured one
        Map<String, String> properties = new HashMap<String, String>();
        Path dbDir = Paths.get( uvpDataImporter.conf.databaseDir );
        properties.put( "javax.persistence.jdbc.url", "jdbc:h2:" + dbDir.toFile().getAbsolutePath() + "/urls;MVCC=true;AUTO_SERVER=TRUE" );

        // get an entity manager instance (initializes properties in the
        // DBManager)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory( uvpDataImporter.conf.databaseID, properties );

        DBManager.INSTANCE.intialize( emf );

        uvpDataImporter.startImport();
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

    private static String getUrlWithoutParameters(String url) {
        return url.split( "\\?" )[0];
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
     * Scan Excel file and gather all infos. Requires a specific excel table
     * layout
     *
     * @param excelFile
     * @return
     * @throws IOException
     */
    public static List<BlpModel> readData(InputStream inputStream, String excelFile) throws IOException {
        List<BlpModel> blpModels = new ArrayList<BlpModel>();

        Workbook workbook = null;

        try {

            if (excelFile.endsWith( "xlsx" )) {
                workbook = new XSSFWorkbook( inputStream );
            } else if (excelFile.endsWith( "xls" )) {
                workbook = new HSSFWorkbook( inputStream );
            } else {
                throw new IllegalArgumentException( "The specified file is not an Excel file" );
            }
            Sheet sheet = workbook.getSheetAt( 0 );
            Iterator<Row> it = sheet.iterator();
            boolean gotHeader = false;
            Map<Integer, String> columnNames = new HashMap<Integer, String>();
            if (it.hasNext()) {
                // iterate over all rows
                while (it.hasNext()) {
                    Iterator<Cell> ci = it.next().cellIterator();
                    // handle header
                    if (!gotHeader) {
                        // iterate over all columns
                        while (ci.hasNext()) {
                            Cell cell = ci.next();
                            int columnIndex = cell.getColumnIndex();
                            String columnName = cell.getStringCellValue();
                            if (columnName == null || columnName.length() == 0) {
                                throw new IllegalArgumentException( "No column name specified for column " + columnIndex + "." );
                            }
                            columnNames.put( columnIndex, columnName );
                        }
                        validateColumnNames( columnNames );
                        gotHeader = true;
                    } else {

                        BlpModel bm = new UVPDataImporter().new BlpModel();
                        while (ci.hasNext()) {
                            Cell cell = ci.next();
                            int columnIndex = cell.getColumnIndex();

                            if (columnIndex < columnNames.size()) {
                                String colName = columnNames.get( columnIndex );

                                if (colName.equals( "NAME" )) {
                                    bm.name = cell.getStringCellValue();
                                } else if (colName.equals( "STADT/GEMEINDE" )) {
                                    bm.name = cell.getStringCellValue();
                                } else if (colName.equals( "LAT" )) {
                                    try {
                                        bm.lat = cell.getNumericCellValue();
                                    } catch (Exception e) {
                                        try {
                                            bm.lat = Double.valueOf( cell.getStringCellValue() );
                                        } catch (Exception e1) {
                                            // ignore
                                        }
                                    }
                                } else if (colName.equals( "LON" )) {
                                    try {
                                        bm.lon = cell.getNumericCellValue();
                                    } catch (Exception e) {
                                        try {
                                            bm.lon = Double.valueOf( cell.getStringCellValue() );
                                        } catch (Exception e1) {
                                            // ignore
                                        }
                                    }
                                } else if (colName.startsWith( "URL_VERFAHREN_OFFEN" )) {
                                    bm.urlBlpInProgress = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_ABGESCHLOSSEN" )) {
                                    bm.urlBlpFinished = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_FNP_LAUFEND" )) {
                                    bm.urlFnpInProgress = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_FNP_ABGESCHLOSSEN" )) {
                                    bm.urlFnpFinished = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_BEBAUUNGSPLAN_LAUFEND" )) {
                                    bm.urlBpInProgress = cell.getStringCellValue();
                                } else if (colName.startsWith( "URL_VERFAHREN_BEBAUUNGSPLAN_ABGESCHLOSSEN" )) {
                                    bm.urlBpFinished = cell.getStringCellValue();
                                } else if (colName.startsWith( "MITGLIEDSGEMEINDEN" )) {
                                    bm.descr = cell.getStringCellValue();
                                }
                            }
                        }

                        System.out.print( "." );

                        if (bm.name != null && bm.name.length() > 0) {
                            validate( bm );
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

            if (bm.urlBlpFinished != null && !bm.urlBlpFinished.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "blp_url_finished" );
                md.setMetaValue( bm.urlBlpFinished );
                metadata.add( md );
            }

            if (bm.urlBlpInProgress != null && !bm.urlBlpInProgress.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "blp_url_in_progress" );
                md.setMetaValue( bm.urlBlpInProgress );
                metadata.add( md );
            }

            if (bm.urlFnpFinished != null && !bm.urlFnpFinished.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "fnp_url_finished" );
                md.setMetaValue( bm.urlFnpFinished );
                metadata.add( md );
            }

            if (bm.urlFnpInProgress != null && !bm.urlFnpInProgress.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "fnp_url_in_progress" );
                md.setMetaValue( bm.urlFnpInProgress );
                metadata.add( md );
            }

            if (bm.urlBpFinished != null && !bm.urlBpFinished.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "bp_url_finished" );
                md.setMetaValue( bm.urlBpFinished );
                metadata.add( md );
            }

            if (bm.urlBpInProgress != null && !bm.urlBpInProgress.isEmpty()) {
                md = new Metadata();
                md.setMetaKey( "bp_url_in_progress" );
                md.setMetaValue( bm.urlBpInProgress );
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
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Name is null or too short.", "IGNORED" ) );
        }

        if (bm.lat == null || bm.lat < 47 || bm.lat > 56) {
            isValid = false;
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Lat not between 47 and 56.", "IGNORED" ) );
        }

        if (bm.lon == null || bm.lon < 5 || bm.lon > 15) {
            isValid = false;
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Lon not between 5 and 15.", "IGNORED" ) );
        }

        List<String> blpUrls = Arrays.asList( new String[] { bm.urlBlpInProgress, bm.urlBlpFinished, bm.urlFnpInProgress, bm.urlFnpFinished, bm.urlBpInProgress, bm.urlBpFinished } );

        /*
         *
         * for (String url : blpUrls) { if (url != null && url.length() > 0) {
         * try { URLConnection conn = new URL( url ).openConnection();
         * TrustModifier.relaxHostChecking( (HttpURLConnection) conn );
         * conn.connect(); } catch (Exception e) { isValid = false; bm.info.add(
         * new UVPDataImporter().new StatusEntry( "Problems accessing '" + url,
         * "URL_IGNORED" ) ); } } }
         */
        // check if any URL is set.
        boolean hasUrlSet = blpUrls.stream().filter( entry -> (entry != null && entry.trim().length() > 0) ).count() > 0;
        if (!hasUrlSet) {
            isValid = false;
            bm.errors.add( new UVPDataImporter().new StatusEntry( "No URL set.", "IGNORED" ) );
        }

        bm.hasMarker = isValid;

        return isValid;
    }

    /**
     * Validates the excel header.
     *
     * @param columnNames
     * @throws IllegalArgumentException
     */
    private static void validateColumnNames(Map<Integer, String> columnNames) throws IllegalArgumentException {

        if (!columnNames.containsValue( "NAME" ) && !columnNames.containsValue( "STADT/GEMEINDE" )) {
            throw new IllegalArgumentException( "Required elective column header \"NAME\" or \"STADT/GEMEINDE\" not specified in excel file." );
        }
        if (!columnNames.containsValue( "LON" )) {
            throw new IllegalArgumentException( "Required column header \"LON\" not specified in excel file." );
        }
        if (!columnNames.containsValue( "LAT" )) {
            throw new IllegalArgumentException( "Required column header \"LAT\" not specified in excel file." );
        }

    }

    /**
     * Checks if a given URL is shorter than another URL. Ignores the protocol.
     *
     * @param bm
     * @return True if url is shorter than urlComparedTo. False otherwise (url
     *         == NULL; urlComparedTo == NULL; url longer or equal long.);
     * @throws MalformedURLException
     */
    public static boolean isUrlShorterThan(String url, String urlComparedTo) throws MalformedURLException {
        if (url == null || url.trim().length() == 0 || urlComparedTo == null || urlComparedTo.trim().length() == 0) {
            return false;
        }

        URL urlObj = new URL( url );
        String urlStr = url.substring( url.indexOf( urlObj.getProtocol() ) + urlObj.getProtocol().length(), url.length() );
        URL urlComparedToObj = new URL( urlComparedTo );
        String urlComparedToStr = urlComparedTo.substring( urlComparedTo.indexOf( urlComparedToObj.getProtocol() ) + urlComparedToObj.getProtocol().length(), urlComparedTo.length() );

        if (urlStr.startsWith( urlComparedToStr )) {
            return false;
        } else {
            return true;
        }
    }

    public static String getActualUrl(String url, BlpModel bm) throws Exception {

        int termination = 10;
        while (termination-- > 0) {
            String actualUrl = null;
            actualUrl = getRedirect( url, bm );
            if (actualUrl.equals( url )) {
                return url;
            }
            bm.errors.add( new UVPDataImporter().new StatusEntry( "Redirect detected: '" + url + "' -> '" + actualUrl + "'.", "REDIRECTS" ) );
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
        bm.errors.add( new UVPDataImporter().new StatusEntry( "Too many redirects.", "IGNORED" ) );
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
            TrustModifier.relaxHostChecking( con );
            con.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:58.0) Gecko/20100101 Firefox/58.0" );
            con.setInstanceFollowRedirects( false );
            con.setRequestMethod( "HEAD" );
            con.setConnectTimeout( 5000 );
            con.setReadTimeout( 5000 );
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
                throw new Exception( "Problems accessing '" + urlstring + " (HTTP_ERROR: " + responseCode + ") (" + e + ")" );
            } else {
                throw new Exception( "Problems accessing '" + urlstring + " (HTTP_ERROR: " + responseCode + ")" );
            }
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
        String urlBlpInProgress;
        String urlBlpFinished;
        String urlFnpInProgress;
        String urlFnpFinished;
        String urlBpInProgress;
        String urlBpFinished;
        String descr;
        boolean hasMarker = false;
        List<StatusEntry> errors = new ArrayList<StatusEntry>();

        @Override
        public String toString() {
            return "[name: " + name + "; lat:" + lat + "; lon:" + lon + "; urlBlpInProgress:" + urlBlpInProgress + "; urlBlpFinished:" + urlBlpFinished + "; urlFnpInProgress:" + urlFnpInProgress
                    + "; urlFnpFinished:" + urlFnpFinished + "; urlBpInProgress:" + urlBpInProgress + "; urlBpFinished:" + urlBpFinished + "; descr:" + descr + "]";
        }

    }

}
