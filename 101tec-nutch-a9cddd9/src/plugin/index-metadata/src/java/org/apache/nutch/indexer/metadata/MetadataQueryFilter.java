package org.apache.nutch.indexer.metadata;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.searcher.FieldQueryFilter;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.RawFieldQueryFilter;

public class MetadataQueryFilter implements QueryFilter {

  public static class TokenizedQueryFilter extends FieldQueryFilter {
    protected TokenizedQueryFilter(String field) {
      super(field);
    }
  }

  public static class UnTokenizedQueryFilter extends RawFieldQueryFilter {

    private Configuration _conf;

    protected UnTokenizedQueryFilter(String field) {
      super(field);
    }

    @Override
    public Configuration getConf() {
      return _conf;
    }

    @Override
    public void setConf(Configuration conf) {
      _conf = conf;
    }

  }

  private Configuration _conf;

  private List<QueryFilter> _queryFilterChain = new ArrayList<QueryFilter>();

  @Override
  public BooleanQuery filter(Query input, BooleanQuery out)
          throws QueryException {
    for (QueryFilter queryFilter : _queryFilterChain) {
      queryFilter.filter(input, out);
    }
    return out;
  }

  @Override
  public Configuration getConf() {
    return _conf;
  }

  @Override
  public void setConf(Configuration conf) {
    PluginRepository pluginRepository = PluginRepository.get(conf);
    PluginDescriptor pluginDescriptor = pluginRepository
            .getPluginDescriptor("index-metadata");

    Extension[] extensions = pluginDescriptor.getExtensions();
    for (Extension extension : extensions) {
      if (MetadataQueryFilter.class.getSimpleName().equals(extension.getId())) {
        // add raw fields query filter
        String rawFields = extension.getAttribute("raw-fields");
        if (rawFields != null) {
          String[] splits = rawFields.split(",");
          for (String split : splits) {
            _queryFilterChain.add(new UnTokenizedQueryFilter(split.trim()));
          }
        }
        // add fields query filter
        String fields = extension.getAttribute("fields");
        if (fields != null) {
          String[] splits = fields.split(",");
          for (String split : splits) {
            _queryFilterChain.add(new TokenizedQueryFilter(split.trim()));
          }
        }
      }
    }
    _conf = conf;
  }

}
