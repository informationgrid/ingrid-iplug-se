package de.ingrid.iplug.se.iplug;

import org.springframework.stereotype.Service;

import de.ingrid.utils.processor.IPreProcessor;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IPlugSEPreProcessor implements IPreProcessor {

    @Override
    public void process(IngridQuery query) throws Exception {
        for (FieldQuery field : query.getFields()) {
            if (field.getFieldName().equalsIgnoreCase("site")) {
                field.setFieldName("host");
            }
        }
        for (ClauseQuery cq : query.getClauses()) {
            process(cq);
        }
    }
}
