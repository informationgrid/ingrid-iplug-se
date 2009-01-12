package de.ingrid.iplug.se;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.FieldCache;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.IMetadataInjector;
import de.ingrid.utils.metadata.Metadata;

public class IndexMetadataInjector implements IMetadataInjector {

    private Logger LOG = Logger.getLogger(IndexMetadataInjector.class.getName());

    private File _workinDirectory;

    @Override
    public void injectMetaDatas(Metadata metadata) {
        SearchableIndexFinder finder = new SearchableIndexFinder();
        try {
            List<File> indices = finder.findIndices(FileSystem.get(new Configuration()), _workinDirectory);
            IndexReader[] readers = new IndexReader[indices.size()];
            for (int i = 0; i < indices.size(); i++) {
                readers[i] = IndexReader.open(indices.get(i));
            }
            MultiReader multiReader = new MultiReader(readers);
            String[] provider = FieldCache.DEFAULT.getStrings(multiReader, "provider");
            String[] partner = FieldCache.DEFAULT.getStrings(multiReader, "partner");
            multiReader.close();
            
            HashSet<String> partnerSet = new HashSet<String>(Arrays.asList(partner));
            HashSet<String> providerSet = new HashSet<String>(Arrays.asList(provider));
            partnerSet.remove(null);
            providerSet.remove(null);

            metadata.addMetadata("partner", partnerSet);
            metadata.addMetadata("provider", providerSet);
            
        } catch (IOException e) {
            LOG.warning(e.getMessage());
        }

    }

    @Override
    public void configure(PlugDescription plugDescription) {
        _workinDirectory = plugDescription.getWorkinDirectory();
    }

}
