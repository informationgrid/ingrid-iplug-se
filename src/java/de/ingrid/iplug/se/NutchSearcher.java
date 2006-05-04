package de.ingrid.iplug.se;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Query.NutchClause;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.RangeQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.query.WildCardFieldQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * A nutch IPlug.
 */
public class NutchSearcher implements IPlug {

	public static final Object mutex = new Object();

	private static final String DATATYPE = "datatype";

	private Log fLogger = LogFactory.getLog(this.getClass());

	private static NutchBean fNutchBean;

	private String fPlugId;

	private static Configuration fNutchConf;

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
	public NutchSearcher(File indexFolder, String plugId, Configuration conf)
			throws IOException {
		this.fNutchBean = new NutchBean(conf, indexFolder);
		this.fPlugId = plugId;
		this.fNutchConf = conf;
    
	}

	public void configure(PlugDescription plugDescription) throws Exception {
		this.fPlugId = plugDescription.getPlugId();
		synchronized (mutex) {
			if (fNutchConf == null) {
				this.fNutchConf = NutchConfiguration.create();
			}
			if (fNutchBean == null) {
				this.fNutchBean = new NutchBean(this.fNutchConf,
						plugDescription.getWorkinDirectory());
			} else {
				this.fNutchBean.close();
				this.fNutchBean = new NutchBean(this.fNutchConf,
						plugDescription.getWorkinDirectory());
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery,
	 *      int, int)
	 */
	public IngridHits search(IngridQuery query, int start, final int length)
			throws Exception {
		if (fLogger.isDebugEnabled()) {
			fLogger.debug("incomming query: " + query.toString() + " start:" + start + " length:" + length);
		}
		Query nutchQuery = new Query(this.fNutchConf);
		buildNutchQuery(query, nutchQuery);
		if (fLogger.isDebugEnabled()) {
			fLogger.debug("nutch query: " + nutchQuery.toString());
		}

    Hits hits = null;
    if (IngridQuery.DATE_RANKED.equalsIgnoreCase(query.getRankingType())) {
      hits = this.fNutchBean.search(nutchQuery, start + length, null,
          "date", false);
    } else {
      hits = this.fNutchBean.search(nutchQuery, start + length);
    }
		int count = hits.getLength();
		int max = 0;
		final int countMinusStart = count - start;
		if (countMinusStart >= 0) {
			max = Math.min(length, countMinusStart);
		}

    boolean groupByPartner = IngridQuery.GROUPED_BY_PARTNER.equalsIgnoreCase(query.getGrouped()) ? true : false;
		return translateHits(hits, start, max, groupByPartner);
	}

	/**
	 * Translates the Nutch-Hits to IngridHits.
	 * 
	 * @param hits
	 * @param start
	 * @param length
	 * @param groupByPartner 
	 * @return IngridHits The hits translated from nutch hits.
	 * @throws IOException 
	 */
	private IngridHits translateHits(Hits hits, int start, int length, boolean groupByPartner) throws IOException {
		
    IngridHit[] ingridHits = new IngridHit[length];
		for (int i = start; i < (length + start); i++) {
			Hit hit = hits.getHit(i);
			// FIXME: Find the max value of the score.
      // final float normScore = normalize(score, 0, 50);
      // if (this.fLogger.isDebugEnabled()) {
      // this.fLogger.debug("The nutch score: " + score
      // + " and mormalized score: " + normScore);
      // }
      final int documentId = hit.getIndexDocNo();
      final int datasourceId = hit.getIndexNo();
      IngridHit ingridHit = null;
      WritableComparable sortValue = hit.getSortValue();
      if(sortValue instanceof FloatWritable) {
        final float score = ((FloatWritable) hit.getSortValue()).get();
        ingridHit = new IngridHit(this.fPlugId, documentId,
            datasourceId, score);
      } else if(sortValue instanceof IntWritable) {
        final int date = ((IntWritable) hit.getSortValue()).get();
        ingridHit = new IngridHit(this.fPlugId, documentId,
            datasourceId, date);
      }
      
      
      if(groupByPartner) {
          HitDetails details = this.fNutchBean.getDetails(hit);
          String partner = details.getValue("partner");
          if(partner != null) {
            ingridHit.addGroupedField(partner);  
          }
      }
      
			ingridHits[i - start] = ingridHit;
		}

		return new IngridHits(this.fPlugId, hits.getTotal(), ingridHits, true);
	}

	/**
	 * builds a nutch query
	 * 
	 * @param query
	 * @param out
	 * @throws IOException
	 */
	private void buildNutchQuery(IngridQuery query, Query out) throws IOException {

        // first we add the datatype in case there is one setted
        FieldQuery[] dataTypes = query.getDataTypes();
        int count = dataTypes.length;
        for (int i = 0; i < count; i++) {
            FieldQuery dataType = dataTypes[i];
            if ("default".equals(dataType.getFieldValue()) || "www".equals(dataType.getFieldValue())) {
                continue;
            }
            boolean required = dataType.isRequred();
            boolean prohibited = dataType.isProhibited();
            if (required) {
                out.addRequiredTerm(dataType.getFieldValue(), DATATYPE);
            } else if (prohibited) {
                out.addProhibitedTerm(dataType.getFieldValue(), DATATYPE);
            } else if (!required) {
                out.addNonRequiredTerm(dataType.getFieldValue(), DATATYPE);
            }
        }

        // term queries
        TermQuery[] terms = query.getTerms();
        for (int i = 0; i < terms.length; i++) {
            TermQuery termQuery = terms[i];
            boolean prohibited = termQuery.isProhibited();
            boolean required = termQuery.isRequred();
            // boolean optional = termQuery.getOperation() == IngridQuery.OR;
            if (required) {
                out.addRequiredTerm(filterTerm(termQuery.getTerm()));
            } else if (prohibited) {
                out.addProhibitedTerm(filterTerm(termQuery.getTerm()));
            } else if (!required) {
                out.addNonRequiredTerm(filterTerm(termQuery.getTerm()));
            }

        }
        // field queries
        FieldQuery[] fields = query.getFields();
        for (int i = 0; i < fields.length; i++) {
            FieldQuery fieldQuery = fields[i];
            boolean prohibited = fieldQuery.isProhibited();
            boolean required = fieldQuery.isRequred();

            if (required) {
                out.addRequiredTerm(filterTerm(fieldQuery.getFieldValue()), fieldQuery.getFieldName());
            } else if (prohibited) {
                out.addProhibitedTerm(filterTerm(fieldQuery.getFieldValue()), fieldQuery.getFieldName());
            } else if (!required) {
                out.addNonRequiredTerm(filterTerm(fieldQuery.getFieldValue()), fieldQuery.getFieldName());
            }
        }

        RangeQuery[] rangeQueries = query.getRangeQueries();
        for (int i = 0; i < rangeQueries.length; i++) {
            RangeQuery rangeQuery = rangeQueries[i];
            boolean isProhibitet = rangeQuery.isProhibited();
            boolean isRequired = rangeQuery.isRequred();
            String rangeName = rangeQuery.getRangeName();
            String from = rangeQuery.getRangeFrom();
            String to = rangeQuery.getRangeTo();
            // FIXME? method filterTerm does not work with rangequery like:
            // foo:[1 TO 2]
            if (isRequired) {
                out.addRequiredTerm("[" + from + " TO " + to + "]", rangeName);
            } else if (isProhibitet) {
                out.addProhibitedTerm("[" + from + " TO " + to + "]", rangeName);
            } else if (!isRequired) {
                out.addNonRequiredTerm("[" + from + " TO " + to + "]", rangeName);
            }
        }

        // subclauses
        ClauseQuery[] clauses = query.getClauses();
        for (int i = 0; i < clauses.length; i++) {
            ClauseQuery clauseQuery = clauses[i];
            boolean prohibited = clauseQuery.isProhibited();
            boolean required = clauseQuery.isRequred();
            Query.NutchClause nutchClause = new Query.NutchClause(required, prohibited);

            ClauseQuery[] subClauses = clauseQuery.getClauses();
            addSubClauses(subClauses, nutchClause);

            TermQuery[] termQueries = clauseQuery.getTerms();
            FieldQuery[] fieldQueries = clauseQuery.getFields();

            addQueriesToNutchClause(fieldQueries, nutchClause);
            addQueriesToNutchClause(termQueries, nutchClause);
            out.addNutchClause(nutchClause);
        }
        
        
        WildCardFieldQuery[] wildCardQueries = query.getWildCardFieldQueries();
        for (int i = 0; i < wildCardQueries.length; i++) {
           WildCardFieldQuery wildCardQuery = wildCardQueries[i];
           String fieldName = wildCardQuery.getFieldName();
           String fieldValue = wildCardQuery.getFieldValue();
           boolean prohibited = wildCardQuery.isProhibited();
           boolean required = wildCardQuery.isRequred();
           if (required) {
             out.addRequiredTerm(fieldValue, fieldName);
           } else if (prohibited) {
             out.addProhibitedTerm(fieldValue, fieldName);
           } else if (!required) {
             out.addNonRequiredTerm(fieldValue, fieldName);
           }
        }
        
        // provider
        
        String[] providers = query.getPositiveProvider();
        for (int i = 0; i < providers.length; i++) {
            out.addRequiredTerm(filterTerm(providers[i]), "provider");
        }
        providers = query.getNegativeProvider();
        for (int i = 0; i < providers.length; i++) {
            out.addProhibitedTerm(filterTerm(providers[i]), "provider");
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
      Query.NutchClause nextClause = new Query.NutchClause(required, prohibited);
      addSubClauses(subClause.getClauses(), nextClause);
      

      TermQuery[] termQueries = subClause.getTerms();
      FieldQuery[] fieldQueries = subClause.getFields();
     
      addQueriesToNutchClause(fieldQueries, nextClause);
      addQueriesToNutchClause(termQueries, nextClause);
      nutchClause.addNutchClause(nextClause);
    }
    
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
      Query.Clause clause = new Query.Clause(new Query.Term(fieldValue),
          filteredFieldName, query.isRequred(), query.isProhibited(),
          this.fNutchConf);
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
      Query.Clause clause = new Query.Clause(new Query.Term(term),
          query.isRequred(), query.isProhibited(),
          this.fNutchConf);
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
	public IngridHitDetail getDetail(IngridHit ingridHit,
			IngridQuery ingridQuery, String[] requestedFields) throws Exception {
		fLogger.debug("creating details for: " + ingridHit.toString());
		// query required for summary caculation
		Query nutchQuery = new Query(this.fNutchConf);
		buildNutchQuery(ingridQuery, nutchQuery);
		// nutch hit detail
		Hit hit = new Hit(ingridHit.getDataSourceId(), ingridHit
				.getDocumentId());
		HitDetails details = null;
		synchronized (mutex) {
			details = this.fNutchBean.getDetails(hit);
		}
		if (details != null) {
      
      String summary=null;
			String title=details.getValue("title");
			synchronized (mutex) {
				summary = this.fNutchBean.getSummary(details, nutchQuery);
			}
      
      for (int i = 0; i < details.getLength(); i++) {
        String field = details.getField(i);
        
        if("alt_summary".equals(field)) {
          summary = details.getValue(i).equals("null") ? "" : details.getValue(i);
        }
        
        if("alt_title".equals(field)) {
          title = details.getValue(i);
        }
      }
			// push values into hit detail
			IngridHitDetail ingridDetail = new IngridHitDetail(ingridHit,
					title, summary);
			ingridDetail.put("url", details.getValue("url")); // TODO should

			int length = details.getLength();

			for (int j = 0; j < requestedFields.length; j++) {
				ArrayList arrayList = new ArrayList();
				for (int i = 0; i < length; i++) {
					String luceneField = details.getField(i);
					if (luceneField.toLowerCase().equals(
							requestedFields[j].toLowerCase())
							&& !luceneField.toLowerCase().equals("url")
							&& !luceneField.toLowerCase().equals("title")) {
						arrayList.add(details.getValue(i));
					}
				}
				ingridDetail.put(requestedFields[j], (String[]) arrayList
						.toArray(new String[arrayList.size()]));

			}

			return ingridDetail;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit[],
	 *      de.ingrid.utils.query.IngridQuery, java.lang.String[])
	 */
	public IngridHitDetail[] getDetails(IngridHit[] hits, IngridQuery query,
			String[] requestedFields) throws Exception {
		IngridHitDetail[] hitDetails = new IngridHitDetail[hits.length];
		for (int i = 0; i < hits.length; i++) {
			hitDetails[i] = getDetail(hits[i], query, requestedFields);
		}
		return hitDetails;
	}

	/**
	 * @param args
	 * @throws ParseException
	 * @throws Exception
	 */
	public static void main(String[] args) throws ParseException, Exception {

		String usage = "-d FolderToNutchIndex -q query";
		if (args.length < 4 || !args[0].startsWith("-d")
				|| !args[2].startsWith("-q")) {
			System.err.println(usage);
			System.exit(-1);
		}
		File indexFolder = new File(args[1]);
		String query = args[3];
		NutchSearcher searcher = new NutchSearcher(indexFolder, "aTestId",
				NutchConfiguration.create());
		IngridHits hits = searcher
				.search(QueryStringParser.parse(query), 0, 10);
		System.out.println("Results: " + hits.length());
		System.out.println();
		IngridHit[] ingridHits = hits.getHits();
		for (int i = 0; i < ingridHits.length; i++) {
			IngridHit hit = ingridHits[i];
			System.out.println("hist: " + hit.toString());
			System.out.println("details:");
			System.out.println(searcher.getDetail(hit,
					QueryStringParser.parse(query), new String[0]).toString());
		}
	}

	public void close() throws IOException {
		// FIXME normally we do not want to close the search at all.
		// synchronized (mutex) {
		// if (this.fNutchBean != null) {
		// this.fNutchBean.close();
		// }
		// this.fNutchBean = null;
		// }

	}

	protected void finalize() throws Throwable {
		if (this.fNutchBean != null) {
			this.fNutchBean.close();
		}
	}
}
