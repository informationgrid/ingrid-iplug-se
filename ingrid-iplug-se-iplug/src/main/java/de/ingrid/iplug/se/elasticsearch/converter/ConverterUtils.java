package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import de.ingrid.utils.query.IngridQuery;

public class ConverterUtils {
    
    /**
     * Apply generic combination to the query depending on the settings (required, prohibited, optional).
     * 
     * @param fieldQuery is the incoming query used for analysis
     * @param bq is the boolean query that has been generated so far inside the current parser(!) and is used for combination with the subQuery 
     * @param subQuery is the newly generated subquery by a converter that has to be combined with bq
     * @return a combined query according to the AND, OR, NOT rules
     */
    public static BoolQueryBuilder applyAndOrRules(IngridQuery fieldQuery, BoolQueryBuilder bq, QueryBuilder subQuery) {
        if (fieldQuery.isRequred()) {
            if (bq == null) bq = QueryBuilders.boolQuery();
            if (fieldQuery.isProhibited()) {
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
                
                // if bq top type == "should" then add it, otherwise wrap it around
                // the type should be always at the same position!
                if (bq.toString().indexOf( "\"should\"" ) == 21) {
                    bq = bq.should( subQuery );
                
                } else {
                    parentBq.should( bq ).should( subQuery );
                    bq = parentBq;
                }
            }
        }

        return bq;
    }
}
