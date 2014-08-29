package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JettyStarter.class)
public class GeneralSearchTest {

    @Mock JettyStarter jettyStarter;
    
    @BeforeClass
    public static void setUp() throws Exception {
        Utils.setupES();
    }    
    
    @Before
    public void initTest() throws Exception {
        Utils.initIndex( jettyStarter );
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        Utils.index.close();
    }

    @Test
    public void searchForAll() {
        IngridQuery q = Utils.getIngridQuery( "" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );
    }

    @Test
    public void searchForOneTerm() {
        IngridQuery q = Utils.getIngridQuery( "wemove" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 6, 7, 8 );
    }

    @Test
    public void searchForMultipleTermsWithAnd() {
        IngridQuery q = Utils.getIngridQuery( "Welt wemove" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 1 );
    }

    @Test
    public void searchForMultipleTermsWithOr() {
        IngridQuery q = Utils.getIngridQuery( "wemove OR reisen" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 6, 7, 8, 11 );
        
        q = Utils.getIngridQuery( "((wemove) OR (reisen))" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 6, 7, 8, 11 );
    }

    /*
     * Show me all docs containing (Welt AND wemove) plus every doc
     * containing "golem".
     */
    @Test
    public void searchForMultipleTermsWithAndOr() {
        IngridQuery q = Utils.getIngridQuery( "Welt AND wemove OR golem" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 5 );
    }
    
    @Test
    public void searchForMultipleTermsWithAndOrParentheses() {
        IngridQuery q = Utils.getIngridQuery( "Welt AND (wemove OR golem)" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4 );
    }
    
    @Test
    public void searchForTermNot() {
        IngridQuery q = Utils.getIngridQuery( "-wemove" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( (int)Utils.MAX_RESULTS - 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 3, 4, 5, 9, 10, 11 );
    }
    
    @Test
    public void searchForMultipleTermsNot() {
        IngridQuery q = Utils.getIngridQuery( "Welt -wemove" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 4, 11 );
    }
    
    @Test
    public void searchWithWildcardCharacter() {
        // the term Deutschland should be found
        IngridQuery q = Utils.getIngridQuery( "Deutschl?nd" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
        
        // should not find the following, because only one character is a wildcard!
        q = Utils.getIngridQuery( "Deutschl?d" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        q = Utils.getIngridQuery( "au?" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 10 );
    }
    
    @Test
    public void searchWithWildcardString() {
        IngridQuery q = Utils.getIngridQuery( "Deutschl*nd" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
        
        q = Utils.getIngridQuery( "Deutschl*d" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
        
        q = Utils.getIngridQuery( "au*" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 6 ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 7, 9, 10, 11 );
    }
    
    @Test
    public void searchFuzzy() {
        IngridQuery q = Utils.getIngridQuery( "Deutschlnad" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        q = Utils.getIngridQuery( "Deutschlnad~" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
    }
    
    @Test
    public void searchFuzzyCombination() {
        IngridQuery q = Utils.getIngridQuery( "faxen Deutschlnad~" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
        
        q = Utils.getIngridQuery( "wemove -Wetl~" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 6, 7, 8 );
    }
    
    @Test
    public void searchField() {
        IngridQuery q = Utils.getIngridQuery( "title:ausland" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 9 );
    }
    
    @Test
    public void searchFieldAND() {
        IngridQuery q = Utils.getIngridQuery( "abstract:urlaub abstract:welt" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 1l ) );
        Utils.checkHitsForIDs( search.getHits(), 11 );
    }
    
    @Test
    public void searchFieldOR() {
        IngridQuery q = Utils.getIngridQuery( "abstract:urlaub OR abstract:welt" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 3l ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 4, 11 );
    }
    
    @Test
    public void searchFieldSpecialAND() {
        IngridQuery q = Utils.getIngridQuery( "partner:bund datatype:pdf" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 1l ) );
        Utils.checkHitsForIDs( search.getHits(), 1 );
    }
    
    @Test
    public void searchFieldSpecialOR() {
        IngridQuery q = Utils.getIngridQuery( "datatype:xml OR datatype:pdf" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( 5l ) );
        Utils.checkHitsForIDs( search.getHits(), 1, 7, 8, 10, 11 );
    }
    
    @Test
    public void searchPhrase() {
        IngridQuery q = Utils.getIngridQuery( "\"der Wirtschaft\"" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 10 );
        
        q = Utils.getIngridQuery( "\"Welt der Computer\"" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 4 );
        
        // TODO: this should return two documents but somehow is a query of a stopword (here 'der')
        // resulting in no results!!!
        q = Utils.getIngridQuery( "Welt der Computer" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 4, 11 );
    }
    
    @Test
    public void searchWithPaging() {
        IngridQuery q = Utils.getIngridQuery( "" );
        IngridHits search = Utils.index.search( q, 0, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        //Utils.checkHitsForIDs( search.getHits(), 3, 8, 10, 1, 6 );

        search = Utils.index.search( q, 5, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        //Utils.checkHitsForIDs( search.getHits(), 2, 7, 4, 9, 11 );
        
        search = Utils.index.search( q, 10, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        //Utils.checkHitsForIDs( search.getHits(), 5 );
    }

    @Test @Ignore
    public void searchForTermDateLocation() {
        fail( "Not yet implemented" );
    }

    @Test
    public void getDetail() {
        IngridQuery q = Utils.getIngridQuery( "Welt wemove" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        IngridHitDetail detail = Utils.index.getDetail( search.getHits()[0], null );
        assertThat( detail, not( is( nullValue() ) ) );
        // assertThat( detail.getHitId(), is( "1" ) );
        assertThat( detail.getDocumentId(), is( 1 ) );
        assertThat( detail.get("fetched"), is( nullValue() ) );
        assertThat( detail.getTitle(), is( "wemove" ) );
        assertThat( detail.getSummary(), is( "Die beste IT-Firma auf der Welt!" ) );
        assertThat( detail.getScore(), greaterThan( 0.1f ) );
    }
    
    @Test
    public void getDetailWithRequestedField() {
        IngridQuery q = Utils.getIngridQuery( "Welt wemove" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        String[] extraFields = new String[] { "fetched" };
        IngridHitDetail detail = Utils.index.getDetail( search.getHits()[0], extraFields );
        assertThat( detail, not( is( nullValue() ) ) );
        // assertThat( detail.getHitId(), is( "1" ) );
        assertThat( detail.getDocumentId(), is( 1 ) );
        assertThat( detail.getString( "fetched" ), is( "2014-06-03" ) );
    }
    

    @Test @Ignore
    public void testDeleteUrl() {
        fail( "Not yet implemented" );
    }
    
}
