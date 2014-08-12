package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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
import org.elasticsearch.client.Client;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.iplug.se.Index;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.DefaultFieldsQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.IQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.MatchAllQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

public class IndexImplTest {

    static Index index = null;
    private static ElasticsearchNodeFactoryBean elastic;

    private static int maxResult = 11;

    @BeforeClass
    public static void setUp() throws Exception {
        elastic = new ElasticsearchNodeFactoryBean();
        elastic.setConfigLocation( new ClassPathResource( "elasticsearch_memory.yml" ) );
        elastic.afterPropertiesSet();
        QueryConverter qc = new QueryConverter();
        List<IQueryConverter> parsers = new ArrayList<IQueryConverter>();
        parsers.add( new MatchAllQueryConverter() );
        parsers.add( new DefaultFieldsQueryConverter() );
        qc.setQueryParsers( parsers );
        index = new IndexImpl( elastic, qc );

        setMapping();
        prepareIndex();
    }


    @AfterClass
    public static void tearDown() throws Exception {
        index.close();
    }

    @Test
    public void searchForAll() {
        IngridQuery q = ExampleQuery.byTerm( "" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( maxResult ) );
    }

    @Test
    public void searchForOneTerm() {
        IngridQuery q = ExampleQuery.byTerm( "wemove" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
    }

    @Test
    public void searchForMultipleTermsWithAnd() {
        IngridQuery q = ExampleQuery.byTerm( "wemove jobs" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
    }

    @Test
    public void searchForMultipleTermsWithOr() {
        IngridQuery q = ExampleQuery.byTerm( "wemove OR reisen" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 5 ) );
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
    }
    
    @Test
    public void searchForMultipleTermsWithAndOrParentheses() {
        IngridQuery q = ExampleQuery.byTerm( "Welt AND (wemove OR golem)" );
        IngridHits search = index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
    }

    @Test
    public void searchForDates() {
        fail( "Not yet implemented" );
    }

    @Test
    public void searchForLocation() {
        fail( "Not yet implemented" );
    }

    @Test
    public void searchForTermDateLocation() {
        fail( "Not yet implemented" );
    }

    @Test
    public void searchInFieldForTerm() {
        fail( "Not yet implemented" );
    }

    @Test
    public void testGetDetail() {
        fail( "Not yet implemented" );
    }

    @Test
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
}
