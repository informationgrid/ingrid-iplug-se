/**
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.searcher.basic;

import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.nutch.analysis.AnalyzerFactory;
import org.apache.nutch.analysis.CommonGrams;
import org.apache.nutch.analysis.NutchAnalyzer;
import org.apache.nutch.analysis.NutchDocumentAnalyzer;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.Query.Clause;
import org.apache.nutch.searcher.Query.Phrase;
import org.apache.nutch.searcher.Query.Term;
import org.apache.nutch.searcher.Query.Clause.NutchClause;

/**
 * The default query filter. Query terms in the default query field are expanded
 * to search the url, anchor and content document fields.
 */
public class BasicQueryFilter implements QueryFilter {

  private static final Logger LOG = Logger.getLogger(BasicQueryFilter.class.getName());
  
  private float URL_BOOST;

  private float ANCHOR_BOOST;

  private float TITLE_BOOST;

  private float HOST_BOOST;

  private static int SLOP = Integer.MAX_VALUE;

  private float PHRASE_BOOST;

  private static final String[] FIELDS = { "url", "anchor", "content", "title",
      "host" };

  private float[] FIELD_BOOSTS = { URL_BOOST, ANCHOR_BOOST, 1.0f,
      TITLE_BOOST, HOST_BOOST };
  
  private NutchAnalyzer analyzer = null;

  /**
   * Set the boost factor for url matches, relative to content and anchor
   * matches
   */
  public void setUrlBoost(float boost) {
    URL_BOOST = boost;
  }

  /**
   * Set the boost factor for title/anchor matches, relative to url and content
   * matches.
   */
  public void setAnchorBoost(float boost) {
    ANCHOR_BOOST = boost;
  }

  /**
   * Set the boost factor for sloppy phrase matches relative to unordered term
   * matches.
   */
  public void setPhraseBoost(float boost) {
    PHRASE_BOOST = boost;
  }

  /**
   * Set the maximum number of terms permitted between matching terms in a
   * sloppy phrase match.
   */
  public void setSlop(int slop) {
    SLOP = slop;
  }

  private Configuration conf;

  public BooleanQuery filter(Query input, BooleanQuery output) {

    BooleanQuery query = new BooleanQuery();
    // nutchclauses
    NutchClause[] nutchClauses = input.getNutchClauses();
    for (int i = 0; i < nutchClauses.length; i++) {
      NutchClause nutchClause = nutchClauses[i];
      BooleanQuery booleanQuery = new BooleanQuery();
      addNutchClause(nutchClause.getNutchClauses(), booleanQuery);
      Clause[] clauses = nutchClause.getClauses();
      addTerms(clauses, booleanQuery);
      addSloppyPhrases(clauses, booleanQuery);
      if (booleanQuery.getClauses().length > 0) {
        query.add(booleanQuery,
            (nutchClause.isProhibited() ? BooleanClause.Occur.MUST_NOT
                : (nutchClause.isRequired() ? BooleanClause.Occur.MUST
                    : BooleanClause.Occur.SHOULD)));
      }

    }

    
    addTerms(input, query);
    addSloppyPhrases(input, query);
    
    if(query.getClauses().length >0) {
      output.add(query, BooleanClause.Occur.MUST);
    }
    
    if (LOG.isLoggable(Level.FINE)) {
        LOG.fine("BasicQueryFilter.filter(): LuceneQuery: " + output.toString());
    }
    
    return output;
  }

  private void addTerms(Query input, BooleanQuery output) {
    Clause[] clauses = input.getClauses();
    for (int i = 0; i < clauses.length; i++) {
      Clause c = clauses[i];

      if (!c.getField().equals(Clause.DEFAULT_FIELD))
        continue; // skip non-default fields

      BooleanQuery out = new BooleanQuery();
      for (int f = 0; f < FIELDS.length; f++) {

        Clause o = c;
        if (c.isPhrase()) { // optimize phrase clauses
          String[] opt = new CommonGrams(getConf()).optimizePhrase(c
              .getPhrase(), FIELDS[f]);
          if (opt.length == 1) {
            o = new Clause(new Term(opt[0]), c.isRequired(), c.isProhibited(),
                getConf());
          } else {
            o = new Clause(new Phrase(opt), c.isRequired(), c.isProhibited(),
                getConf());
          }
        }

        out.add(o.isPhrase() ? exactPhrase(o.getPhrase(), FIELDS[f],
            FIELD_BOOSTS[f]) : termQuery(FIELDS[f], o.getTerm(),
            FIELD_BOOSTS[f]), BooleanClause.Occur.SHOULD);
      }
      output.add(out, (c.isProhibited() ? BooleanClause.Occur.MUST_NOT
          : (c.isRequired() ? BooleanClause.Occur.MUST
              : BooleanClause.Occur.SHOULD)));
    }
  }

  private void addSloppyPhrases(Query input, BooleanQuery output) {
    Clause[] clauses = input.getClauses();
    for (int f = 0; f < FIELDS.length; f++) {

      PhraseQuery sloppyPhrase = new PhraseQuery();
      sloppyPhrase.setBoost(FIELD_BOOSTS[f] * PHRASE_BOOST);
      sloppyPhrase
          .setSlop("anchor".equals(FIELDS[f]) ? NutchDocumentAnalyzer.INTER_ANCHOR_GAP
              : SLOP);
      int sloppyTerms = 0;

      for (int i = 0; i < clauses.length; i++) {
        Clause c = clauses[i];

        if (!c.getField().equals(Clause.DEFAULT_FIELD))
          continue; // skip non-default fields

        if (c.isPhrase()) // skip exact phrases
          continue;

        if (c.isProhibited()) // skip prohibited terms
          continue;

        sloppyPhrase.add(luceneTerm(FIELDS[f], c.getTerm()));
        sloppyTerms++;
      }

      if (sloppyTerms > 1)
        output.add(sloppyPhrase, BooleanClause.Occur.SHOULD);
    }
  }

  private void addTerms(Clause[] clauses, BooleanQuery output) {
    for (int i = 0; i < clauses.length; i++) {
      Clause c = clauses[i];

      if (!c.getField().equals(Clause.DEFAULT_FIELD))
        continue; // skip non-default fields

      BooleanQuery out = new BooleanQuery();
      for (int f = 0; f < FIELDS.length; f++) {

        Clause o = c;
        if (c.isPhrase()) { // optimize phrase clauses
          String[] opt = new CommonGrams(getConf()).optimizePhrase(c
              .getPhrase(), FIELDS[f]);
          if (opt.length == 1) {
            o = new Clause(new Term(opt[0]), c.isRequired(), c.isProhibited(),
                getConf());
          } else {
            o = new Clause(new Phrase(opt), c.isRequired(), c.isProhibited(),
                getConf());
          }
        }

        out.add(o.isPhrase() ? exactPhrase(o.getPhrase(), FIELDS[f],
            FIELD_BOOSTS[f]) : termQuery(FIELDS[f], o.getTerm(),
            FIELD_BOOSTS[f]), BooleanClause.Occur.SHOULD);
      }
      output.add(out, (c.isProhibited() ? BooleanClause.Occur.MUST_NOT
          : (c.isRequired() ? BooleanClause.Occur.MUST
              : BooleanClause.Occur.SHOULD)));
    }
  }

  private void addSloppyPhrases(Clause[] clauses, BooleanQuery output) {
    for (int f = 0; f < FIELDS.length; f++) {

      PhraseQuery sloppyPhrase = new PhraseQuery();
      sloppyPhrase.setBoost(FIELD_BOOSTS[f] * PHRASE_BOOST);
      sloppyPhrase
          .setSlop("anchor".equals(FIELDS[f]) ? NutchDocumentAnalyzer.INTER_ANCHOR_GAP
              : SLOP);
      int sloppyTerms = 0;

      for (int i = 0; i < clauses.length; i++) {
        Clause c = clauses[i];

        if (!c.getField().equals(Clause.DEFAULT_FIELD))
          continue; // skip non-default fields

        if (c.isPhrase()) // skip exact phrases
          continue;

        if (c.isProhibited()) // skip prohibited terms
          continue;

        sloppyPhrase.add(luceneTerm(FIELDS[f], c.getTerm()));
        sloppyTerms++;
      }

      if (sloppyTerms > 1)
        output.add(sloppyPhrase, BooleanClause.Occur.SHOULD);
    }
  }

  private org.apache.lucene.search.Query termQuery(String field, Term term,
      float boost) {
    org.apache.lucene.search.Query query = null;
    String termString = term.toString();
    QueryParser parser = new QueryParser(field, new NutchDocumentAnalyzer(this.conf));
    try {
      BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE/400);
      query = parser.parse(termString);
    } catch (ParseException e) {
      LOG.warning("can not parse term: " + termString);
      LOG.warning(e.getMessage());
    }
    if(query == null) {
      query = new TermQuery(luceneTerm(field, term));  
    }
    

    if ((query instanceof TermQuery) && "content".equals(field)) {
      String stemmedText = null;
      try {
        stemmedText = stemming(((TermQuery) query).getTerm().text());
        query = new TermQuery(new org.apache.lucene.index.Term(field,
            stemmedText));
      } catch (IOException e) {
        LOG.warning("can not stemm the query because: " + e.getMessage());
      }
    }
    query.setBoost(boost);
    return query;
  }

  /** Utility to construct a Lucene exact phrase query for a Nutch phrase. */
  private org.apache.lucene.search.Query exactPhrase(Phrase nutchPhrase,
      String field, float boost) {
    Query.Term[] terms = nutchPhrase.getTerms();
    PhraseQuery exactPhrase = new PhraseQuery();
    for (int i = 0; i < terms.length; i++) {
      exactPhrase.add(luceneTerm(field, terms[i]));
    }
    exactPhrase.setBoost(boost);
    return exactPhrase;
  }

  /** Utility to construct a Lucene Term given a Nutch query term and field. */
  private static org.apache.lucene.index.Term luceneTerm(String field, Query.Term term) {
    return new org.apache.lucene.index.Term(field, term.toString());
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
    this.URL_BOOST = conf.getFloat("query.url.boost", 4.0f);
    this.ANCHOR_BOOST = conf.getFloat("query.anchor.boost", 2.0f);
    this.TITLE_BOOST = conf.getFloat("query.title.boost", 1.5f);
    this.HOST_BOOST = conf.getFloat("query.host.boost", 2.0f);
    this.PHRASE_BOOST = conf.getFloat("query.phrase.boost", 1.0f);
    FIELD_BOOSTS = new float[]{ URL_BOOST, ANCHOR_BOOST, 1.0f,
        TITLE_BOOST, HOST_BOOST };
    analyzer = AnalyzerFactory.get(conf).get("de");
  }

  public Configuration getConf() {
    return this.conf;
  }

  /**
   * @param nutchClauses
   * @param booleanQuery
   */
  private void addNutchClause(NutchClause[] nutchClauses,
      BooleanQuery booleanQuery) {
    for (int i = 0; i < nutchClauses.length; i++) {
      BooleanQuery query = new BooleanQuery();
      NutchClause nutchClause = nutchClauses[i];
      addNutchClause(nutchClause.getNutchClauses(), query);
      Clause[] clauses = nutchClause.getClauses();
      addTerms(clauses, booleanQuery);
      addSloppyPhrases(clauses, booleanQuery);
      if (query.getClauses().length > 0) {
        booleanQuery.add(query,
            (nutchClause.isProhibited() ? BooleanClause.Occur.MUST_NOT
                : (nutchClause.isRequired() ? BooleanClause.Occur.MUST
                    : BooleanClause.Occur.SHOULD)));
      }
    }
  }
  
  /**
   * @param term
   * @return the stemmed term
   * @throws IOException
   */
  public String stemming(String term) throws IOException {
    String result = "";
    
    TokenStream ts = analyzer.tokenStream(null, new StringReader(term));
    Token token = ts.next();
    while (null != token) {
        result = result + " " + token.termText();
        token = ts.next();
    }

    return result.trim();
  }
}
