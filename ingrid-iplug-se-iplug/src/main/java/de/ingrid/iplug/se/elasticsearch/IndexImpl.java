package de.ingrid.iplug.se.elasticsearch;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.se.Index;
import de.ingrid.iplug.se.IndexFields;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
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
    
    private final static String[] detailFields =  { "url", "title", "abstract" };
    
    // TODO: get all active indices to search for
    private String instances = "test";

    private String plugId = null;

    // SearchType see: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-search-type.html
    private SearchType searchType = null;

    @Autowired
    public IndexImpl(ElasticsearchNodeFactoryBean elasticSearch, QueryConverter qc) {
        try {
            this.elasticSearch = elasticSearch;
            this.queryConverter = qc;
            client = elasticSearch.getObject().client();

            log.info( "Elastic Search Settings: " + elasticSearch.getObject().settings().toDelimitedString( ',' ) );
            //log.info( "Elastic Port: " + elasticSearch.getObject().settings() );

        } catch (Exception e) {
            log.error( "Error during initialization of ElasticSearch-Client!" );
            e.printStackTrace();
        }
        
        this.searchType = SEIPlug.conf.searchType;
        this.plugId = JettyStarter.getInstance().config.communicationProxyUrl;
    }

    @Override
    public IngridHits search(IngridQuery ingridQuery, int startHit, int num) {

        // convert InGrid-query to QueryBuilder
        QueryBuilder query = queryConverter.convert( ingridQuery );

        boolean isLocationSearch = ingridQuery.containsField( "x1" );
        
        // search prepare
        SearchRequestBuilder srb = client.prepareSearch( instances )
                // .setTypes("type1", "type2")
                .setSearchType( searchType  )
                .setQuery( query ) // Query
                // .addAggregation( aggregation ) // Facets
                // .setPostFilter(FilterBuilders.rangeFilter("age").from(12).to(18))
                .setFrom( startHit ).setSize( num )
                .setExplain( true );
        
        // Filter for results only with location information
        if (isLocationSearch) {
            srb.setPostFilter( FilterBuilders.existsFilter( "x1" ) );
        }

        // TODO: add facets/aggregations

        // search!
        SearchResponse searchResponse = srb.execute().actionGet();

        // convert to IngridHits
        IngridHits hits = getHitsFromResponse( searchResponse );

        return hits;
    }

    private IngridHits getHitsFromResponse(SearchResponse searchResponse) {
        SearchHits hits = searchResponse.getHits();

        // the size will not be bigger than it was requested in the query with
        // 'num'
        // so we can convert from long to int here!
        int length = (int) hits.getHits().length;
        int totalHits = (int) hits.getTotalHits();
        IngridHit[] hitArray = new IngridHit[length];
        int pos = 0;
        for (SearchHit hit : hits.hits()) {
            int docId = Integer.valueOf( hit.getId() );
            hitArray[pos] = new IngridHit(this.plugId, docId, -1, hit.getScore() );
            pos++;
        }

        IngridHits ingridHits = new IngridHits( totalHits, hitArray );

        return ingridHits;
    }

    @Override
    public IngridHitDetail getDetail(IngridHit hit, String[] requestedFields) {
        String documentId = String.valueOf( hit.getDocumentId() );
        String[] allFields = (String[]) ArrayUtils.addAll( detailFields, requestedFields );
        
        GetResponse response = client.prepareGet( instances, "_all", documentId )
                .setFields( allFields )
                .execute()
                .actionGet();

        String title = (String) response.getField( IndexFields.TITLE ).getValue();
        String summary = (String) response.getField( IndexFields.ABSTRACT ).getValue();
        IngridHitDetail detail = new IngridHitDetail( hit.getPlugId(), hit.getDocumentId(), hit.getDataSourceId(), hit.getScore(), title, summary );
        if (requestedFields != null) {
            for (String field : requestedFields) {
                detail.put( field, response.getField( field ).getValue());
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

}
