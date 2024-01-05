/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.elasticsearch;

import de.ingrid.elasticsearch.*;
import de.ingrid.elasticsearch.search.FacetConverter;
import de.ingrid.elasticsearch.search.IQueryParsers;
import de.ingrid.elasticsearch.search.IndexImpl;
import de.ingrid.elasticsearch.search.converter.*;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;

public class Utils {
    public static final long MAX_RESULTS = 11;
    
    public static IndexImpl index = null;

    public static ElasticsearchNodeFactoryBean elastic;
    
    public static IndexManager indexManager;
    public static ElasticConfig elasticConfig;

    public static void setupES() throws Exception {

        Properties elasticProperties = getElasticProperties();

        elasticConfig = new ElasticConfig();
        elasticConfig.isEnabled = true;
        elasticConfig.communicationProxyUrl = "/ingrid-group:unit-tests";
        elasticConfig.indexSearchDefaultFields = new String[]{"title", "content"};
        elasticConfig.additionalSearchDetailFields = new String[0];
        elasticConfig.remoteHosts = new String[] { elasticProperties.get("network.host") + ":9300"};
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setToIndex("test_1");
        indexInfo.setToType("web");
        indexInfo.setToAlias("ingrid_test");
        elasticConfig.activeIndices = new IndexInfo[1];
        elasticConfig.activeIndices[0] = indexInfo;
        elasticConfig.esCommunicationThroughIBus = false;

        elastic = new ElasticsearchNodeFactoryBean();
        elastic.init(elasticConfig);
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

        SEIPlug.baseConfig.docProducerIndices = new String[] {"test"};
        
        // setMapping( elastic, "test_1","web" );
        prepareIndex( elastic );
    }

    public static Properties getElasticProperties() {
        Properties p = new Properties();
        try {
            // check for elastic search settings in classpath, which works
            // during development
            // and production
            Resource resource = new ClassPathResource("/elasticsearch.properties");
            if (resource.exists()) {
                p.load(resource.getInputStream());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return p;
    }

    public static void prepareIndex(ElasticsearchNodeFactoryBean elastic, String fileData, String index, String type) throws Exception {
        Client client = elastic.getClient();
        ClassPathResource resource = new ClassPathResource( fileData );

        setMapping(elastic, index, type);

        byte[] urlsData = Files.readAllBytes( Paths.get( resource.getURI() ) );

        client.prepareBulk().add( urlsData, 0, urlsData.length, XContentType.JSON )
                .execute()
                .actionGet();

        // make sure the indexed data is available immediately during search!
        RefreshRequest refreshRequest = new RefreshRequest( index );
        client.admin().indices().refresh( refreshRequest ).actionGet();
    }
    
    static void prepareIndex(ElasticsearchNodeFactoryBean elastic) throws Exception {
        prepareIndex( elastic, "data/webUrls.json", "test_1", "web" );
    }

    public static void setMapping(ElasticsearchNodeFactoryBean elastic, String index, String type) {
        String mappingSource = "";
        try {
            Client client = elastic.getClient();
            ClassPathResource resource = new ClassPathResource( "data/mapping.json" );

            List<String> urlsData = Files.readAllLines( Paths.get( resource.getURI() ), Charset.defaultCharset() );
            for (String line : urlsData) {
                mappingSource += line;
            }

            System.out.println(mappingSource);
            mappingSource = mappingSource.replace("\"web\"", "\"" + type + "\"");

            if (client.admin().indices().prepareExists(index).execute().actionGet().isExists()) {
                client.admin().indices().prepareDelete(index).execute().actionGet();
            }
            client.admin().indices().prepareCreate(index).execute().actionGet();
            
            client.admin().indices().preparePutMapping().setIndices( index )
                    .setType(type)
                    .setSource( mappingSource, XContentType.JSON )
                    .execute()
                    .actionGet();
            
            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void initIndex() {
        QueryConverter qc = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<>();
        parsers.add( new DefaultFieldsQueryConverter(elasticConfig) );
        parsers.add( new WildcardQueryConverter() );
        parsers.add( new FuzzyQueryConverter() );
        parsers.add( new FieldQueryIGCConverter() );
        parsers.add( new DatatypePartnerProviderQueryConverter() );
        parsers.add( new MatchAllQueryConverter() );
        qc.setQueryParsers( parsers );
        
        indexManager = new IndexManager( elastic, elasticConfig );
        indexManager.postConstruct();
        
        index = new IndexImpl( elasticConfig, indexManager, qc, new FacetConverter(qc), new QueryBuilderService());
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
                if (Integer.valueOf( hit.getDocumentId() ) == id) {
                    found = true;
                    break;
                }
            }
            assertThat("The following ID was not found in the results: " + id, found, is(true));
        }        
    }
    
    public static void addDefaultFacets(IngridQuery ingridQuery) {
        IngridDocument f1 = new IngridDocument();
        f1.put("id", "partner");

        IngridDocument f2 = new IngridDocument();
        f2.put("id", "after");
        Map<String, String> classes = new HashMap<>();
        classes.put("id", "April2014");
        classes.put("query", "t1:2014-05-01 t2:2014-09-01");
        f2.put("classes", Arrays.asList(new Object[] { classes }));

        IngridDocument f3 = new IngridDocument();
        f3.put("id", "datatype");
        Map<String, String> classes2 = new HashMap<>();
        classes2.put("id", "bundPDFs");
        classes2.put("query", "partner:bund datatype:pdf");
        f3.put("classes", Arrays.asList(new Object[] { classes2 }));

        ingridQuery.put("FACETS", Arrays.asList(new Object[] { f1, f2, f3 }));
    }
}
