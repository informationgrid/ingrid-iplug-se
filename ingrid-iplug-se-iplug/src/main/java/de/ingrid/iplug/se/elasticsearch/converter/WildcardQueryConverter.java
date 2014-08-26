package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.WildCardTermQuery;

@Service
public class WildcardQueryConverter implements IQueryConverter {
    
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        WildCardTermQuery[] terms = ingridQuery.getWildCardTermQueries();

        BoolQueryBuilder bq = null;
        
        if (terms.length > 0) {
            
            for (WildCardTermQuery term : terms) {
                QueryBuilder subQuery = QueryBuilders.queryString( term.getTerm() );
                
                if (term.isRequred()) {
                    if (bq == null) bq = QueryBuilders.boolQuery();
                    if (term.isProhibited()) {
                        bq.mustNot( subQuery );
                    } else {                        
                        bq.must( subQuery );
                    }
                    
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
