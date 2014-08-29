package de.ingrid.iplug.se.elasticsearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.InternalFilter;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.UnmappedTerms;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.elasticsearch.converter.QueryConverter;
import de.ingrid.search.utils.facet.FacetClassDefinition;
import de.ingrid.search.utils.facet.FacetClassProducer;
import de.ingrid.search.utils.facet.FacetClassRegistry;
import de.ingrid.search.utils.facet.FacetDefinition;
import de.ingrid.search.utils.facet.FacetUtils;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

@Service
public class FacetConverter {
    
    private FacetClassRegistry _facetClassRegistry;

    public FacetConverter() {
        _facetClassRegistry = new FacetClassRegistry();
        _facetClassRegistry.setFacetClassProducer( new FacetClassProducer() );
    }

    public List<AbstractAggregationBuilder> getAggregations(IngridQuery ingridQuery, QueryConverter queryConverter) {
        // get all FacetDefinitions from the Query
        List<FacetDefinition> defs = FacetUtils.getFacetDefinitions(ingridQuery);
        
        // TODO: filter facets!
        
        List<AbstractAggregationBuilder> aggregations = new ArrayList<AbstractAggregationBuilder>();
        
        for (FacetDefinition facetDefinition : defs) {
            String name = facetDefinition.getName();
            String field = facetDefinition.getField();
            List<FacetClassDefinition> classes = facetDefinition.getClasses();
            AbstractAggregationBuilder aggr = null;
            if (classes != null) {
                for (FacetClassDefinition fClass : classes) {
                    //aggr = AggregationBuilders.filter( fClass.getName() ).filter( FilterBuilders.queryFilter( QueryBuilders.queryString( fClass.getFragment() ) ) );
                    // TODO: try to convert query string into an InGrid-Query and transform it to a QueryBuilder as being done with the main query
                    IngridQuery facetQuery;
                    try {
                        facetQuery = QueryStringParser.parse( fClass.getFragment() );
                        aggr = AggregationBuilders.filter( fClass.getName() ).filter( FilterBuilders.queryFilter( queryConverter.convert( facetQuery ) ) );
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
            } else {
                aggr = AggregationBuilders.terms( name ).field( field );
            }
            aggregations.add( aggr );
        }
        
//        List<Map<String,Object>> facets = ingridQuery.getArrayList( "FACETS" );
        
//        for (Map<String,Object> facet : facets) {
//            String name = (String) facet.get( "id" );
//            
//        }
        
        
        
        return aggregations;
    }

    public IngridDocument convertFacetResultsToDoc(SearchResponse response) {
        IngridDocument facets = new IngridDocument();

        List<Aggregation> aggregations = response.getAggregations().asList();
        for (Aggregation aggregation : aggregations) {
            //facets.put( aggregation.getName(), -1l );
            Collection<Bucket> buckets = null;
            if ( aggregation.getClass() == UnmappedTerms.class ) {
                
            } else if ( aggregation.getClass() == StringTerms.class ) {
                StringTerms partnerAgg = (StringTerms) aggregation;
                for (Bucket bucket : partnerAgg.getBuckets()) {
                    facets.put(aggregation.getName() + ":" + bucket.getKey(), bucket.getDocCount() );
                }
            } else if ( aggregation.getClass() == InternalFilter.class ) {
                InternalFilter agg = (InternalFilter) aggregation;
                facets.put(aggregation.getName(), agg.getDocCount() );
                
            } else {
                throw new RuntimeException( "Aggregation Class not supported: " + aggregation.getClass() );
            }
        }
        
        
        return facets; 
    }

}
