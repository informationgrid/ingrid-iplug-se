/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.springframework.core.io.ClassPathResource;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.FacetConverter;
import de.ingrid.admin.elasticsearch.IQueryParsers;
import de.ingrid.admin.elasticsearch.IndexImpl;
import de.ingrid.admin.elasticsearch.converter.DatatypePartnerProviderQueryConverter;
import de.ingrid.admin.elasticsearch.converter.DefaultFieldsQueryConverter;
import de.ingrid.admin.elasticsearch.converter.FieldQueryIGCConverter;
import de.ingrid.admin.elasticsearch.converter.FuzzyQueryConverter;
import de.ingrid.admin.elasticsearch.converter.MatchAllQueryConverter;
import de.ingrid.admin.elasticsearch.converter.QueryConverter;
import de.ingrid.admin.elasticsearch.converter.WildcardQueryConverter;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

public class Utils {
    public static final long MAX_RESULTS = 11;
    
    public static IndexImpl index = null;

    public static ElasticsearchNodeFactoryBean elastic;

    public static void setupES() throws Exception {
        
        elastic = new ElasticsearchNodeFactoryBean();
        elastic.afterPropertiesSet();
        
        // set necessary configurations for startup
//        Configuration configuration = new Configuration();
//        configuration.searchType = SearchType.DFS_QUERY_THEN_FETCH;
//        configuration.index = "test";
//        configuration.activeInstances = Arrays.asList( "web" );
//        configuration.esBoostField = "boost";
//        configuration.esBoostModifier = Modifier.LOG1P;
//        configuration.esBoostFactor = 0.1f;
//        configuration.esBoostMode = "sum";
//        SEIPlug.conf = configuration;
        
        
        setMapping( elastic, "test_1" );
        prepareIndex( elastic );
    }
    
    public static void prepareIndex(ElasticsearchNodeFactoryBean elastic, String fileData, String index) throws ElasticsearchException, Exception {
        Client client = elastic.getObject().client();
        ClassPathResource resource = new ClassPathResource( fileData );

        byte[] urlsData = Files.readAllBytes( Paths.get( resource.getURI() ) );

        client.prepareBulk().add( urlsData, 0, urlsData.length, true )
                .execute()
                .actionGet();

        // make sure the indexed data is available immediately during search!
        RefreshRequest refreshRequest = new RefreshRequest( index );
        client.admin().indices().refresh( refreshRequest ).actionGet();
    }
    
    static void prepareIndex(ElasticsearchNodeFactoryBean elastic) throws Exception {
        prepareIndex( elastic, "data/webUrls.json", "test_1" );
    }

    public static void setMapping(ElasticsearchNodeFactoryBean elastic, String index) {
        String mappingSource = "";
        try {
            Client client = elastic.getObject().client();
            ClassPathResource resource = new ClassPathResource( "data/mapping.json" );

            List<String> urlsData = Files.readAllLines( Paths.get( resource.getURI() ), Charset.defaultCharset() );
            for (String line : urlsData) {
                mappingSource += line;
            }
            
            if (client.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
                client.admin().indices().prepareDelete(index).execute().actionGet();
            }
            client.admin().indices().prepareCreate(index).execute().actionGet();
            
            client.admin().indices().preparePutMapping().setIndices( index )
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
        //PowerMockito.mockStatic( JettyStarter.class );
        //Mockito.when(JettyStarter.getInstance()).thenReturn( jettyStarter );
        
//        Config config = new Config();
//        config.communicationProxyUrl = "/ingrid-group:iplug-se-test";
//        jettyStarter.config = config;
        
        QueryConverter qc = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<IQueryParsers>();
        parsers.add( new DefaultFieldsQueryConverter() );
        parsers.add( new WildcardQueryConverter() );
        parsers.add( new FuzzyQueryConverter() );
        parsers.add( new FieldQueryIGCConverter() );
        parsers.add( new DatatypePartnerProviderQueryConverter() );
        parsers.add( new MatchAllQueryConverter() );
        qc.setQueryParsers( parsers );
        
        index = new IndexImpl( elastic, qc, new FacetConverter(qc) );
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
