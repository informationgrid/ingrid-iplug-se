package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.Index;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.DefaultFieldsQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.FieldQueryIGCConverter;
import de.ingrid.iplug.se.elasticsearch.converter.FuzzyQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.IQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.MatchAllQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.WildcardQueryConverter;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JettyStarter.class)
public class IndexImplTest {

    static Index index = null;
    private static ElasticsearchNodeFactoryBean elastic;

    private static int maxResult = 11;
    
    @Mock JettyStarter jettyStarter;
    
    @BeforeClass
    public static void setUp() throws Exception {
        elastic = new ElasticsearchNodeFactoryBean();
        elastic.afterPropertiesSet();
        
        // set necessary configurations for startup
        Configuration configuration = new Configuration();
        configuration.searchType = SearchType.DFS_QUERY_THEN_FETCH;
        SEIPlug.conf = configuration;
        
        
        setMapping();
        prepareIndex();
    }
    
    @Before
    public void initTest() throws Exception {
        PowerMockito.mockStatic( JettyStarter.class );
        Mockito.when(JettyStarter.getInstance()).thenReturn( jettyStarter );
        
        Config config = new Config();
        config.communicationProxyUrl = "/ingrid-group:iplug-se-test";
        jettyStarter.config = config;
        
        QueryConverter qc = new QueryConverter();
        List<IQueryConverter> parsers = new ArrayList<IQueryConverter>();
        parsers.add( new DefaultFieldsQueryConverter() );
        parsers.add( new WildcardQueryConverter() );
        parsers.add( new FuzzyQueryConverter() );
        parsers.add( new FieldQueryIGCConverter() );
        parsers.add( new MatchAllQueryConverter() );
        qc.setQueryParsers( parsers );
        
        index = new IndexImpl( elastic, qc );
    }
    
    
//    @Before
//    public void initTest() {
//        MockitoAnnotations.initMocks(this);
//    }


    @AfterClass
    public static void tearDown() throws Exception {
        index.close();
    }

    @Test
    public void searchForAll() {
        IngridQuery q = ExampleQuery.byTerm( "" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( maxResult ) ) );
    }

    @Test
    public void searchForOneTerm() {
        IngridQuery q = ExampleQuery.byTerm( "wemove" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        checkHitsForIDs( search.getHits(), 1, 6, 7, 8 );
    }

    @Test
    public void searchForMultipleTermsWithAnd() {
        IngridQuery q = ExampleQuery.byTerm( "Welt wemove" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 1 );
    }

    @Test
    public void searchForMultipleTermsWithOr() {
        IngridQuery q = ExampleQuery.byTerm( "wemove OR reisen" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        checkHitsForIDs( search.getHits(), 1, 6, 7, 8, 11 );
        
        q = ExampleQuery.byTerm( "((wemove) OR (reisen))" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        checkHitsForIDs( search.getHits(), 1, 6, 7, 8, 11 );
    }

    /*
     * Show me all docs containing (Welt AND wemove) plus every doc
     * containing "golem".
     */
    @Test
    public void searchForMultipleTermsWithAndOr() {
        IngridQuery q = ExampleQuery.byTerm( "Welt AND wemove OR golem" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        checkHitsForIDs( search.getHits(), 1, 4, 5 );
    }
    
    @Test
    public void searchForMultipleTermsWithAndOrParentheses() {
        IngridQuery q = ExampleQuery.byTerm( "Welt AND (wemove OR golem)" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        checkHitsForIDs( search.getHits(), 1, 4 );
    }
    
    @Test
    public void searchForTermNot() {
        IngridQuery q = ExampleQuery.byTerm( "-wemove" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( maxResult - 4 ) );
        checkHitsForIDs( search.getHits(), 2, 3, 4, 5, 9, 10, 11 );
    }
    
    @Test
    public void searchForMultipleTermsNot() {
        IngridQuery q = ExampleQuery.byTerm( "Welt -wemove" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        checkHitsForIDs( search.getHits(), 4, 11 );
    }
    
    @Test
    public void searchWithWildcardCharacter() {
        // the term Deutschland should be found
        IngridQuery q = ExampleQuery.byTerm( "Deutschl?nd" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 9 );
        
        // should not find the following, because only one character is a wildcard!
        q = ExampleQuery.byTerm( "Deutschl?d" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        q = ExampleQuery.byTerm( "au?" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        checkHitsForIDs( search.getHits(), 1, 4, 10 );
    }
    
    @Test
    public void searchWithWildcardString() {
        IngridQuery q = ExampleQuery.byTerm( "Deutschl*nd" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 9 );
        
        q = ExampleQuery.byTerm( "Deutschl*d" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 9 );
        
        q = ExampleQuery.byTerm( "au*" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 6 ) );
        checkHitsForIDs( search.getHits(), 1, 4, 7, 9, 10, 11 );
    }
    
    @Test
    public void searchFuzzy() {
        IngridQuery q = ExampleQuery.byTerm( "Deutschlnad" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        q = ExampleQuery.byTerm( "Deutschlnad~" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 9 );
    }
    
    @Test
    public void searchFuzzyCombination() {
        IngridQuery q = ExampleQuery.byTerm( "faxen Deutschlnad~" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 9 );
        
        q = ExampleQuery.byTerm( "wemove -Wetl~" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        checkHitsForIDs( search.getHits(), 6, 7, 8 );
    }
    
    @Test
    public void searchField() {
        IngridQuery q = ExampleQuery.byTerm( "title:ausland" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 9 );
        
    }
    
    @Test
    public void searchPhrase() {
        IngridQuery q = ExampleQuery.byTerm( "\"der Wirtschaft\"" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 10 );
        
        q = ExampleQuery.byTerm( "\"Welt der Computer\"" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 4 );
        
        // TODO: this should return two documents but somehow is a query of a stopword (here 'der')
        // resulting in no results!!!
        q = ExampleQuery.byTerm( "Welt der Computer" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        checkHitsForIDs( search.getHits(), 4, 11 );
    }
    
    @Test
    public void searchWithPaging() {
        IngridQuery q = ExampleQuery.byTerm( "" );
        IngridHits search = index.search( q, 0, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( maxResult ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        //checkHitsForIDs( search.getHits(), 3, 8, 10, 1, 6 );

        search = index.search( q, 5, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( maxResult ) ) );
        assertThat( search.getHits().length, is( 5 ) );
        //checkHitsForIDs( search.getHits(), 2, 7, 4, 9, 11 );
        
        search = index.search( q, 10, 5 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( maxResult ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        //checkHitsForIDs( search.getHits(), 5 );
    }

    @Test
    public void searchForDates() {
        // all results are completely inside the time range
        IngridQuery q = ExampleQuery.byTerm( "t1:2014-03-30 t2:2014-04-30" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        checkHitsForIDs( search.getHits(), 2, 7 );
        
        // at least one result is on the edge of the time range
        q = ExampleQuery.byTerm( "t1:2014-04-01 t2:2014-04-15" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        checkHitsForIDs( search.getHits(), 2, 7 );
        
        // one result is only partially in the time range and will not be listed
        q = ExampleQuery.byTerm( "t1:2014-04-03 t2:2014-04-18" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 7 );
        
        // we only search for concrete date, which is not found
        // although a time range includes this date it will not be found since
        // option "include" is off!
        q = ExampleQuery.byTerm( "t0:2014-04-07" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // we only search for concrete date, which is found
        q = ExampleQuery.byTerm( "t0:2014-04-04" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 7 );
    }
    
    @Test
    public void searchForDatesIntersect() {
        // intersect two time ranges (with intersection below t2!) where one is completely inside and a date
        IngridQuery q = ExampleQuery.byTerm( "t1:2014-03-30 t2:2014-04-30 time:intersect" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        checkHitsForIDs( search.getHits(), 2, 5, 7 );
        
        // intersect two time ranges (with intersection above t1!) and a date
        q = ExampleQuery.byTerm( "t1:2014-03-30 t2:2014-04-10 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        checkHitsForIDs( search.getHits(), 2, 5, 7 );
        
        // all docs containing a given date as a start/end date 
        q = ExampleQuery.byTerm( "t0:2014-04-07 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with a single date 
        q = ExampleQuery.byTerm( "t0:2014-04-04 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 7 );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with a starting date 
        q = ExampleQuery.byTerm( "t0:2014-04-01 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 2 );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with an end date 
        q = ExampleQuery.byTerm( "t0:2014-04-15 time:intersect" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        checkHitsForIDs( search.getHits(), 2 );
    }
    
    @Test
    public void searchForDatesInclude() {
        IngridQuery q = ExampleQuery.byTerm( "t1:2014-03-30 t2:2014-04-30 time:include" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        // document 5 is only intersecting and so not in the results!!!
        checkHitsForIDs( search.getHits(), 2, 7, 8 );
        
        // all docs intersecting one date 
        q = ExampleQuery.byTerm( "t0:2014-04-07 time:include" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        checkHitsForIDs( search.getHits(), 2, 5, 8 );
        
        // all docs intersecting one date
        // -> match also doc with exact end date (doc: 5)
        q = ExampleQuery.byTerm( "t0:2014-04-09 time:include" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        checkHitsForIDs( search.getHits(), 2, 5, 8 );
        
        // all docs intersecting one date
        // -> match a doc with a single date
        q = ExampleQuery.byTerm( "t0:2014-04-04 time:include" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
    }
    
    @Test
    public void searchForDatesIntersectAndInclude() {
        IngridQuery q = ExampleQuery.byTerm( "((t0:2014-04-04 time:intersect) OR (t0:2014-04-04 time:include))" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
        
        q = ExampleQuery.byTerm( "((t1:2014-03-30 t2:2014-04-30 time:intersect) OR (t1:2014-03-30 t2:2014-04-30 time:include))" );
        search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
        
    }

    @Test @Ignore
    public void searchForLocation() {
        fail( "Not yet implemented" );
    }

    @Test @Ignore
    public void searchForTermDateLocation() {
        fail( "Not yet implemented" );
    }

    @Test @Ignore
    public void searchInFieldForTerm() {
        fail( "Not yet implemented" );
    }

    @Test
    public void getDetail() {
        IngridQuery q = ExampleQuery.byTerm( "Welt wemove" );
        IngridHits search = index.search( q, 0, 10 );
        IngridHitDetail detail = index.getDetail( search.getHits()[0], null );
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
        IngridQuery q = ExampleQuery.byTerm( "Welt wemove" );
        IngridHits search = index.search( q, 0, 10 );
        String[] extraFields = new String[] { "fetched" };
        IngridHitDetail detail = index.getDetail( search.getHits()[0], extraFields );
        assertThat( detail, not( is( nullValue() ) ) );
        // assertThat( detail.getHitId(), is( "1" ) );
        assertThat( detail.getDocumentId(), is( 1 ) );
        assertThat( detail.getString( "fetched" ), is( "2014-06-03" ) );
    }

    @Test @Ignore
    public void testDeleteUrl() {
        fail( "Not yet implemented" );
    }

    
    
    
    private static void prepareIndex() throws Exception {
        Client client = elastic.getObject().client();
        ClassPathResource resource = new ClassPathResource( "data/webUrls.json" );

        byte[] urlsData = Files.readAllBytes( Paths.get( resource.getURI() ) );

        BulkResponse indexResponse = client.prepareBulk().add( urlsData, 0, urlsData.length, true ).execute()
                .actionGet();

        // make sure the indexed data is available immediately during search!
        RefreshRequest refreshRequest = new RefreshRequest( "test" );
        client.admin().indices().refresh( refreshRequest ).actionGet();
    }

    private static void setMapping() {
        String mappingSource = "";
        try {
            Client client = elastic.getObject().client();
            ClassPathResource resource = new ClassPathResource( "data/mapping.json" );

            List<String> urlsData = Files.readAllLines( Paths.get( resource.getURI() ), Charset.defaultCharset() );
            for (String line : urlsData) {
                mappingSource += line;
            }
            
            if (client.admin().indices().prepareExists("test").execute().actionGet().isExists()) {
                client.admin().indices().prepareDelete("test").execute().actionGet();
            }
            client.admin().indices().prepareCreate("test").execute().actionGet();
            
            PutMappingResponse mapping = client.admin().indices().preparePutMapping().setIndices( "test" )
                    .setType("web")
                    .setSource( mappingSource )
                    .execute()
                    .actionGet();
            
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    private void checkHitsForIDs(IngridHit[] hits, int... ids) {
        for (int id : ids) {
            boolean found = false;
            for (IngridHit hit : hits) {
                if (hit.getDocumentId() == id) {
                    found = true;
                    break;
                }
            }
            assertThat("The following ID was not found in the results: " + id, found, is(true));
        }        
    }
    
}
