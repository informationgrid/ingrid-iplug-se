package de.ingrid.admin.object;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@Service
public class BasePlug extends HeartBeatPlug {

    public BasePlug() {
        super(60000);
    }

    @Override
    public IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {
        return null;
    }

    @Override
    public IngridHitDetail getDetail(final IngridHit hit, final IngridQuery query, final String[] requestedFields) throws Exception {
        return null;
    }

    @Override
    public IngridHitDetail[] getDetails(final IngridHit[] hits, final IngridQuery query, final String[] requestedFields) throws Exception {
        return null;
    }
}
