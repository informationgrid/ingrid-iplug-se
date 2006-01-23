package de.ingrid.iplug.se;

import java.io.File;
import java.io.IOException;

import org.apache.nutch.io.FloatWritable;
import org.apache.nutch.searcher.Hit;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.searcher.Hits;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;

import de.ingrid.iplug.IPlug;
import de.ingrid.iplug.PlugDescription;
import de.ingrid.utils.IDetailer;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

/**
 * A nutch Iplug
 * 
 */
public class NutchSearcher implements IPlug, IDetailer {

    private NutchBean fNutchBean;

    private String fProviderId;

    /**
     * default constructor...
     */
    public NutchSearcher() {
        // nothing to do..
    }

    /**
     * JUST FOR TESTING...
     * 
     * @param indexFolder
     * @param providerId
     * @throws IOException
     */
    public NutchSearcher(File indexFolder, String providerId) throws IOException {
        this.fNutchBean = new NutchBean(indexFolder);
        this.fProviderId = providerId;
    }

    public void configure(PlugDescription plugDescription) throws Exception {
        this.fNutchBean = new NutchBean(new File(plugDescription.getWorkinDirectory(), "nutch"));
        this.fProviderId = plugDescription.getIPlugClass() + plugDescription.getOrganisation();
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.ISearcher#search(de.ingrid.utils.query.IngridQuery, int, int)
     */
    public IngridHits search(IngridQuery query, int start, final int length) throws Exception {
        Query nutchQuery = new Query();
        buildNutchQuery(query, nutchQuery);
        Hits hits = this.fNutchBean.search(nutchQuery, start + length);

        int count = hits.getLength();
        int max = 0;
        final int countMinusStart = count - start;
        if (countMinusStart >= 0) {
            max = Math.min(length, countMinusStart);
        }
        String[] content = getSummary(hits, start, max, nutchQuery);

        return translateHits(hits, content, start, max);
    }

    /**
     * Translates the Nutch-Hits to IngridHits.
     * 
     * @param hits
     * @param content
     * @param length
     * @return IngridHits translated from nutch hits.
     */
    private IngridHits translateHits(Hits hits, String[] content, int start, int length) {
        IngridHit[] ingridHits = new IngridHit[length];
        for (int i = start; i < (length + start); i++) {
            Hit hit = hits.getHit(i);
            final float score = ((FloatWritable) hit.getSortValue()).get();
            final int documentId = hit.getIndexDocNo();
            final int datasourceId = hit.getIndexNo();

            IngridHit ingridHit = new IngridHit(this.fProviderId, documentId, datasourceId, score);
            ingridHit.put(IngridDocument.DOCUMENT_CONTENT, content[i - start]);
            ingridHits[i - start] = ingridHit;
        }

        return new IngridHits(this.fProviderId, hits.getTotal(), ingridHits);
    }

    private String[] getSummary(final Hits hits, final int start, final int length, Query nutchQuery)
            throws IOException {
        Hit[] hitArray = hits.getHits(start, length);
        HitDetails[] hitDetailsArray = this.fNutchBean.getDetails(hitArray);

        return this.fNutchBean.getSummary(hitDetailsArray, nutchQuery);
    }

    /**
     * builds a nutch query
     * 
     * @param query
     * @param out
     */
    private void buildNutchQuery(IngridQuery query, Query out) {
        // term queries
        TermQuery[] terms = query.getTerms();
        for (int i = 0; i < terms.length; i++) {
            TermQuery termQuery = terms[i];
            boolean prohibited = termQuery.getOperation() == IngridQuery.NOT;
            boolean required = termQuery.getOperation() == IngridQuery.AND;
            boolean optional = termQuery.getOperation() == IngridQuery.OR;
            if (required) {
                out.addRequiredTerm(termQuery.getTerm());
            } else if (prohibited) {
                out.addProhibitedTerm(termQuery.getTerm());
            } else if (optional) {
                throw new UnsupportedOperationException("'non required' actually not implemented, INGRID-455");
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
                out.addRequiredTerm(fieldQuery.getFieldValue(), fieldQuery.getFieldName());
            } else if (prohibited) {
                out.addProhibitedTerm(fieldQuery.getFieldValue(), fieldQuery.getFieldName());
            } else if (optonal) {
                throw new UnsupportedOperationException("'non required' actually not implemented, INGRID-455");
            }

        }

        // subclauses

        ClauseQuery[] clauses = query.getClauses();
        for (int i = 0; i < clauses.length; i++) {
            throw new UnsupportedOperationException("'sub Clauses' actually not implemented, INGRID-455");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see de.ingrid.utils.IDetailer#getDetails(de.ingrid.utils.IngridHit)
     */
    public IngridDocument getDetails(IngridHit ingridHit) throws Exception {
        Hit hit = new Hit(ingridHit.getDataSourceId(), ingridHit.getDocumentId());
        HitDetails details = this.fNutchBean.getDetails(hit);
        IngridDocument document = new IngridDocument();
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
        if (args.length < 4 || !args[0].startsWith("-d") || !args[2].startsWith("-q")) {
            System.err.println(usage);
            System.exit(-1);
        }
        File indexFolder = new File(args[1]);
        String query = args[3];
        NutchSearcher searcher = new NutchSearcher(indexFolder, "aTestId");
        IngridHits hits = searcher.search(QueryStringParser.parse(query), 0, 10);
        System.out.println("Results: " + hits.length());
        System.out.println();
        IngridHit[] ingridHits = hits.getHits();
        for (int i = 0; i < ingridHits.length; i++) {
            IngridHit hit = ingridHits[i];
            System.out.println("hist: " + hit.toString());
            System.out.println("details:");
            System.out.println(searcher.getDetails(hit).toString());
        }
    }
}
