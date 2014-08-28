package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;

@Service
public class MatchAllQueryConverter implements IQueryConverter {
    
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        // NOTICE: is also called on sub clauses BUT WE ONLY PROCESS THE TOP INGRID QUERY.
        // all other ones are subclasses !
        //
        boolean isTopQuery = (ingridQuery.getClass().equals(IngridQuery.class));
        boolean hasTerms = ingridQuery.getTerms().length > 0;
        if (!hasTerms && isTopQuery && !queryBuilder.hasClauses()) {
            BoolQueryBuilder bq = QueryBuilders.boolQuery();
            bq.must( QueryBuilders.matchAllQuery() );
            queryBuilder.must( bq );
        }
    }

}
