package de.ingrid.admin.object;

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

    public IndexSePlug() {
        // inject just the basic metadata, no partner, provider will be injected into indexer iplug!! 
        super(60000, new PlugDescriptionFieldFilters(), new IMetadataInjector[] { new ManifestMetadataInjector() }, null, null);
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
