package de.ingrid.iplug.se.preprocessors;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class TopicsPreprocessor implements IPreProcessor {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void process(IngridQuery query) throws Exception {
        Map<String, String> facetMap = SEIPlug.conf.facetMap;
        Map<String, String> queryMap = SEIPlug.conf.queryFieldMap;
        
        List<Map<String, Object>> facets = (List<Map<String, Object>>) query.get( "FACETS" );
        // iterate over all facets
        for (Map<String, Object> facet : facets) {
            List<Map> classes = (List<Map>) facet.get( "classes" );
            // iterate over all facet classes
            if (classes != null) {
                for (Map clazz : classes) {
                    String value = facetMap.get( clazz.get( "id" ) );
                    // if we have a mapping for a specific id, then we exchange
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
        
        FieldQuery[] fields = query.getFields();
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
