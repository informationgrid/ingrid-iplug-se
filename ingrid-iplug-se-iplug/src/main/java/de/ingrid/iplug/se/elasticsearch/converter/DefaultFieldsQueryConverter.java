package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

public class DefaultFieldsQueryConverter implements IQueryConverter {
    
    private static final String[] content = {"title", "abstract"};

    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        TermQuery[] terms = ingridQuery.getTerms();

        BoolQueryBuilder bq = null;//QueryBuilders.boolQuery();
        
        if (terms.length > 0) {
            
            for (TermQuery term : terms) {
                QueryBuilder subQuery = QueryBuilders.multiMatchQuery( term.getTerm(), content );
                
                if (term.isRequred()) {
                    if (bq == null) bq = QueryBuilders.boolQuery();
                    bq.must( subQuery );
                    
                } else {
                    // if it's an OR-connection then the currently built query must become a sub-query
                    // so that the AND/OR connection is correctly transformed. In case there was an
                    // AND-connection before, the transformation would become:
                    // OR( (term1 AND term2), term3)
                    if (bq == null) {
                        bq = QueryBuilders.boolQuery();
                        bq.should( subQuery );
                        
                    } else {
                        BoolQueryBuilder parentBq = QueryBuilders.boolQuery();
                        parentBq.should( bq ).should( subQuery );
                        bq = parentBq;
                    }
                    
                }
            }
                
            queryBuilder.must( bq );
        }
    }

}
