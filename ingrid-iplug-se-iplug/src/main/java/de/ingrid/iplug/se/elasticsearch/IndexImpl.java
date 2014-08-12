package de.ingrid.iplug.se.elasticsearch;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.Index;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IndexImpl implements Index {

    private static Logger log = Logger.getLogger( IndexImpl.class );

    private ElasticsearchNodeFactoryBean elasticSearch;

    private Client client;

    private QueryConverter queryConverter;

    @Autowired
    public IndexImpl(ElasticsearchNodeFactoryBean elasticSearch, QueryConverter qc) {
        try {
            this.elasticSearch = elasticSearch;
            this.queryConverter = qc;
            client = elasticSearch.getObject().client();

            log.info( "Elastic Search Settings: " + elasticSearch.getObject().settings().toDelimitedString( ',' ) );

        } catch (Exception e) {
            log.error( "Error during initialization of ElasticSearch-Client!" );
            e.printStackTrace();
        }
    }

    @Override
    public IngridHits search(IngridQuery ingridQuery, int startHit, int num) {

        // TODO: get all active indices to search for
        String instances = "test";

        // convert InGrid-query to QueryBuilder
        // see:
        // http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/query-dsl-queries.html
        // QueryBuilder query = QueryBuilders.termQuery("message", "out");
        QueryBuilder query = queryConverter.convert( ingridQuery );

        // search prepare
        SearchRequestBuilder srb = client.prepareSearch( instances )
                // .setTypes("type1", "type2")
                .setSearchType( SearchType.DFS_QUERY_THEN_FETCH )
                .setQuery( query ) // Query
                // .addAggregation( aggregation ) // Facets
                // .setPostFilter(FilterBuilders.rangeFilter("age").from(12).to(18))
                // Filter
                .setFrom( startHit ).setSize( num ).setExplain( true );

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
        int length = (int) hits.totalHits();
        IngridHit[] hitArray = new IngridHit[length];

        IngridHits ingridHits = new IngridHits( length, hitArray );

        return ingridHits;
    }

    @Override
    public IngridHitDetail getDetail(IngridHit hit) {
        // TODO Auto-generated method stub
        return null;
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
