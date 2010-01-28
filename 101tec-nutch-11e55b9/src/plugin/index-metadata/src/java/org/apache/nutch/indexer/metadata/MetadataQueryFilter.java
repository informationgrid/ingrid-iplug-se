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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.nutch.searcher.Query.Clause;

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
  
  //------ none nutch-specific code starts here
  private List<String> _rawFields = new ArrayList<String>();
  //------ none nutch-specific code ends here

  @Override
  public BooleanQuery filter(Query input, BooleanQuery out)
          throws QueryException {
    for (QueryFilter queryFilter : _queryFilterChain) {
      queryFilter.filter(input, out);
    }
    // ------ none nutch-specific code starts here
    addNutchClauses(input.getNutchClauses(), out);
    //------ none nutch-specific code ends here
    return out;
  }
  //------ none nutch-specific code starts here
  private boolean addNutchClauses(Query.Clause.NutchClause[] clauses, BooleanQuery query) {
      boolean add = false;
      for (Query.Clause.NutchClause clause : clauses) {
          BooleanQuery newQuery = new BooleanQuery();
          if (addNutchClause(clause, newQuery)) {
              query.add(newQuery, getOccur(clause.isProhibited(), clause.isRequired()));
              add = true;
          }
      }
      return add;
  }
  
  private boolean addNutchClause(Query.Clause.NutchClause clause, BooleanQuery query) {
      boolean add = false;
      for (Clause c : clause.getClauses()) {
          String field = c.getField();
          if (_rawFields.contains(field)) {
              TermQuery term = new TermQuery(new Term(field, c.getTerm().toString()));
              query.add(term, getOccur(c.isProhibited(), c.isRequired()));
              add = true;
          }
      }
      return add || addNutchClauses(clause.getNutchClauses(), query);
  }
  
  private BooleanClause.Occur getOccur(boolean isProhibited, boolean isRequired) {
      return isProhibited ? BooleanClause.Occur.MUST_NOT : isRequired ?  BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD;
  }
  //------ none nutch-specific code ends here

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
            // ------ none nutch-specific code starts here
            _rawFields.add(split);
            //------ none nutch-specific code ends here
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
