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
        CommandLine cmd = parser.parse( options, args );

        String instance = null;
        if (cmd.hasOption( "instance" )) {
            instance = cmd.getOptionValue( "instance" );
        } else {
            System.out.println( "Missing patameter 'instance'." );
            System.exit( 0 );
        }

        String excelfile = null;
        if (cmd.hasOption( "excelfile" )) {
            excelfile = cmd.getOptionValue( "excelfile" );
        } else {
            System.out.println( "Missing patameter 'excelfile'." );
            System.exit( 0 );
        }

        String partner = null;
        if (cmd.hasOption( "partner" )) {
            partner = cmd.getOptionValue( "partner" );
        } else {
            System.out.println( "Missing patameter 'partner'." );
            System.exit( 0 );
        }

        conf = new ConfigBuilder<Configuration>( Configuration.class ).withCommandLineArgs( args ).build();

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

            List<BlpModel> blpModels = readData( excelfile );

            for (Url url : existingUrls) {
                em.remove( url );
            }

            for (BlpModel bm : blpModels) {
                em.getTransaction().begin();

                if (bm.urlInProgress != null && bm.urlInProgress.length() > 0) {
                    Url url = createUrl( instance, partner, bm.urlInProgress, bm, true );
                    em.persist( url );
                }
                if (bm.urlFinished != null && bm.urlFinished.length() > 0) {
                    Url url = createUrl( instance, partner, bm.urlFinished, bm, false );
                    em.persist( url );
                }
            }

            tx.commit();
        } catch (RuntimeException e) {
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
                    validate( bm );
                    blpModels.add( bm );
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

    private static Url createUrl(String instance, String partner, String urlStr, BlpModel bm, boolean writeCoordinateInformation) throws MalformedURLException {
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
        md.setMetaKey( "blp_name" );
        md.setMetaValue( bm.name );
        metadata.add( md );

        md = new Metadata();
        md.setMetaKey( "procedure" );
        md.setMetaValue( "dev_plan" );
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

        return idxUrl;

    }

    private static void validate(BlpModel bm) {
        if (bm.name == null || bm.name.length() <= 3) {
            System.out.println( "Name is null or too short." + bm );
        }

        if (bm.lat < 47 || bm.lat > 56) {
            System.out.println( "Lat not between 47 and 56." + bm );
        }

        if (bm.lon < 5 || bm.lon > 15) {
            System.out.println( "Lon not between 5 and 15." + bm );
        }

        String url = bm.urlInProgress;
        if (url != null && url.length() > 0) {
            try {
                URLConnection conn = new URL( url ).openConnection();
                conn.connect();
            } catch (Exception e) {
                System.out.println( "Problems accessing '" + url + "'. " + bm );
            }
        }

        url = bm.urlFinished;
        if (url != null && url.length() > 0) {
            try {
                URLConnection conn = new URL( url ).openConnection();
                conn.connect();
            } catch (Exception e) {
                System.out.println( "Problems accessing '" + url + "'. " + bm );
            }
        }
    }

    private static void setupTestData(EntityManager em) {
        em.getTransaction().begin();

        // check first if test data already has been added
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Url> criteria = cb.createQuery( Url.class );
        Root<Url> urlTable = criteria.from( Url.class );
        Predicate instanceCriteria = cb.equal( urlTable.get( "instance" ), "catalog" );

        criteria.from( Url.class );
        criteria.where( instanceCriteria );

        int size = em.createQuery( criteria ).getResultList().size();

        if (size == 0) {

            Url url = new Url( "catalog" );
            url.setStatus( "200" );
            url.setUrl( "http://www.wemove.com/" );
            List<Metadata> metadata = new ArrayList<Metadata>();
            Metadata m1 = new Metadata();
            m1.setMetaKey( "lang" );
            m1.setMetaValue( "en" );
            Metadata m2 = new Metadata();
            m2.setMetaKey( "topic" );
            m2.setMetaValue( "t2" );
            Metadata m3 = new Metadata();
            m3.setMetaKey( "topic" );
            m3.setMetaValue( "t3" );
            Metadata m4 = new Metadata();
            m4.setMetaKey( "unknown" );
            m4.setMetaValue( "xxx" );
            Metadata m5 = new Metadata();
            m5.setMetaKey( "topic" );
            m5.setMetaValue( "angularjs" );
            metadata.add( m1 );
            metadata.add( m2 );
            metadata.add( m3 );
            metadata.add( m4 );
            metadata.add( m5 );
            url.setMetadata( metadata );
            List<String> limitUrls = new ArrayList<String>();
            limitUrls.add( "http://www.wemove.com/" );
            url.setLimitUrls( limitUrls );
            List<String> excludeUrls = new ArrayList<String>();
            excludeUrls.add( "http://www.wemove.com/about" );
            url.setExcludeUrls( excludeUrls );

            em.persist( url );

            String[] urls = new String[] { "http://www.spiegel.de", "http://www.heise.de", "http://www.apple.com", "http://www.engadget.com", "http://www.tagesschau.de",
                    "http://www.home-mag.com/", "http://www.ultramusicfestival.com/", "http://www.ebook.de/de/", "http://www.audible.de", "http://www.amazon.com",
                    "http://www.powerint.com/", "http://www.tanzkongress.de/", "http://www.thesourcecode.de/", "http://werk-x.at/", "http://keinundapel.com/",
                    "http://www.ta-trung.com/", "http://www.attac.de/", "http://www.altana-kulturstiftung.de/", "http://www.lemagazinedouble.com/",
                    "http://www.montessori-muehlheim.de/", "http://missy-magazine.de/", "http://www.eh-darmstadt.de/", "http://herbert.de/", "http://www.mousonturm.de/",
                    "http://www.zeit.de/", "https://read2burn.com/" };

            metadata = new ArrayList<Metadata>();
            Metadata md = new Metadata();
            md.setMetaKey( "lang" );
            md.setMetaValue( "de" );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "partner" );
            md.setMetaValue( "bund" );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "provider" );
            md.setMetaValue( "bu_bmu" );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "datatype" );
            md.setMetaValue( "www" );
            metadata.add( md );

            md = new Metadata();
            md.setMetaKey( "datatype" );
            md.setMetaValue( "default" );
            metadata.add( md );

            for (String uri : urls) {
                url = new Url( "catalog" );
                url.setStatus( "400" );
                url.setUrl( uri );
                List<String> limit = new ArrayList<String>();
                limit.add( uri );
                url.setLimitUrls( limit );
                url.setMetadata( metadata );
                em.persist( url );
            }

            url = new Url( "other" );
            url.setStatus( "200" );
            url.setUrl( "http://de.wikipedia.org/" );
            List<String> limit = new ArrayList<String>();
            limit.add( "http://de.wikipedia.org" );
            url.setLimitUrls( limit );
            em.persist( url );
        }

        em.getTransaction().commit();
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
