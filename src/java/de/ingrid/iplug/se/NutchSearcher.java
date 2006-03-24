package de.ingrid.iplug.se;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.FloatWritable;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.utils.IPlug;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
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
			fLogger.debug("incomming query: " + query.toString());
		}
		Query nutchQuery = new Query(this.fNutchConf);
		buildNutchQuery(query, nutchQuery);
		System.out.println(nutchQuery.toString());
		if (fLogger.isDebugEnabled()) {
			fLogger.debug("nutch query: " + nutchQuery.toString());
		}

		Hits hits = this.fNutchBean.search(nutchQuery, start + length);

		int count = hits.getLength();
		int max = 0;
		final int countMinusStart = count - start;
		if (countMinusStart >= 0) {
			max = Math.min(length, countMinusStart);
		}

		return translateHits(hits, start, max);
	}

	/**
	 * Translates the Nutch-Hits to IngridHits.
	 * 
	 * @param hits
	 * @param start
	 * @param length
	 * @return IngridHits The hits translated from nutch hits.
	 */
	private IngridHits translateHits(Hits hits, int start, int length) {
		IngridHit[] ingridHits = new IngridHit[length];
		for (int i = start; i < (length + start); i++) {
			Hit hit = hits.getHit(i);
			final float score = ((FloatWritable) hit.getSortValue()).get();
			// FIXME: Find the max value of the score.
			// final float normScore = normalize(score, 0, 50);
			// if (this.fLogger.isDebugEnabled()) {
			// this.fLogger.debug("The nutch score: " + score
			// + " and mormalized score: " + normScore);
			// }
			final int documentId = hit.getIndexDocNo();
			final int datasourceId = hit.getIndexNo();

			IngridHit ingridHit = new IngridHit(this.fPlugId, documentId,
					datasourceId, score);
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
	private void buildNutchQuery(IngridQuery query, Query out)
			throws IOException {

		// FIXME handling DEFAUL datatype, handling exception datatype:default +
		// datatype:research
		// first we add the datatype in case there is one setted
		FieldQuery[] dataTypes = query.getDataTypes();
		int count = dataTypes.length;
		for (int i = 0; i < count; i++) {
			FieldQuery dataType = dataTypes[i];
			boolean required = dataType.isRequred();
			boolean prohibited = dataType.isProhibited();
			if (required) {
				out.addRequiredTerm(dataType.getFieldValue(), DATATYPE);
			} else if (prohibited) {
				out.addProhibitedTerm(dataType.getFieldValue(), DATATYPE);
			} else if (!required) {
				throw new UnsupportedOperationException(
						"'non required' actually not implemented, INGRID-455");
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
				throw new UnsupportedOperationException(
						"'non required' actually not implemented, INGRID-455");
			}

		}
		// field queries
		FieldQuery[] fields = query.getFields();
		for (int i = 0; i < fields.length; i++) {
			FieldQuery fieldQuery = fields[i];
			boolean prohibited = fieldQuery.isProhibited();
			boolean required = fieldQuery.isRequred();

			if (required) {
				out.addRequiredTerm(filterTerm(fieldQuery.getFieldValue()),
						fieldQuery.getFieldName());
			} else if (prohibited) {
				out.addProhibitedTerm(filterTerm(fieldQuery.getFieldValue()),
						fieldQuery.getFieldName());
			} else if (!required) {
				throw new UnsupportedOperationException(
						"'non required' actually not implemented, INGRID-455");
			}
		}

		// subclauses
		ClauseQuery[] clauses = query.getClauses();
		for (int i = 0; i < clauses.length; i++) {
			throw new UnsupportedOperationException(
					"'sub Clauses' actually not implemented, INGRID-455");
		}
	}

	private String filterTerm(String term) throws IOException {
		// FIXME DO WE REALLY NEED THIS`????
		return new SimpleAnalyzer().tokenStream(null, new StringReader(term))
				.next().termText();
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
			String title = details.getValue("title");
			String summary;
			synchronized (mutex) {
				summary = this.fNutchBean.getSummary(details, nutchQuery);
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
