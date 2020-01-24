/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2020 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.preprocessors;

import java.util.List;
import java.util.Map;

import de.ingrid.iplug.se.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class TopicsPreprocessor implements IPreProcessor {

    @Autowired
    private Configuration seConfig;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void process(IngridQuery query) throws Exception {
        Map<String, String> facetMap = seConfig.facetMap;
        Map<String, String> queryMap = seConfig.queryFieldMap;

        List<Map<String, Object>> facets = (List<Map<String, Object>>) query.get( "FACETS" );
        // iterate over all facets
        if (facets != null) {
            for (Map<String, Object> facet : facets) {
                List<Map> classes = (List<Map>) facet.get( "classes" );
                // iterate over all facet classes
                if (classes != null) {
                    for (Map clazz : classes) {
                        String value = facetMap.get( clazz.get( "id" ) );
                        // if we have a mapping for a specific id, then we
                        // exchange
                        // the query content
                        if (value != null) {
                            clazz.put( "query", value );
                        }
                    }
                } else {
                    String value = facetMap.get( facet.get( "id" ) );
                    if (value != null) {
                        facet.put( "query", value );
                    }
                }
            }
        }

        handleFields( query.getFields(), queryMap );
        
        ClauseQuery[] clauses = query.getClauses();
        for (ClauseQuery clause : clauses) {
            handleFields( clause.getFields(), queryMap );
        }
    }
    
    private void handleFields(FieldQuery[] fields, Map<String, String> queryMap) {
        for (FieldQuery field : fields) {
            String str = field.getFieldName() + ":" + field.getFieldValue();
            String value = queryMap.get( str );
            if (value != null) {
                String[] split = value.split( ":" );
                field.setFieldName( split[0] );
                field.setFieldValue( split[1] );
            }
        }
    }

}
