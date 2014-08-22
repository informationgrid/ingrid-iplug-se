package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

public class TitleQueryConverter implements IQueryConverter {
    
    private static final String content = "title";

    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        TermQuery[] terms = ingridQuery.getTerms();

        BoolQueryBuilder bq = QueryBuilders.boolQuery();
        
        if (terms.length > 0) {
            
            for (TermQuery term : terms) {
                if (term.isRequred()) {
                    bq.must( QueryBuilders.matchQuery( content, term.getTerm() ) );
                    
                } else {
                    bq.should( QueryBuilders.matchQuery( content, term.getTerm() ) );
                    
                }
            }
                
            queryBuilder.must( bq );
        }
    }

}
