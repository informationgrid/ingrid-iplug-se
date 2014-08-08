package de.ingrid.iplug.se.elasticsearch;

import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.Index;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IndexImpl implements Index {
	
	private static Logger log = Logger.getLogger( IndexImpl.class );
	
    private ElasticsearchNodeFactoryBean elasticSearch;
	
	private Client client;
	
	@Autowired
	public IndexImpl(ElasticsearchNodeFactoryBean elasticSearch) {
		try {
			this.elasticSearch = elasticSearch;
			client = elasticSearch.getObject().client();
			
			log.info( "Elastic Search Settings: " + elasticSearch.getObject().settings().toDelimitedString( ',' ) );
			
		} catch (Exception e) {
			log.error( "Error during initialization of ElasticSearch-Client!" );
			e.printStackTrace();
		}
	}

	@Override
	public IngridHits search(IngridQuery query, int startHit, int num) {
		
		// get all active indices to search for

		// convert InGrid-query to QueryBuilder
		// see: http://www.elasticsearch.org/guide/en/elasticsearch/client/java-api/current/query-dsl-queries.html
		
		// add facets
		
		// search
		
		// convert to IngridHits
		

		// test search
//		SearchResponse searchResponse = client.prepareSearch("iplugse")
//		        //.setTypes("type1", "type2")
//		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
//		        .setQuery(QueryBuilders.termQuery("message", "out"))             // Query
//		        //.setPostFilter(FilterBuilders.rangeFilter("age").from(12).to(18))   // Filter
//		        .setFrom(0).setSize(60).setExplain(true)
//		        .execute()
//		        .actionGet();

		return null;
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
