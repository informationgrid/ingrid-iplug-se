package de.ingrid.iplug.se;

import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;

public class IndexIPlug implements IPlug {

    private static final IngridHitDetail detail = new IngridHitDetail();

    public void close() throws Exception {
    // nothing todo
    }

    public void configure(PlugDescription arg0) throws Exception {
    // nothing todo
    }

    public IngridHits search(IngridQuery arg0, int arg1, int arg2)
            throws Exception {
        return new IngridHits();
    }

    public IngridHitDetail getDetail(IngridHit arg0, IngridQuery arg1,
            String[] arg2) throws Exception {
        return detail;
    }

    public IngridHitDetail[] getDetails(IngridHit[] arg0, IngridQuery arg1,
            String[] arg2) throws Exception {
        return new IngridHitDetail[] {};
    }
}
