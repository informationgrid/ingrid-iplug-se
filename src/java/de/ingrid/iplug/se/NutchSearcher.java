package de.ingrid.iplug.se;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.nutch.io.FloatWritable;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.util.NutchConf;

import de.ingrid.iplug.IPlug;
import de.ingrid.iplug.PlugDescription;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHitDetail;
import de.ingrid.utils.IngridHits;
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

    private Log fLogger = LogFactory.getLog(this.getClass());

    private NutchBean fNutchBean;

    private String fPlugId;

    private NutchConf fNutchConf;

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
    public NutchSearcher(File indexFolder, String plugId, NutchConf conf)
            throws IOException {
        this.fNutchBean = new NutchBean(conf, indexFolder);
        this.fPlugId = plugId;
        this.fNutchConf = conf;
    }

    public void configure(PlugDescription plugDescription) throws Exception {
        this.fPlugId = plugDescription.getPlugId();
        this.fNutchConf = new NutchConf();
        this.fNutchBean = new NutchBean(this.fNutchConf, new File(
                plugDescription.getWorkinDirectory(), "nutch"));
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
            final float normScore = normalize(score, 0, 50);
//            if (this.fLogger.isDebugEnabled()) {
//                this.fLogger.debug("The nutch score: " + score
//                        + " and mormalized score: " + normScore);
//            }
            final int documentId = hit.getIndexDocNo();
            final int datasourceId = hit.getIndexNo();

            IngridHit ingridHit = new IngridHit(this.fPlugId, documentId,
                    datasourceId, normScore);
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
        // first we add the datatype in case there is one setted
        if (query.getDataType() != null) {
            out.addRequiredTerm(query.getDataType(), "datatype");
        }

        // term queries
        TermQuery[] terms = query.getTerms();
        for (int i = 0; i < terms.length; i++) {
            TermQuery termQuery = terms[i];
            boolean prohibited = termQuery.getOperation() == IngridQuery.NOT;
            boolean required = termQuery.getOperation() == IngridQuery.AND;
            boolean optional = termQuery.getOperation() == IngridQuery.OR;
            if (required) {
                out.addRequiredTerm(filterTerm(termQuery.getTerm()));
            } else if (prohibited) {
                out.addProhibitedTerm(filterTerm(termQuery.getTerm()));
            } else if (optional) {
                throw new UnsupportedOperationException(
                        "'non required' actually not implemented, INGRID-455");
            }

        }
        // field queries
        FieldQuery[] fields = query.getFields();
        for (int i = 0; i < fields.length; i++) {
            FieldQuery fieldQuery = fields[i];
            boolean prohibited = fieldQuery.getOperation() == IngridQuery.NOT;
            boolean required = fieldQuery.getOperation() == IngridQuery.AND;
            boolean optonal = fieldQuery.getOperation() == IngridQuery.OR;
            if (required) {
                out.addRequiredTerm(filterTerm(fieldQuery.getFieldValue()),
                        fieldQuery.getFieldName());
            } else if (prohibited) {
                out.addProhibitedTerm(filterTerm(fieldQuery.getFieldValue()),
                        fieldQuery.getFieldName());
            } else if (optonal) {
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
        return new StandardAnalyzer().tokenStream(null, new StringReader(term))
                .next().termText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit)
     */
    public IngridHitDetail getDetails(IngridHit ingridHit,
            IngridQuery ingridQuery) throws Exception {
        // query required for summary caculation
        Query nutchQuery = new Query(this.fNutchConf);
        buildNutchQuery(ingridQuery, nutchQuery);
        // nutch hit detail
        Hit hit = new Hit(ingridHit.getDataSourceId(), ingridHit
                .getDocumentId());
        HitDetails details = this.fNutchBean.getDetails(hit);
        String title = details.getValue("title");
        String summary = this.fNutchBean.getSummary(details, nutchQuery);
        // push values into hit detail
        IngridHitDetail document = new IngridHitDetail(ingridHit, title,
                summary);
        int fieldCount = details.getLength();
        for (int i = 0; i < fieldCount; i++) {
            String field = details.getField(i);
            document.put(field, details.getValue(field));
        }
        return document;
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
                new NutchConf());
        IngridHits hits = searcher
                .search(QueryStringParser.parse(query), 0, 10);
        System.out.println("Results: " + hits.length());
        System.out.println();
        IngridHit[] ingridHits = hits.getHits();
        for (int i = 0; i < ingridHits.length; i++) {
            IngridHit hit = ingridHits[i];
            System.out.println("hist: " + hit.toString());
            System.out.println("details:");
            System.out.println(searcher.getDetails(hit,
                    QueryStringParser.parse(query)).toString());
        }
    }

    private float normalize(float value, float min, float max) {
        // new_value = ((value - min_value) / (max_value - min_value)) *
        // (new_max_value - new_min_value) + new_min_value
        if (value > max) {
            value = max;
        }

        return ((value - min) / (max - min));
    }
}
