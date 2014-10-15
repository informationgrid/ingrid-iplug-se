package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction.Modifier;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.DatatypePartnerProviderQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.DefaultFieldsQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.FieldQueryIGCConverter;
import de.ingrid.iplug.se.elasticsearch.converter.FuzzyQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.IQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.MatchAllQueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.iplug.se.elasticsearch.converter.WildcardQueryConverter;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

public class Utils {
    public static final long MAX_RESULTS = 11;
    
    public static IndexImpl index = null;

    private static ElasticsearchNodeFactoryBean elastic;

    public static void setupES() throws Exception {
        elastic = new ElasticsearchNodeFactoryBean();
        elastic.setLocal( true );
        elastic.afterPropertiesSet();
        
        // set necessary configurations for startup
        Configuration configuration = new Configuration();
        configuration.searchType = SearchType.DFS_QUERY_THEN_FETCH;
        configuration.index = "test";
        configuration.activeInstances = Arrays.asList( "web" );
        configuration.esBoostField = "boost";
        configuration.esBoostModifier = Modifier.LOG1P;
        configuration.esBoostFactor = 0.1f;
        configuration.esBoostMode = "sum";
        SEIPlug.conf = configuration;
        
        
        setMapping( elastic );
        prepareIndex( elastic );
    }
    
    
    private static void prepareIndex(ElasticsearchNodeFactoryBean elastic) throws Exception {
        Client client = elastic.getObject().client();
        ClassPathResource resource = new ClassPathResource( "data/webUrls.json" );

        byte[] urlsData = Files.readAllBytes( Paths.get( resource.getURI() ) );

        client.prepareBulk().add( urlsData, 0, urlsData.length, true )
                .execute()
                .actionGet();

        // make sure the indexed data is available immediately during search!
        RefreshRequest refreshRequest = new RefreshRequest( "test" );
        client.admin().indices().refresh( refreshRequest ).actionGet();
    }

    private static void setMapping(ElasticsearchNodeFactoryBean elastic) {
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
            
            client.admin().indices().preparePutMapping().setIndices( "test" )
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


    public static void initIndex(JettyStarter jettyStarter) {
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
        parsers.add( new DatatypePartnerProviderQueryConverter() );
        parsers.add( new MatchAllQueryConverter() );
        qc.setQueryParsers( parsers );
        
        index = new IndexImpl( elastic, qc, new FacetConverter() );
    }
    
    public static IngridQuery getIngridQuery( String term ) {
        try {
            return QueryStringParser.parse( term );
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
    public static void checkHitsForIDs(IngridHit[] hits, int... ids) {
        for (int id : ids) {
            boolean found = false;
            for (IngridHit hit : hits) {
                if (Integer.valueOf( (String)hit.get( IndexImpl.ELASTIC_SEARCH_ID ) ) == id) {
                    found = true;
                    break;
                }
            }
            assertThat("The following ID was not found in the results: " + id, found, is(true));
        }        
    }
    
    public static void addDefaultFacets(IngridQuery ingridQuery) {
        Map<String, String> f1 = new HashMap<String, String>();
        f1.put("id", "partner");

        Map<String, Object> f2 = new HashMap<String, Object>();
        f2.put("id", "after");
        Map<String, String> classes = new HashMap<String, String>();
        classes.put("id", "April2014");
        classes.put("query", "t1:2014-05-01 t2:2014-09-01");
        f2.put("classes", Arrays.asList(new Object[] { classes }));

        Map<String, Object> f3 = new HashMap<String, Object>();
        f3.put("id", "datatype");
        Map<String, String> classes2 = new HashMap<String, String>();
        classes2.put("id", "bundPDFs");
        classes2.put("query", "partner:bund datatype:pdf");
        f3.put("classes", Arrays.asList(new Object[] { classes2 }));

        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1, f2, f3 }));
    }
}
