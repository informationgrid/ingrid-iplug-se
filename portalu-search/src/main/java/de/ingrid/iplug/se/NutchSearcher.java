package de.ingrid.iplug.se;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.nutch.admin.searcher.MultipleSearcher;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Query.Clause;
import org.apache.nutch.searcher.Query.Term;
import org.apache.nutch.searcher.Query.Clause.NutchClause;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.iplug.se.searcher.DirectoryScanningSearcherFactory;
import de.ingrid.iplug.util.TimeProvider;
import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.processor.ProcessorPipe;
import de.ingrid.utils.processor.ProcessorPipeFactory;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.FuzzyFieldQuery;
import de.ingrid.utils.query.FuzzyTermQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.RangeQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.query.WildCardFieldQuery;
import de.ingrid.utils.query.WildCardTermQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * A nutch IPlug.
 */
public class NutchSearcher implements IPlug {

  private static final Log LOG = LogFactory.getLog(NutchSearcher.class);

  public static final String EXPLANATION = "explanation";

  private String fPlugId;

  private static Configuration _configuration;

  private ProcessorPipe _processorPipe = new ProcessorPipe();

  private DirectoryScanningSearcherFactory _searcherFactory;
  
  private SearchUpdateScanner _scanner;

  /**
   * The default constructor.
   */
  public NutchSearcher() {
    // nothing to do..
  }

  /**
   * JUST FOR TESTING...
   * 
   * @param indexFolder
   * @param plugId
   * @param conf
   * @throws IOException
   */
  public NutchSearcher(File indexFolder, String plugId, Configuration conf) throws IOException {
    _searcherFactory = new DirectoryScanningSearcherFactory(conf, new TimeProvider());
    this.fPlugId = plugId;
    _configuration = conf;
  }

  public void configure(PlugDescription plugDescription) throws Exception {
    this.fPlugId = plugDescription.getPlugId();
    if (_configuration == null) {
      _configuration = NutchConfiguration.create();
    }
    File workinDirectory = plugDescription.getWorkinDirectory();
    _configuration.set("nutch.instance.folder", workinDirectory.getAbsolutePath());
    _searcherFactory = new DirectoryScanningSearcherFactory(_configuration, new TimeProvider());
    ProcessorPipeFactory processorPipeFactory = new ProcessorPipeFactory(plugDescription);
    _processorPipe = processorPipeFactory.getProcessorPipe();
    if (_scanner != null) {
        _scanner.cancel();
    }
    _scanner = new SearchUpdateScanner(workinDirectory, _searcherFactory, 60000);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery,
   * int, int)
   */
  public IngridHits search(IngridQuery query, int start, int length) throws Exception {
    _processorPipe.preProcess(query);
    Query nutchQuery = new Query(_configuration);
    buildNutchQuery(query, nutchQuery);
    if (LOG.isDebugEnabled()) {
      LOG.debug("incoming nutch query: " + nutchQuery);
    }

    Hits hits = null;
    MultipleSearcher searcher = _searcherFactory.get();
    if (IngridQuery.DATE_RANKED.equalsIgnoreCase(query.getRankingType())) {
      hits = searcher.search(nutchQuery, start + length, null, "date", true);
    } else {
      hits = searcher.search(nutchQuery, start + length, "site", null, false);
    }

    if (hits == null) {
      LOG.warn("No searcher Beans are registered in MultipleSearcher. It seems that no index is available.");
      return new IngridHits();
    }

    int count = hits.getLength();
    int max = 0;
    final int countMinusStart = count - start;
    if (countMinusStart >= 0) {
      max = Math.min(length, countMinusStart);
    }

    IngridHits translateHits = translateHits(searcher, hits, start, max, query.getGrouped());
    IngridHit[] ingridHits = translateHits.getHits();
    _processorPipe.postProcess(query, ingridHits);

    return translateHits;
  }

  @SuppressWarnings("unused")
  private void printMemoryStatus() {
    Runtime runtime = Runtime.getRuntime();
    long freeMemory = runtime.freeMemory();
    long maxMemory = runtime.maxMemory();
    long reservedMemory = runtime.totalMemory();
    long used = reservedMemory - freeMemory;
    float percent = 100 * used / maxMemory;
    LOG.info("Memory: [" + (used / (1024 * 1024)) + " MB used of " + (maxMemory / (1024 * 1024)) + " MB total ("
        + percent + " %)" + "]");
  }

  @SuppressWarnings("unused")
  private void printNumberOfOpenFiles() {
    try {
      String property = System.getProperty("pid");
      Integer integer = new Integer(property);
      String[] cmd = { "lsof", "-p", "" + integer };
      Process proccess = Runtime.getRuntime().exec(cmd);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(proccess.getInputStream()));
      int lineCount = 0;
      while ((bufferedReader.readLine()) != null) {
        lineCount++;
      }
      LOG.info("Number of Open Files: " + lineCount);
      bufferedReader.close();
    } catch (Exception e) {
      LOG.error("can not parse process id: " + e.getMessage());
    }
  }

  /**
   * Translates the Nutch-Hits to IngridHits.
   * 
   * @param hits
   * @param start
   * @param length
   * @param groupBy
   * @return IngridHits The hits translated from nutch hits.
   * @throws IOException
   */
  private IngridHits translateHits(MultipleSearcher searcher, Hits hits, int start, int length, String groupBy)
      throws IOException {

    IngridHit[] ingridHits = new IngridHit[length];
    for (int i = start; i < (length + start); i++) {
      Hit hit = hits.getHit(i);
      // FIXME: Find the max value of the score.
      // final float normScore = normalize(score, 0, 50);
      // if (this.fLogger.isDebugEnabled()) {
      // this.fLogger.debug("The nutch score: " + score
      // + " and mormalized score: " + normScore);
      // }
      final int documentId = Integer.parseInt(hit.getUniqueKey());
      final int datasourceId = hit.getIndexNo();
      IngridHit ingridHit = null;
      WritableComparable<?> sortValue = hit.getSortValue();
      if (sortValue instanceof FloatWritable) {
        final float score = ((FloatWritable) hit.getSortValue()).get();
        ingridHit = new IngridHit(this.fPlugId, documentId, datasourceId, score);
      } else if (sortValue instanceof IntWritable) {
        final int date = ((IntWritable) hit.getSortValue()).get();
        ingridHit = new IngridHit(this.fPlugId, documentId, datasourceId, date);
      }

      String groupValue = null;
      HitDetails details = searcher.getDetails(hit);

      if (IngridQuery.GROUPED_BY_PARTNER.equalsIgnoreCase(groupBy)) {
        groupValue = details.getValue("partner");
      } else if (IngridQuery.GROUPED_BY_ORGANISATION.equalsIgnoreCase(groupBy)) {
        groupValue = details.getValue("provider");
      } else if (IngridQuery.GROUPED_BY_DATASOURCE.equalsIgnoreCase(groupBy)) {
        groupValue = details.getValue("url");
        try {
          groupValue = new URL(groupValue).getHost();
        } catch (MalformedURLException e) {
          LOG.warn("can not group url: " + groupValue, e);
        }
      }

      if (groupValue != null) {
        ingridHit.addGroupedField(groupValue);
      }

      ingridHits[i - start] = ingridHit;
    }

    IngridHits ret = new IngridHits(this.fPlugId, hits.getTotal(), ingridHits, true);
    return ret;
  }

  /**
   * builds a nutch query
   * 
   * @param query
   * @param out
   * @throws IOException
   */
  private void buildNutchQuery(IngridQuery query, Query out) throws IOException {
    // term queries
    TermQuery[] terms = query.getTerms();
    for (int i = 0; i < terms.length; i++) {
      TermQuery termQuery = terms[i];

      final boolean prohibited = termQuery.isProhibited();
      final boolean required = termQuery.isRequred();

      if (terms[i].getTerm().indexOf(" ") != -1) {
        termQuery = new TermQuery(required, prohibited, "\"" + termQuery.getTerm() + "\"");
      }
      if (prohibited) {
        out.addProhibitedTerm(filterTerm(termQuery.getTerm()));
      } else if (required) {
        out.addRequiredTerm(filterTerm(termQuery.getTerm()));
      } else {
        out.addNonRequiredTerm(filterTerm(termQuery.getTerm()));
      }
    }

    RangeQuery[] rangeQueries = query.getRangeQueries();
    for (int i = 0; i < rangeQueries.length; i++) {
      RangeQuery rangeQuery = rangeQueries[i];
      final boolean isProhibitet = rangeQuery.isProhibited();
      final boolean isRequired = rangeQuery.isRequred();
      String rangeName = rangeQuery.getRangeName();
      String from = rangeQuery.getRangeFrom();
      String to = rangeQuery.getRangeTo();
      // FIXME method filterTerm does not work with rangequeries like:
      // foo:[1 TO 2]?
      // filterTerm should only be used by term queries.
      if (isProhibitet) {
        out.addProhibitedTerm("[" + from + " TO " + to + "]", rangeName);
      } else if (isRequired) {
        out.addRequiredTerm("[" + from + " TO " + to + "]", rangeName);
      } else {
        out.addNonRequiredTerm("[" + from + " TO " + to + "]", rangeName);
      }
    }

    // subclauses
    ClauseQuery[] clauses = query.getClauses();
    NutchClause partnerClause = new NutchClause(true, false);
    NutchClause providerClause = new NutchClause(true, false);
    NutchClause datatypeClause = new NutchClause(true, false);

    for (int i = 0; i < clauses.length; i++) {
      ClauseQuery clauseQuery = clauses[i];
      final boolean prohibited = clauseQuery.isProhibited();
      final boolean required = clauseQuery.isRequred();
      NutchClause nutchClause = new NutchClause(required, prohibited);

      ClauseQuery[] subClauses = clauseQuery.getClauses();
      addSubClauses(subClauses, nutchClause);

      TermQuery[] termQueries = clauseQuery.getTerms();
      FieldQuery[] fieldQueries = clauseQuery.getFields();

      addQueriesToNutchClause(fieldQueries, nutchClause);
      addQueriesToNutchClause(termQueries, nutchClause);

      addToClause(partnerClause, getFields(clauseQuery, "partner"));
      addToClause(providerClause, getFields(clauseQuery, "provider"));
      addToClause(datatypeClause, getFields(clauseQuery, "datatype"));

      out.addNutchClause(nutchClause);
    }

    // wildcard fields
    WildCardFieldQuery[] wildCardQueries = query.getWildCardFieldQueries();
    for (int i = 0; i < wildCardQueries.length; i++) {
      WildCardFieldQuery wildCardQuery = wildCardQueries[i];
      String fieldName = wildCardQuery.getFieldName();
      String fieldValue = wildCardQuery.getFieldValue();
      boolean prohibited = wildCardQuery.isProhibited();
      boolean required = wildCardQuery.isRequred();
      if (prohibited) {
        out.addProhibitedTerm(fieldValue, fieldName);
      } else if (required) {
        out.addRequiredTerm(fieldValue, fieldName);
      } else {
        out.addNonRequiredTerm(fieldValue, fieldName);
      }
    }

    // wildcard Terms
    WildCardTermQuery[] wildCardTermQueries = query.getWildCardTermQueries();
    for (int i = 0; i < wildCardTermQueries.length; i++) {
      WildCardTermQuery wildCardTermQuery = wildCardTermQueries[i];
      if (wildCardTermQuery.isProhibited()) {
        out.addProhibitedTerm(filterTerm(wildCardTermQuery.getTerm()));
      } else if (wildCardTermQuery.isRequred()) {
        out.addRequiredTerm(filterTerm(wildCardTermQuery.getTerm()));
      } else {
        out.addNonRequiredTerm(filterTerm(wildCardTermQuery.getTerm()));
      }
    }

    // fuzzy fields
    FuzzyFieldQuery[] fuzzyFieldQueries = query.getFuzzyFieldQueries();
    for (int i = 0; i < fuzzyFieldQueries.length; i++) {
      FuzzyFieldQuery fuzzyFieldQuery = fuzzyFieldQueries[i];
      String fieldValue = filterTerm(fuzzyFieldQuery.getFieldValue()) + '~';
      if (fuzzyFieldQuery.isProhibited()) {
        out.addProhibitedTerm(fieldValue, fuzzyFieldQuery.getFieldName());
      } else if (fuzzyFieldQuery.isRequred()) {
        out.addRequiredTerm(fieldValue, fuzzyFieldQuery.getFieldName());
      } else {
        out.addNonRequiredTerm(fieldValue, fuzzyFieldQuery.getFieldName());
      }
    }

    // fuzzy terms
    FuzzyTermQuery[] fuzzyTermQueries = query.getFuzzyTermQueries();
    for (int i = 0; i < fuzzyTermQueries.length; i++) {
      FuzzyTermQuery fuzzyTermQuery = fuzzyTermQueries[i];
      String term = filterTerm(fuzzyTermQuery.getTerm()) + '~';
      if (fuzzyTermQuery.isProhibited()) {
        out.addProhibitedTerm(filterTerm(term));
      } else if (fuzzyTermQuery.isRequred()) {
        out.addRequiredTerm(term);
      } else {
        out.addNonRequiredTerm(filterTerm(term));
      }
    }

    // field queries
    addFielQueriesToNutchQuery(out, query.getFields());

    addToClause(partnerClause, getFields(query, "partner"));
    addToClause(providerClause, getFields(query, "provider"));
    addToClause(datatypeClause, getFields(query, "datatype"));
    addToQuery(out, partnerClause);
    addToQuery(out, providerClause);
    addToQuery(out, datatypeClause);
    }
    
    private void addToQuery(final Query query, final NutchClause nutchClause) {
        Clause[] clauses = nutchClause.getClauses();
        if (clauses != null) {
            for (int i = 0; i < clauses.length; i++) {
                // if there is one, that is not prohibited, add whole clause
                if (!clauses[i].isProhibited()) {
                    query.addNutchClause(nutchClause);
                    return;
                }
            }
            // otherwise add them seperately
            for (int i = 0; i < clauses.length; i++) {
                final Clause clause = clauses[i];
                NutchClause nutch = new NutchClause(clause.isRequired(), clause.isProhibited());
                nutch.addClause(new Clause(clause.getTerm(), clause.getField(), false, false, _configuration));
                query.addNutchClause(nutch);
            }
        }
    }

  /**
   * @param clause
   * @param fieldQueries
   * @param partnerClause
   * @param fields
   * @throws IOException
   */
  private void addToClause(NutchClause clause, FieldQuery[] fieldQueries) throws IOException {
    for (int i = 0; i < fieldQueries.length; i++) {
      FieldQuery fieldQuery = fieldQueries[i];
      boolean prohibited = fieldQuery.isProhibited();
      String fieldName = fieldQuery.getFieldName();
      Term term = new Query.Term(filterTerm(fieldQuery.getFieldValue()));
      clause.addClause(new Query.Clause(term, fieldName, false, prohibited, _configuration));
    }
  }

  private void addFielQueriesToNutchQuery(Query query, FieldQuery[] fieldQueries) throws IOException {
    for (int i = 0; i < fieldQueries.length; i++) {
      FieldQuery fieldQuery = fieldQueries[i];

      boolean prohibited = fieldQuery.isProhibited();
      boolean required = fieldQuery.isRequred();

      if (prohibited) {
        query.addProhibitedTerm(filterTerm(fieldQuery.getFieldValue()), fieldQuery.getFieldName());
      } else if (required) {
        query.addRequiredTerm(filterTerm(fieldQuery.getFieldValue()), fieldQuery.getFieldName());
      } else {
        query.addNonRequiredTerm(filterTerm(fieldQuery.getFieldValue()), fieldQuery.getFieldName());
      }
    }
  }

  /**
   * @param subClauses
   * @param nutchClause
   * @throws IOException
   */
  private void addSubClauses(ClauseQuery[] subClauses, NutchClause nutchClause) throws IOException {
    for (int i = 0; i < subClauses.length; i++) {
      ClauseQuery subClause = subClauses[i];
      boolean prohibited = subClause.isProhibited();
      boolean required = subClause.isRequred();
      NutchClause nextClause = new NutchClause(required, prohibited);
      addSubClauses(subClause.getClauses(), nextClause);

      TermQuery[] termQueries = subClause.getTerms();
      FieldQuery[] fieldQueries = subClause.getFields();

      addQueriesToNutchClause(fieldQueries, nextClause);
      addQueriesToNutchClause(termQueries, nextClause);
      addQueriesToNutchClause(getFields(subClause, "partner"), nutchClause);
      addQueriesToNutchClause(getFields(subClause, "provider"), nutchClause);
      addQueriesToNutchClause(getFields(subClause, "datatype"), nutchClause);
      nutchClause.addNutchClause(nextClause);
    }
  }

  @SuppressWarnings("unchecked")
  private static FieldQuery[] getFields(IngridQuery ingridQuery, String key) {
    ArrayList<FieldQuery> fields = ingridQuery.getArrayList(key);
    if (fields == null) {
      return new FieldQuery[0];
    }
    return fields.toArray(new FieldQuery[fields.size()]);
  }

  /**
   * @param fieldQueries
   * @param nutchClause
   * @throws IOException
   */
  private void addQueriesToNutchClause(FieldQuery[] fieldQueries, NutchClause nutchClause) throws IOException {
    for (int i = 0; i < fieldQueries.length; i++) {
      FieldQuery query = fieldQueries[i];

      String filteredFieldName = query.getFieldName().toLowerCase();
      String fieldValue = query.getFieldValue();
      Query.Clause clause = new Query.Clause(new Query.Term(fieldValue), filteredFieldName, query.isRequred(), query
          .isProhibited(), _configuration);
      nutchClause.addClause(clause);
    }
  }

  /**
   * @param termQueries
   * @param nutchClause
   */
  private void addQueriesToNutchClause(TermQuery[] termQueries, NutchClause nutchClause) {
    for (int i = 0; i < termQueries.length; i++) {
      TermQuery query = termQueries[i];
      String term = query.getTerm();
      Query.Clause clause = new Query.Clause(new Query.Term(term), query.isRequred(), query.isProhibited(),
          _configuration);
      nutchClause.addClause(clause);
    }
  }

  private String filterTerm(String term) throws IOException {
    return term.toLowerCase();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit)
   */
  public IngridHitDetail getDetail(IngridHit ingridHit, IngridQuery ingridQuery, String[] requestedFields)
      throws Exception {

    LOG.debug("creating details for: " + ingridHit.toString());
    // query required for summary calculation
    Query nutchQuery = new Query(_configuration);
    buildNutchQuery(ingridQuery, nutchQuery);
    // nutch hit detail
    Hit hit = new Hit(ingridHit.getDataSourceId(), "" + ingridHit.getDocumentId());
    HitDetails details = null;
    MultipleSearcher searcher = _searcherFactory.get();
    details = searcher.getDetails(hit);
    if (details != null) {

      String summary = null;
      String title = details.getValue("title");
      summary = searcher.getSummary(details, nutchQuery).toString();

      for (int i = 0; i < details.getLength(); i++) {
        String field = details.getField(i);

        if ("alt_summary".equals(field)) {
          summary = details.getValue(i).equals("no_alt_summary") ? "" : details.getValue(i);
        }

        if ("alt_title".equals(field)) {
          title = details.getValue(i);
        }
      }
      // push values into hit detail
      IngridHitDetail ingridDetail = new IngridHitDetail(ingridHit, title, summary);
      ingridDetail.put("url", details.getValue("url")); // TODO should

      int length = details.getLength();

      for (int j = 0; j < requestedFields.length; j++) {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 0; i < length; i++) {
          String luceneField = details.getField(i);
          if (luceneField.toLowerCase().equals(requestedFields[j].toLowerCase())
              && !luceneField.toLowerCase().equals("url") && !luceneField.toLowerCase().equals("title")) {
            arrayList.add(details.getValue(i));
          }
        }
        ingridDetail.put(requestedFields[j], (String[]) arrayList.toArray(new String[arrayList.size()]));

        if (requestedFields[j].equals(EXPLANATION)) {
          String detailString = details.toHtml();
          String hitExplanation = searcher.getExplanation(nutchQuery, hit);
          ingridDetail.put(EXPLANATION, detailString + " " + hitExplanation);
        }
      }
      
      // filter same multiple partner and provider from each detail
      filterPartnerAndProvider(ingridDetail);
      
      return ingridDetail;
    }
    return new IngridHitDetail();
  }


  /*
   * (non-Javadoc)
   * 
   * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit[],
   * de.ingrid.utils.query.IngridQuery, java.lang.String[])
   */
  public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query, String[] requestedFields) throws Exception {
    IngridHitDetail[] hitDetails = new IngridHitDetail[hits.length];
    for (int i = 0; i < hits.length; i++) {
      hitDetails[i] = getDetail(hits[i], query, requestedFields);
    }
    
    return hitDetails;
  }
  
  private void filterPartnerAndProvider(IngridHitDetail hitDetail) {
      String[] fields = {"partner", "provider"};
      for (String field : fields) {
          String[] values = (String[])hitDetail.get(field);
          if (values != null)
              hitDetail.put(field, removeMultipleValues(values));
      }
  }

  private String[] removeMultipleValues(String[] values) {
      List<String> uniqueValues = new ArrayList<String>();
      for (String value : values) {
          if (!uniqueValues.contains(value))
              uniqueValues.add(value);
      }
      return uniqueValues.toArray(new String[0]);
  }
    
  /**
   * @param args
   * @throws ParseException
   * @throws Exception
   */
  public static void main(String[] args) throws ParseException, Exception {

    String usage = "-d FolderToNutchIndex -q query";
    if (args.length < 4 || !args[0].startsWith("-d") || !args[2].startsWith("-q")) {
      System.err.println(usage);
      System.exit(-1);
    }
    File indexFolder = new File(args[1]);
    String query = args[3];
    PlugDescription plugDescription = new PlugDescription();
    plugDescription.setWorkinDirectory(indexFolder);
    NutchSearcher searcher = new NutchSearcher();
    searcher.configure(plugDescription);
    IngridHits hits = searcher.search(QueryStringParser.parse(query), 0, 10);
    System.out.println("Results: " + hits.length());
    System.out.println();
    IngridHit[] ingridHits = hits.getHits();
    for (int i = 0; i < ingridHits.length; i++) {
      IngridHit hit = ingridHits[i];
      System.out.println("hist: " + hit.toString());
      System.out.println("details:");
      System.out.println(searcher.getDetail(hit, QueryStringParser.parse(query), new String[0]).toString());
    }
    searcher.close();
  }

  public void close() throws IOException {
    if (_searcherFactory != null) {
      MultipleSearcher multipleSearcher = _searcherFactory.get();
      if (multipleSearcher != null) {
        multipleSearcher.close();
      }
    }
  }

}
