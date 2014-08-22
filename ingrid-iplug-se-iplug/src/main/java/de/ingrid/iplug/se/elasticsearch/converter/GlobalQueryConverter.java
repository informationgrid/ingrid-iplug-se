package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

public class GlobalQueryConverter implements IQueryConverter {

    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        TermQuery[] terms = ingridQuery.getTerms();

        BoolQueryBuilder bq = QueryBuilders.boolQuery();
        
        if (terms.length == 0) {
            bq.must( QueryBuilders.matchAllQuery() );
            
        } else {
            for (TermQuery term : terms) {
                if (term.isRequred()) {
                    bq.must( QueryBuilders.queryString( term.getTerm() ) );
                    
                } else {
                    bq.should( QueryBuilders.queryString( term.getTerm() ) );
                    
                }
            }
            
        }

        queryBuilder.must( bq );
        
    }

}
