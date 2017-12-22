/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

/**
 * UVP data importer. Imports data from an excel file directly into the url
 * database.
 * 
 * All urls from instance are deleted.
 * 
 * The limit urls are set to the domain of the start url. Make sure the depth of
 * the crawl is set to 1.
 * 
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

    public static void main(String[] args) throws Exception {

        CommandLineParser parser = new BasicParser();
        Options options = new Options();
        @SuppressWarnings("static-access")
        Option instanceOption = OptionBuilder.withArgName( "instance name" ).hasArg().withDescription( "an existing instance name" ).create( "instance" );
        options.addOption( instanceOption );
        @SuppressWarnings("static-access")
        Option excelfileOption = OptionBuilder.withArgName( "excel file name" ).hasArg().withDescription( "path tp excel file" ).create( "excelfile" );
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

            for (Url url : existingUrls) {
                em.remove( url );
            }

            int cntUrls = 0;

            for (BlpModel bm : blpModels) {

                // for display in map we need ONE marker per blp dataset
                // therefore the map marker data is pushed to index only for one
                // of the URLs
                boolean pushBlpDataToIndex = true;

                System.out.println( "Add entry '" + bm.name + "'." );
                if (bm.urlInProgress != null && bm.urlInProgress.length() > 0) {
                    Url url = createUrl( instance, partner, bm.urlInProgress, bm, pushBlpDataToIndex );
                    pushBlpDataToIndex = false;
                    em.persist( url );
                    cntUrls++;
                }
                if (bm.urlFinished != null && bm.urlFinished.length() > 0 && bm.urlFinished != bm.urlInProgress) {
                    Url url = createUrl( instance, partner, bm.urlFinished, bm, pushBlpDataToIndex );
                    em.persist( url );
                    cntUrls++;
                }
            }
            System.out.println( "Finish. Added  " + cntUrls + " urls to instance '" + instance + "'." );

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
        return urlStr.substring( 0, urlStr.indexOf( host ) + host.length() ) + "/";
    }

    /**
     * Scan Excel file and gathe alle infos.
     * 
     * @param excelFile
     * @return
     * @throws IOException
     */
    public static List<BlpModel> readData(String excelFile) throws IOException {
        List<BlpModel> blpModels = new ArrayList<BlpModel>();

        boolean hasErrors = false;

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
                // skip first row
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

                    if (bm.name != null && bm.name.length() > 0) {
                        hasErrors = hasErrors | !validate( bm );
                        blpModels.add( bm );
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

        if (hasErrors) {
            System.out.println( "Excel data has errors. Please correct!" );
            System.exit( 0 );
        }

        return blpModels;

    }

    private static Url createUrl(String instance, String partner, String urlStr, BlpModel bm, boolean pushBlpDataToIndex) throws MalformedURLException {
        Url idxUrl = new Url( instance );

        idxUrl.setStatus( "200" );
        idxUrl.setUrl( urlStr );

        String lUrl = getDomain( urlStr );
        List<String> limitUrls = new ArrayList<String>();
        limitUrls.add( lUrl );
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
        md.setMetaKey( "partner" );
        md.setMetaValue( partner );
        metadata.add( md );

        if (pushBlpDataToIndex) {
            md = new Metadata();
            md.setMetaKey( "blp_name" );
            md.setMetaValue( bm.name );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "blp_description" );
            md.setMetaValue( bm.descr );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "blp_url_finished" );
            md.setMetaValue( bm.urlFinished );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "blp_url_in_progress" );
            md.setMetaValue( bm.urlInProgress );
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
        }

        idxUrl.setMetadata( metadata );

        return idxUrl;

    }

    private static boolean validate(BlpModel bm) {
        boolean isValid = true;

        if (bm.name == null || bm.name.length() <= 3) {
            isValid = false;
            System.out.println( "Name is null or too short." + bm );
        }

        if (bm.lat < 47 || bm.lat > 56) {
            isValid = false;
            System.out.println( "Lat not between 47 and 56." + bm );
        }

        if (bm.lon < 5 || bm.lon > 15) {
            isValid = false;
            System.out.println( "Lon not between 5 and 15." + bm );
        }

        String url = bm.urlInProgress;
        if (url != null && url.length() > 0) {
            try {
                URLConnection conn = new URL( url ).openConnection();
                conn.connect();
            } catch (Exception e) {
                isValid = false;
                System.out.println( "Problems accessing '" + url + "'. " + bm + ": " + e );
            }
        }

        url = bm.urlFinished;
        if (url != null && url.length() > 0) {
            try {
                URLConnection conn = new URL( url ).openConnection();
                conn.connect();
            } catch (Exception e) {
                isValid = false;
                System.out.println( "Problems accessing '" + url + "'. " + bm + ": " + e );
            }
        }

        return isValid;
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