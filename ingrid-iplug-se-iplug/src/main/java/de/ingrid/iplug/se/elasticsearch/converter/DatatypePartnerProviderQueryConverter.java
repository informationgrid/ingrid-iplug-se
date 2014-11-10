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

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class DatatypePartnerProviderQueryConverter implements IQueryConverter {
    
    @SuppressWarnings("unchecked")
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        final List<FieldQuery> dataTypes = ingridQuery.getArrayList( IngridQuery.DATA_TYPE );
        final List<FieldQuery> partner = ingridQuery.getArrayList( IngridQuery.PARTNER );
        final List<FieldQuery> provider = ingridQuery.getArrayList( IngridQuery.PROVIDER );
        
        // concatenate all fields
        List<FieldQuery> allFields = new ArrayList<FieldQuery>();
        if (dataTypes != null) allFields.addAll( dataTypes );
        if (partner != null) allFields.addAll( partner );
        if (provider != null) allFields.addAll( provider );
        
        if (!allFields.isEmpty()) {
            BoolQueryBuilder bq = null;
            for (final FieldQuery fieldQuery : allFields) {
                final String field = fieldQuery.getFieldName();
                final String value = fieldQuery.getFieldValue().toLowerCase();
                TermQueryBuilder subQuery = QueryBuilders.termQuery( field, value );
                
                bq = ConverterUtils.applyAndOrRules( fieldQuery, bq, subQuery );
            }
            queryBuilder.must( bq );
        }
    }

}
