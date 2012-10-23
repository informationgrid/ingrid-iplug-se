package de.ingrid.admin.object;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.PlugDescriptionFieldFilters;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.metadata.ManifestMetadataInjector;
import de.ingrid.utils.query.IngridQuery;

@Service
public class IndexSePlug extends HeartBeatPlug {

    private static Log log = LogFactory.getLog(IndexSePlug.class);

    private static final IngridHitDetail detail = new IngridHitDetail();

    public IndexSePlug() {
        // inject just the basic metadata, no partner, provider will be injected
        // into indexer iplug!!
        super(60000, new PlugDescriptionFieldFilters(), new IMetadataInjector[] { new ManifestMetadataInjector() },
                null, null);
    }

    @Override
    public IngridHits search(final IngridQuery query, final int start, final int length) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Search index iplug, return empty hits object.");
        }
        return new IngridHits(null, 0, new IngridHit[] {}, true);
    }

    @Override
    public IngridHitDetail getDetail(final IngridHit hit, final IngridQuery query, final String[] requestedFields)
            throws Exception {
        return detail;
    }

    @Override
    public IngridHitDetail[] getDetails(final IngridHit[] hits, final IngridQuery query, final String[] requestedFields)
            throws Exception {
        return new IngridHitDetail[] {};
    }
}
