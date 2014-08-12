package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

public class MatchAllQueryConverter implements IQueryConverter {
    
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        TermQuery[] terms = ingridQuery.getTerms();

        if (terms.length == 0) {
            BoolQueryBuilder bq = QueryBuilders.boolQuery();
            bq.must( QueryBuilders.matchAllQuery() );
            queryBuilder.must( bq );
        }
    }

}
