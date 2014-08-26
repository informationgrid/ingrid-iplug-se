package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

public class FieldQueryConverter implements IQueryConverter {
    
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        FieldQuery[] fields = ingridQuery.getFields();

        BoolQueryBuilder bq = null;
        
        for (FieldQuery fieldQuery : fields) {
            
            QueryBuilder subQuery = QueryBuilders.matchQuery( fieldQuery.getFieldName(), fieldQuery.getFieldValue() );
            
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
                    parentBq.should( bq ).should( subQuery );
                    bq = parentBq;
                }
                
            }
            queryBuilder.must( bq );
        
        }
    }

}
