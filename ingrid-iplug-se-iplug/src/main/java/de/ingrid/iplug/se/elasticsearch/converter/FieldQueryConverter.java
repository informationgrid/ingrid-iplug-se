/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
