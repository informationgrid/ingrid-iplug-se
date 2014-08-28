package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JettyStarter.class)
public class DateSearchTest  {

    @Mock JettyStarter jettyStarter;
    
    private IndexImpl index;
    private static ElasticsearchNodeFactoryBean elastic;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        elastic = Utils.setupES();
    }
    
    @Before
    public void initTest() throws Exception {
        QueryConverter qc = Utils.getQueryConverter( jettyStarter );
        index = new IndexImpl( elastic, qc, new FacetConverter() );
    }

    @Test
    public void searchForDates() {
        // all results are completely inside the time range
        IngridQuery q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-30" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 7 );
        
        // at least one result is on the edge of the time range
        q = Utils.getIngridQuery( "t1:2014-04-01 t2:2014-04-15" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 7 );
        
        // one result is only partially in the time range and will not be listed
        q = Utils.getIngridQuery( "t1:2014-04-03 t2:2014-04-18" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 7 );
        
        // we only search for concrete date, which is not found
        // although a time range includes this date it will not be found since
        // option "include" is off!
        q = Utils.getIngridQuery( "t0:2014-04-07" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // we only search for concrete date, which is found
        q = Utils.getIngridQuery( "t0:2014-04-04" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 7 );
    }
    
    @Test
    public void searchForDatesIntersect() {
        // intersect two time ranges (with intersection below t2!) where one is completely inside and a date
        IngridQuery q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-30 time:intersect" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7 );
        
        // intersect two time ranges (with intersection above t1!) and a date
        q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-10 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7 );
        
        // all docs containing a given date as a start/end date 
        q = Utils.getIngridQuery( "t0:2014-04-07 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with a single date 
        q = Utils.getIngridQuery( "t0:2014-04-04 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 7 );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with a starting date 
        q = Utils.getIngridQuery( "t0:2014-04-01 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with an end date 
        q = Utils.getIngridQuery( "t0:2014-04-15 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
    }
    
    @Test
    public void searchForDatesInclude() {
        IngridQuery q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-30 time:include" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        // document 5 is only intersecting and so not in the results!!!
        Utils.checkHitsForIDs( search.getHits(), 2, 7, 8 );
        
        // all docs intersecting one date 
        q = Utils.getIngridQuery( "t0:2014-04-07 time:include" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 8 );
        
        // all docs intersecting one date
        // -> match also doc with exact end date (doc: 5)
        q = Utils.getIngridQuery( "t0:2014-04-09 time:include" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 8 );
        
        // all docs intersecting one date
        // -> match a doc with a single date
        q = Utils.getIngridQuery( "t0:2014-04-04 time:include" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
    }
    
    @Test
    public void searchForDatesIntersectAndInclude() {
        IngridQuery q = Utils.getIngridQuery( "((t0:2014-04-04 time:intersect) OR (t0:2014-04-04 time:include))" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
        
        q = Utils.getIngridQuery( "((t1:2014-03-30 t2:2014-04-30 time:intersect) OR (t1:2014-03-30 t2:2014-04-30 time:include))" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
        
    }
}
