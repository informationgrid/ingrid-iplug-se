package de.ingrid.iplug.se.migrate;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;

import com.tngtech.configbuilder.ConfigBuilder;

import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.webapp.controller.ListInstancesController;

public class Migrator {
    private static Logger log = Logger.getLogger( Migrator.class.getName() );
    
    private static String ID = "ID";
//    private static String TYPE = "TYPE";
//    private static String STATUS_UPDATED = "STATUSUPDATED";
//    private static String CREATED = "CREATED";
//    private static String UPDATED = "UPDATED";
//    private static String DELETED = "DELETED";
//    private static String START_FK = "STARTURL_FK";
//    private static String PROVIDER_FK = "PROVIDER_FK";
    private static String STATUS = "STATUS";
    private static String URL = "URL";
    private static String METADATAKEY = "METADATAKEY";
    private static String METADATAVALUE = "METADATAVALUE";
    
    static Connection con = null;
    static Statement st = null;

    private static MigratorConfig conf;
    
    private static Url convertBasicUrl( ResultSet rs ) throws SQLException {
        Url u = new Url();
        u.setUrl( rs.getString( URL ) );
        // u.setCreated( rs.getDate( CREATED ) );
        // u.setDeleted( rs.getDate( DELETED ) );
        // u.setStatusUpdated( rs.getDate( STATUS_UPDATED ) );
        // u.setUpdated( rs.getDate( UPDATED ) );
        u.setStatus( rs.getInt( STATUS ) );
        
        // get metadata
        PreparedStatement stmt = con.prepareStatement( "SELECT * FROM url_metadata um, metadata m WHERE um.metadatas__id=m.id AND um.url__id=?" );
        stmt.setInt( 1, rs.getInt( ID ) );
        ResultSet rs_meta = stmt.executeQuery();
        //ResultSet rs_meta = st.executeQuery( "SELECT * FROM url u, url_metadata um, metadata m WHERE um.metadatas__id=m.id AND um.url__id=" + rs.getInt( ID ) );
        List<Metadata> metadataList = new ArrayList<Metadata>();
        while (rs_meta.next()) {
            Metadata metadata = new Metadata();
            metadata.setMetaKey( rs_meta.getString( METADATAKEY ) );
            metadata.setMetaValue( rs_meta.getString( METADATAVALUE ) );
            metadataList.add( metadata );
        }
        u.setMetadata( metadataList );
        rs_meta.close();
        
        // get provider/partner
        // not needed ... these are defined per iPlug
        
        return u;
    }
    
    private static List<Url> convertToWebUrls(ResultSet rs) throws SQLException {
        List<Url> webUrls = new ArrayList<Url>();
        
        // iterate over all Start Urls and convert them to the new format
        while (rs.next()) {
            
            log.debug( "Processing: " + rs.getInt( ID ) );
            Url u = convertBasicUrl( rs );
            
            u.setInstance( conf.webInstance );
            
            // LIMIT-Urls
            PreparedStatement stmt = con.prepareStatement( "SELECT * FROM url WHERE startUrl_fk=? AND type='LIMIT'" );
            stmt.setInt( 1, rs.getInt( ID ) );
            ResultSet rs_limit = stmt.executeQuery();
            List<String> limitUrls = new ArrayList<String>();
            while (rs_limit.next()) {
                limitUrls.add( rs_limit.getString( URL ) );
            }
            rs_limit.close();
            
            // EXCLUDE-Urls
            stmt = con.prepareStatement( "SELECT * FROM url WHERE startUrl_fk=? AND type='EXCLUDE'" );
            stmt.setInt( 1, rs.getInt( ID ) );
            ResultSet rs_exclude = stmt.executeQuery();
            List<String> excludeUrls = new ArrayList<String>();
            while (rs_exclude.next()) {
                excludeUrls.add( rs_exclude.getString( URL ) );
            }
            rs_exclude.close();
            
            u.setLimitUrls( limitUrls );
            u.setExcludeUrls( excludeUrls );
            
            webUrls.add( u );
        }
        return webUrls;
    }
    

    private static List<Url> convertToCatalogUrls(ResultSet rs) throws SQLException {
        List<Url> catalogUrls = new ArrayList<Url>();
        // iterate over all Catalog Urls and convert them to the new format
        while (rs.next()) {
            log.debug( "Processing Catalog: " + rs.getInt( ID ) );
            Url u = convertBasicUrl( rs );
            
            u.setInstance( conf.catalogInstance );
            
            // set the limit url with the value of the start url to only fetch this one page
            // in this case the start url must be a single page
            List<String> limitUrls = new ArrayList<String>();
            limitUrls.add( u.getUrl() );
            u.setLimitUrls( limitUrls );
            
            catalogUrls.add( u );
        }
        return catalogUrls;
    }
    
    public static void main(String[] args) throws Exception {
        
//        if (args.length != 3) {
//            System.out.println("Usage: Migrator.java <db-path> <username> <password>");
//            System.exit( -1 );
//        }

        conf = new ConfigBuilder<MigratorConfig>(MigratorConfig.class).withCommandLineArgs(args).build();
        
        ResultSet rs = null;

        //String url = "jdbc:mysql://localhost:3306/iplugse_ni";
        
        List<Url> webUrls = null;
        List<Url> catalogUrls = null;

        try {
            con = DriverManager.getConnection( conf.dbPath, conf.username, conf.password );
            st = con.createStatement();
            
            // get all start Urls
            log.info( "Fetch all web urls ..." );
            rs = st.executeQuery( "SELECT * FROM url WHERE startUrl_fk IS NULL AND type!='CATALOG'" );
            webUrls = convertToWebUrls( rs );
            rs.close();
            
            // get all catalog Urls
            log.info( "Fetch all catalog urls ..." );
            rs = st.executeQuery( "SELECT * FROM url WHERE type='CATALOG'" );
            catalogUrls = convertToCatalogUrls( rs );

            // add urls to h2 database
            Map<String, String> properties = new HashMap<String, String>();
            Path dbDir = Paths.get( conf.databaseDir );
            properties.put("javax.persistence.jdbc.url", "jdbc:h2:" + dbDir.toFile().getAbsolutePath() + "/urls;MVCC=true");
            EntityManagerFactory emf = null;
            if ( "iplug-se-dev".equals( conf.databaseID ) ) {
                emf = Persistence.createEntityManagerFactory(conf.databaseID);
            } else {
                emf = Persistence.createEntityManagerFactory(conf.databaseID, properties);
            }
            
            log.info( "Create instance directories." );
            // create directory for web instance
            boolean webInstanceDir = ListInstancesController.initializeInstanceDir( conf.getInstancesDir() + "/" + conf.webInstance );
            boolean catalogInstanceDir = false;
            
            // only create instance dir of catalog if it's a different directory
            if (!conf.webInstance.equals( conf.catalogInstance )) {
                catalogInstanceDir = ListInstancesController.initializeInstanceDir( conf.getInstancesDir() + "/" + conf.catalogInstance );
            }
            
            log.info( "Created web instance dir: " + webInstanceDir );
            log.info( "Created catalog instance dir: " + catalogInstanceDir );
            
            // add urls to database
            log.info( "Adding urls to our database." );
            DBManager.INSTANCE.intialize(emf);
            EntityManager em = DBManager.INSTANCE.getEntityManager();
            em.getTransaction().begin();
            
            // concatenate web and catalog urls
            webUrls.addAll( catalogUrls );
            
            for (Url url : webUrls) {
                em.persist( url );
            }
            
            em.getTransaction().commit();
            
            log.info( "Migration successful.\nPlease restart the iPlug for correct elastic search and scheduler setup." );
            
        } catch (SQLException ex) {
            log.error( ex.getMessage(), ex );

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (st != null) {
                    st.close();
                }
                if (con != null) {
                    con.close();
                }

            } catch (SQLException ex) {
                log.error( ex.getMessage(), ex );
            }
        }
    }
    
}
