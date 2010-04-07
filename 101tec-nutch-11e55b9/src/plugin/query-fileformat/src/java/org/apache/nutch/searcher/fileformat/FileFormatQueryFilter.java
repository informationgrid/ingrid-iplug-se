package org.apache.nutch.searcher.fileformat;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryException;
import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.Query.Clause;

public class FileFormatQueryFilter implements QueryFilter {

  private static final String INDEX_FIELD_NAME = "subType";

  private static final String QUERY_FIELD_NAME = "fileformat";

  private Map<String, String> _fileformatMapping = new HashMap<String, String>();

  private Configuration _configuration;

  @Override
  public BooleanQuery filter(Query input, BooleanQuery output)
      throws QueryException {
    Clause[] clauses = input.getClauses();

    for (int i = 0; i < clauses.length; i++) {
      Clause c = clauses[i];

      if (!c.getField().equals(QUERY_FIELD_NAME))
        continue;

      String text = c.getTerm().toString();
      text = _fileformatMapping.get(text);
      Term term = new Term(INDEX_FIELD_NAME, text);
      TermQuery termQuery = new TermQuery(term);
      termQuery.setBoost(0.0f);

      output.add(termQuery, (c.isProhibited() ? BooleanClause.Occur.MUST_NOT
          : (c.isRequired() ? BooleanClause.Occur.MUST
              : BooleanClause.Occur.SHOULD)));

    }

    return output;

  }

  @Override
  public Configuration getConf() {
    return _configuration;
  }

  @Override
  public void setConf(Configuration configuration) {
    _configuration = configuration;
    _fileformatMapping.put("pdf", "pdf");
    _fileformatMapping.put("rtf", "rtf");
    _fileformatMapping.put("msword", "msword");
    _fileformatMapping.put("mspowerpoint", "vnd.ms-powerpoint");
    _fileformatMapping.put("msexcel", "vnd.ms-excel");
  }

}
