package de.ingrid.iplug.se.elasticsearch;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.se.Index;
import de.ingrid.iplug.se.IndexFields;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@Component
public class IndexImpl implements Index {

    private static Logger log = Logger.getLogger( IndexImpl.class );

    private ElasticsearchNodeFactoryBean elasticSearch;

    private Client client;

    private QueryConverter queryConverter;
    
    private FacetConverter facetConverter;
    
    private final static String[] detailFields =  { "url", "title" };

    private static final String ELASTIC_SEARCH_ID = "es_id";

    private static final String ELASTIC_SEARCH_INDEX = "es_index";

    private static final String ELASTIC_SEARCH_INDEX_TYPE = "es_type";
    
    // comma-separated list of instances used for search
    // -> set through configuration
    private String[] instances = null;

    private String plugId = null;

    // SearchType see: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-search-type.html
    private SearchType searchType = null;

    private String indexName;

    @Autowired
    public IndexImpl(ElasticsearchNodeFactoryBean elasticSearch, QueryConverter qc, FacetConverter fc) {
        this.indexName = SEIPlug.conf.index;
        this.searchType = SEIPlug.conf.searchType;
        this.plugId = JettyStarter.getInstance().config.communicationProxyUrl;
        
        try {
            this.elasticSearch = elasticSearch;
            this.queryConverter = qc;
            this.facetConverter = fc;
            client = elasticSearch.getObject().client();

            log.info( "Elastic Search Settings: " + elasticSearch.getObject().settings().toDelimitedString( ',' ) );
            boolean indexExists = client.admin().indices().prepareExists( indexName ).execute().actionGet().isExists();
            if (!indexExists) {
                client.admin().indices().prepareCreate( indexName ).execute().actionGet();
            }
            
            setActiveInstances( SEIPlug.conf.activeInstances );

        } catch (Exception e) {
            log.error( "Error during initialization of ElasticSearch-Client!" );
            e.printStackTrace();
        }
        
    }
    
    @Override
    public IngridHits search(IngridQuery ingridQuery, int startHit, int num) {

        // convert InGrid-query to QueryBuilder
        QueryBuilder query = queryConverter.convert( ingridQuery );
        
        if (log.isDebugEnabled()) {
            log.debug( "Elastic Search Query: \n" + query );
        }

        boolean isLocationSearch = ingridQuery.containsField( "x1" );
        boolean hasFacets = ingridQuery.containsKey( "FACETS" );
        
        // search prepare
        SearchRequestBuilder srb = client.prepareSearch( indexName )
                .setTypes( instances )
                .setSearchType( searchType  )
                .setQuery( query ) // Query
                .setFrom( startHit ).setSize( num )
                .setExplain( false )
                .setNoFields();
        
        // Filter for results only with location information
        if (isLocationSearch) {
            srb.setPostFilter( FilterBuilders.existsFilter( "x1" ) );
        }

        // pre-processing: add facets/aggregations to the query
        if (hasFacets) {
            List<AbstractAggregationBuilder> aggregations = facetConverter.getAggregations( ingridQuery, queryConverter );
            for (AbstractAggregationBuilder aggregation : aggregations) {
                srb.addAggregation( aggregation );
            }
        }

        // search!
        SearchResponse searchResponse = srb.execute().actionGet();

        
        // convert to IngridHits
        IngridHits hits = getHitsFromResponse( searchResponse, ingridQuery );
        
        // post-processing: extract and convert facets to InGrid-Document
        if (hasFacets) {
            // add facets from response
            IngridDocument facets = facetConverter.convertFacetResultsToDoc( searchResponse );
            hits.put( "FACETS", facets );
        }
        
        return hits;
    }

    private IngridHits getHitsFromResponse(SearchResponse searchResponse, IngridQuery ingridQuery) {
        SearchHits hits = searchResponse.getHits();

        // the size will not be bigger than it was requested in the query with
        // 'num'
        // so we can convert from long to int here!
        int length = (int) hits.getHits().length;
        int totalHits = (int) hits.getTotalHits();
        IngridHit[] hitArray = new IngridHit[length];
        int pos = 0;
        int docId = 0;
        for (SearchHit hit : hits.hits()) {
            IngridHit ingridHit = new IngridHit(this.plugId, docId++, -1, hit.getScore() );
            ingridHit.put( ELASTIC_SEARCH_ID, hit.getId() );
            ingridHit.put( ELASTIC_SEARCH_INDEX, hit.getIndex() );
            ingridHit.put( ELASTIC_SEARCH_INDEX_TYPE, hit.getType() );
            hitArray[pos] = ingridHit; 
            pos++;
        }

        IngridHits ingridHits = new IngridHits( totalHits, hitArray );
        
        return ingridHits;
    }

    @Override
    public IngridHitDetail getDetail(IngridHit hit, IngridQuery ingridQuery, String[] requestedFields) {
        String documentId = hit.getString( ELASTIC_SEARCH_ID );
        String fromIndex = hit.getString( ELASTIC_SEARCH_INDEX );
        String fromType = hit.getString( ELASTIC_SEARCH_INDEX_TYPE );
        String[] allFields = (String[]) ArrayUtils.addAll( detailFields, requestedFields );
        
     // convert InGrid-query to QueryBuilder
//        QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.idsQuery(documentId)).should(queryConverter.convert( ingridQuery ));
        QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("_id", documentId)).must(queryConverter.convert( ingridQuery ));
        
        
        // search prepare
        SearchRequestBuilder srb = client.prepareSearch( fromIndex )
                .setTypes( fromType )
                .setSearchType( searchType  )
                .setQuery( query ) // Query
                .setFrom( 0 ).setSize( 1 )
                .setExplain( false )
                .addHighlightedField("content")
                .addFields(allFields)
                .setSource("");

        SearchResponse searchResponse = srb.execute().actionGet();
        
        SearchHits dHits = searchResponse.getHits();
        SearchHit dHit = dHits.getAt(0);
        
/*        GetResponse response = client.prepareGet( fromIndex, fromType, documentId )
                .setFields( allFields )
                .execute()
                .actionGet();
*/
        String title = "untitled";
        if (dHit.field( IndexFields.TITLE ) != null) {
            title = (String) dHit.field( IndexFields.TITLE ).getValue();
        }
        String summary = "";
        if (dHit.getHighlightFields().containsKey("content")) {
            summary = StringUtils.join(dHit.getHighlightFields().get( "content" ).fragments(), " ... ");
        }
                //(String) response.getField( IndexFields.ABSTRACT ).getValue();
        IngridHitDetail detail = new IngridHitDetail( hit.getPlugId(), hit.getDocumentId(), hit.getDataSourceId(), hit.getScore(), title, summary );
        if (requestedFields != null) {
            for (String field : requestedFields) {
                detail.put( field, dHit.field( field ).getValue());
            }
        }
        return detail;
    }

    @Override
    public boolean deleteUrl(String url) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    // FIXME: is destroyed automatically via the BEAN!!!
    public void close() {
        try {
            elasticSearch.getObject().close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void setActiveInstances(List<String> values) {
        if (values == null) {
            this.instances = new String[0];
        } else {
            this.instances = values.toArray(new String[0]);
        }
    }

}
