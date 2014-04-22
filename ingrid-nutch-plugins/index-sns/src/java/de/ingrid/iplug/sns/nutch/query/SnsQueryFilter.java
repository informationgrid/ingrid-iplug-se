/**
 * 
 */
package de.ingrid.iplug.sns.nutch.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.Version;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRepository;

import de.ingrid.iplug.se.crawl.sns.DoublePadding;
import de.ingrid.iplug.sns.nutch.IPlugSNSPlugin;
import de.ingrid.nutch.analysis.NutchDocumentAnalyzer;
import de.ingrid.nutch.searcher.Query;
import de.ingrid.nutch.searcher.Query.Clause;
import de.ingrid.nutch.searcher.Query.Clause.NutchClause;
import de.ingrid.nutch.searcher.QueryException;
import de.ingrid.nutch.searcher.QueryFilter;

/**
 * @author mb
 */
public class SnsQueryFilter implements QueryFilter {

    private static final Logger LOGGER = Logger.getLogger(SnsQueryFilter.class.getName());

    /**
     * neither qx1 or qx2 are between x1 and x2
     */
    private static final int FIRST_X_CASE = 0;

    /**
     * qx1 and qx2 are between x1 and x2
     */
    private static final int SECOND_X_CASE = 1;

    /**
     * qx1 and qx2 are not between x1 and x2
     */
    private static final int THIRD_X_CASE = 2;

    private Configuration fConfiguration;

    private Map<String, Object> fTimeMap;

    private Map<String, Object> fGeoMap;

    private Set<String> fBuzzwordSet;

    private String fX1;

    private String fY1;

    private String fX2;

    private String fY2;

    private String fT0;

    private String fT1;

    private String fT2;

    private String fArea;

    private String fLocation;

    private String fTime;

    private String fCoord;

    private SimpleDateFormat fDateFormat;

    private String fX1Min;

    private String fX2Max;

    private String fY1Min;

    private String fY2Max;

    public SnsQueryFilter() {
        super();
    }

    @Override
    public BooleanQuery filter(Query input, BooleanQuery output) throws QueryException {

        boolean search = false;
        clearFields();
        // examine each clause in the Nutch query
        Clause[] clauses = input.getClauses();
        handleClauses(output, search, clauses);

        NutchClause[] nutchClauses = input.getNutchClauses();
        handleNutchClauses(output, nutchClauses);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("SnsQueryFilter.filter(): " + output.toString());
        }
        return output;
    }

    private void handleNutchClauses(BooleanQuery output, NutchClause[] nutchClauses) {
        clearFields();
        for (int i = 0; i < nutchClauses.length; i++) {
            BooleanQuery query = new BooleanQuery();
            NutchClause nutchClause = nutchClauses[i];

            Clause[] clauses2 = nutchClause.getClauses();
            handleClauses(query, false, clauses2);
            if (query.getClauses().length > 0) {
                output.add(query, nutchClause.isRequired() ? Occur.MUST : nutchClause.isProhibited() ? Occur.MUST_NOT
                        : Occur.SHOULD);
            }
            NutchClause[] nutchClauses2 = nutchClause.getNutchClauses();

            handleNutchClauses(query, nutchClauses2);

        }
    }

    private void handleClauses(BooleanQuery output, boolean search, Clause[] clauses) {
        for (int i = 0; i < clauses.length; i++) {
            Clause c = clauses[i];
            search = handleClause(output, search, c);
        }

        prepareTime(output);
        prepareGeo(output);
        if (search) {
            prepareBuzzwords(output);
        }
    }

    @SuppressWarnings("unchecked")
    private boolean handleClause(BooleanQuery output, boolean search, Clause c) {
        String indexField = c.getField();
        String value = c.getTerm().toString().toLowerCase();
        if (indexField.equals(this.fX1)) {
            this.fGeoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
        } else if (indexField.equals(this.fX2)) {
            this.fGeoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
        } else if (indexField.equals(this.fY1)) {
            this.fGeoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
        } else if (indexField.equals(this.fY2)) {
            this.fGeoMap.put(indexField, DoublePadding.padding(Double.parseDouble(value)));
        } else if (this.fCoord.equals(indexField)) {
            List<String> list = (List<String>) this.fGeoMap.get(indexField);
            if (list == null) {
                list = new LinkedList<String>();
            }
            list.add(value);
            this.fGeoMap.put(indexField, list);
        } else if (this.fArea.equals(indexField)) {
            addClause(output, c, indexField, value);
        } else if (this.fLocation.equals(indexField)) {
            addClause(output, c, indexField, value);
        } else if (this.fT0.equals(indexField)) {
            value = value.replaceAll("-", "");
            addClause(output, c, indexField, value);
        } else if (this.fT1.equals(indexField)) {
            this.fTimeMap.put(indexField, value);
        } else if (this.fT2.equals(indexField)) {
            this.fTimeMap.put(indexField, value);
        } else if (this.fTime.equals(indexField)) {
            // special case: time has two values: intersect, include
            List<String> list = (List<String>) this.fTimeMap.get(indexField);
            if (list == null) {
                list = new LinkedList<String>();
            }
            list.add(value);
            this.fTimeMap.put(indexField, list);
        } else if ("incl_meta".equals(indexField) && "on".equals(value)) {
            // search on sns meta datas
            search = true;
        } else if (indexField.equals("DEFAULT")) {
            this.fBuzzwordSet.add(value);
        }
        return search;
    }

    /**
     * @param query
     * 
     */
    private void prepareBuzzwords(BooleanQuery query) {
        BooleanClause[] clauses = query.getClauses();
        for (int i = 0; i < clauses.length; i++) {
            BooleanClause clause = clauses[i];
            org.apache.lucene.search.Query query2 = clause.getQuery();
            Set<Term> set = extractTerms(query2);
            addBuzzwordToBasicQuery(query2, set);
        }
    }

    /**
     * @param query2
     * @param set
     */
    private void addBuzzwordToBasicQuery(org.apache.lucene.search.Query query2, Set<Term> set) {
        BooleanQuery buzzwordTermQuery = new BooleanQuery();
        if (query2 instanceof BooleanQuery) {
            for (Term term : set) {
                if (term.field().equals("url")) {
                    for (String buzzword : fBuzzwordSet) {
                        org.apache.lucene.search.Query buzzwordQuery = new TermQuery(new Term("buzzword", buzzword));
                        ((BooleanQuery) buzzwordTermQuery).add(buzzwordQuery, Occur.MUST);
                    }
                    break;
                }
            }
            // add all buzzwords to the query as optional
            if (buzzwordTermQuery.getClauses().length != 0)
                ((BooleanQuery) query2).add(buzzwordTermQuery, Occur.SHOULD);
        }

    }

    /**
     * @param query2
     * @return
     */
    private Set<Term> extractTerms(org.apache.lucene.search.Query query2) {
        Set<Term> set = new HashSet<Term>();
        try {
            query2.extractTerms(set);
        } catch (UnsupportedOperationException e) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("query does not support the extraction of terms: " + query2);
            }
        }
        return set;
    }

    /**
     * 
     */
    private void clearFields() {
        this.fGeoMap.clear();
        this.fTimeMap.clear();
        this.fBuzzwordSet.clear();
    }

    /**
     * @param booleanQuery
     * 
     */
    @SuppressWarnings("unchecked")
    private void prepareGeo(BooleanQuery booleanQuery) {
        List<String> list = (List<String>) this.fGeoMap.get(this.fCoord);
        if (list != null) {
            for (String value : list) {
                if ("inside".equals(value)) {
                    // innerhalb
                    prepareInsideGeoQuery(booleanQuery);
                } else if ("intersect".equals(value)) {
                    // schneiden
                    prepareIntersectGeoQuery(booleanQuery);
                } else if ("include".equals(value)) {
                    // innerhalb
                    prepareIncludeGeoQuery(booleanQuery);
                }
            }
        }
    }

    /**
     * @param booleanQuery
     */
    private void prepareIncludeGeoQuery(BooleanQuery booleanQuery) {
        String x1 = (String) this.fGeoMap.get(this.fX1);
        String x2 = (String) this.fGeoMap.get(this.fX2);
        String y1 = (String) this.fGeoMap.get(this.fY1);
        String y2 = (String) this.fGeoMap.get(this.fY2);

        if (x1 != null && x2 != null && y1 != null && y2 != null) {

            TermRangeQuery xRangeQuery1 = new TermRangeQuery(this.fX1, this.fX1Min, x1, true, true);
            TermRangeQuery xRangeQuery2 = new TermRangeQuery(this.fX2, x2, this.fX2Max, true, true);
            TermRangeQuery yRangeQuery1 = new TermRangeQuery(this.fY1, this.fY1Min, y1, true, true);
            TermRangeQuery yRangeQuery2 = new TermRangeQuery(this.fY2, y2, this.fY2Max, true, true);

            booleanQuery.add(xRangeQuery1, BooleanClause.Occur.MUST);
            booleanQuery.add(xRangeQuery2, BooleanClause.Occur.MUST);
            booleanQuery.add(yRangeQuery1, BooleanClause.Occur.MUST);
            booleanQuery.add(yRangeQuery2, BooleanClause.Occur.MUST);

        }

    }

    /**
     * @param booleanQuery
     */
    private void prepareIntersectGeoQuery(BooleanQuery booleanQuery) {
        String x1 = (String) this.fGeoMap.get(this.fX1);
        String x2 = (String) this.fGeoMap.get(this.fX2);
        String y1 = (String) this.fGeoMap.get(this.fY1);
        String y2 = (String) this.fGeoMap.get(this.fY2);

        BooleanQuery geoQuery = new BooleanQuery();
        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            BooleanQuery query1 = prepareIntersectGeoQuery(x1, x2, y1, y2, FIRST_X_CASE);
            BooleanQuery query2 = prepareIntersectGeoQuery(x1, x2, y1, y2, SECOND_X_CASE);
            BooleanQuery query3 = prepareIntersectGeoQuery(x1, x2, y1, y2, THIRD_X_CASE);

            geoQuery.add(query1, BooleanClause.Occur.SHOULD);
            geoQuery.add(query2, BooleanClause.Occur.SHOULD);
            geoQuery.add(query3, BooleanClause.Occur.SHOULD);
            if (geoQuery.getClauses().length > 0) {
                booleanQuery.add(geoQuery, BooleanClause.Occur.MUST);
            }
        }

    }

    /**
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param x_case
     * @return booleanquery
     */
    private BooleanQuery prepareIntersectGeoQuery(String x1, String x2, String y1, String y2, int x_case) {

        BooleanQuery booleanQuery = new BooleanQuery();

        switch (x_case) {
        case FIRST_X_CASE:
            BooleanQuery xQuery1FirstCase = new BooleanQuery();
            BooleanQuery xQuery2FirstCase = new BooleanQuery();
            BooleanQuery yQueryFistCase = new BooleanQuery();
            BooleanQuery yOutside = new BooleanQuery();

            TermRangeQuery xRangeQuery1 = new TermRangeQuery(this.fX1, x1, x2, true, true);
            TermRangeQuery xRangeQuery2 = new TermRangeQuery(this.fX2, x1, x2, true, true);
            TermRangeQuery xRangeQuery3 = new TermRangeQuery(this.fX1, x1, x2, true, true);
            TermRangeQuery xRangeQuery4 = new TermRangeQuery(this.fX2, x1, x2, true, true);

            TermRangeQuery yRangeQuery1 = new TermRangeQuery(this.fY1, y1, y2, true, true);
            TermRangeQuery yRangeQuery2 = new TermRangeQuery(this.fY2, y1, y2, true, true);
            TermRangeQuery yRangeQuery3 = new TermRangeQuery(this.fY1, this.fY1Min, y1, true, true);
            TermRangeQuery yRangeQuery4 = new TermRangeQuery(this.fY2, y1, this.fY2Max, true, true);

            xQuery1FirstCase.add(xRangeQuery1, BooleanClause.Occur.MUST);
            xQuery1FirstCase.add(xRangeQuery2, BooleanClause.Occur.MUST_NOT);
            xQuery2FirstCase.add(xRangeQuery3, BooleanClause.Occur.MUST_NOT);
            xQuery2FirstCase.add(xRangeQuery4, BooleanClause.Occur.MUST);

            yOutside.add(yRangeQuery3, BooleanClause.Occur.MUST);
            yOutside.add(yRangeQuery4, BooleanClause.Occur.MUST);

            yQueryFistCase.add(yRangeQuery1, BooleanClause.Occur.SHOULD);
            yQueryFistCase.add(yRangeQuery2, BooleanClause.Occur.SHOULD);
            yQueryFistCase.add(yOutside, BooleanClause.Occur.SHOULD);

            booleanQuery.add(xQuery1FirstCase, BooleanClause.Occur.SHOULD);
            booleanQuery.add(xQuery2FirstCase, BooleanClause.Occur.SHOULD);

            break;

        case SECOND_X_CASE:
            TermRangeQuery xRangeQuery1SecondCase = new TermRangeQuery(this.fX1, x1, x2, true, true);
            TermRangeQuery xRangeQuery2SecondCase = new TermRangeQuery(this.fX2, x1, x2, true, true);
            TermRangeQuery yRangeQuery1SecondCase = new TermRangeQuery(this.fY1, y1, y2, true, true);
            TermRangeQuery yRangeQuery2SecondCase = new TermRangeQuery(this.fY1, y1, y2, true, true);
            booleanQuery.add(xRangeQuery1SecondCase, BooleanClause.Occur.MUST);
            booleanQuery.add(xRangeQuery2SecondCase, BooleanClause.Occur.MUST);
            booleanQuery.add(yRangeQuery1SecondCase, BooleanClause.Occur.MUST_NOT);
            booleanQuery.add(yRangeQuery2SecondCase, BooleanClause.Occur.MUST_NOT);
            break;

        case THIRD_X_CASE:
            BooleanQuery thirdCase = new BooleanQuery();
            TermRangeQuery xRangeQuery1ThirdCase = new TermRangeQuery(this.fX1, x1, x2, true, true);
            TermRangeQuery xRangeQuery2ThirdCase = new TermRangeQuery(this.fX2, x1, x2, true, true);
            TermRangeQuery yRangeQuery1ThirdCase = new TermRangeQuery(this.fY1, y1, y2, true, true);
            TermRangeQuery yRangeQuery2ThirdCase = new TermRangeQuery(this.fY2, y1, y2, true, true);
            thirdCase.add(yRangeQuery1ThirdCase, BooleanClause.Occur.SHOULD);
            thirdCase.add(yRangeQuery2ThirdCase, BooleanClause.Occur.SHOULD);
            booleanQuery.add(xRangeQuery1ThirdCase, BooleanClause.Occur.MUST_NOT);
            booleanQuery.add(xRangeQuery2ThirdCase, BooleanClause.Occur.MUST_NOT);
            booleanQuery.add(thirdCase, BooleanClause.Occur.MUST);
            break;

        default:
            break;
        }

        return booleanQuery;
    }

    /**
     * @param booleanQuery
     */
    private void prepareInsideGeoQuery(BooleanQuery booleanQuery) {
        String x1 = (String) this.fGeoMap.get(this.fX1);
        String x2 = (String) this.fGeoMap.get(this.fX2);
        String y1 = (String) this.fGeoMap.get(this.fY1);
        String y2 = (String) this.fGeoMap.get(this.fY2);

        if (x1 != null && x2 != null && y1 != null && y2 != null) {
            TermRangeQuery xRangeQuery1 = new TermRangeQuery(this.fX1, x1, x2, true, true);
            TermRangeQuery xRangeQuery2 = new TermRangeQuery(this.fX2, x1, x2, true, true);
            TermRangeQuery yRangeQuery1 = new TermRangeQuery(this.fY1, y1, y2, true, true);
            TermRangeQuery yRangeQuery2 = new TermRangeQuery(this.fY2, y1, y2, true, true);

            booleanQuery.add(xRangeQuery1, BooleanClause.Occur.MUST);
            booleanQuery.add(xRangeQuery2, BooleanClause.Occur.MUST);
            booleanQuery.add(yRangeQuery1, BooleanClause.Occur.MUST);
            booleanQuery.add(yRangeQuery2, BooleanClause.Occur.MUST);

        }
    }

    /**
     * @param query
     * 
     */
    @SuppressWarnings("unchecked")
    private void prepareTime(BooleanQuery query) {
        List<String> list = (List<String>) this.fTimeMap.get(this.fTime);
        if (list == null) {
            // nothing selected -> default inside
            prepareInsideTime(query);
        } else if (list.size() == 1) {
            String value = list.get(0);
            if ("intersect".equals(value)) {
                // innerhalb oder schneidet
                prepareIntersectTime(query);
            } else if ("include".equals(value)) {
                // innerhalb oder umschliesst
                prepareInsideOrIncludeQuery(query);
            }
        } else if (list.size() == 2) {
            if (list.contains("intersect") && list.contains("include")) {
                prepareIntersectOrIncludeTime(query);
            }
        }
    }

    /**
     * @param query
     */
    private void prepareIntersectOrIncludeTime(BooleanQuery query) {

        BooleanQuery booleanQueryTime = new BooleanQuery();
        BooleanQuery inside = new BooleanQuery();
        BooleanQuery include = new BooleanQuery();
        BooleanQuery traverse = new BooleanQuery();
        prepareInsideTime(inside);
        prepareIncludeTimeQuery(include);
        prepareTraverseTime(traverse);
        booleanQueryTime.add(inside, BooleanClause.Occur.SHOULD);
        booleanQueryTime.add(include, BooleanClause.Occur.SHOULD);
        booleanQueryTime.add(traverse, BooleanClause.Occur.SHOULD);
        query.add(booleanQueryTime, BooleanClause.Occur.MUST);
    }

    /**
     * @param query
     */
    private void prepareInsideOrIncludeQuery(BooleanQuery query) {
        BooleanQuery booleanQueryTime = new BooleanQuery();
        BooleanQuery inside = new BooleanQuery();
        BooleanQuery include = new BooleanQuery();
        prepareInsideTime(inside);
        prepareIncludeTimeQuery(include);
        booleanQueryTime.add(inside, BooleanClause.Occur.SHOULD);
        booleanQueryTime.add(include, BooleanClause.Occur.SHOULD);
        query.add(booleanQueryTime, BooleanClause.Occur.MUST);

    }

    /**
     * @param query
     */
    private void prepareIntersectTime(BooleanQuery query) {
        BooleanQuery booleanQueryTime = new BooleanQuery();
        BooleanQuery inside = new BooleanQuery();
        BooleanQuery traverse = new BooleanQuery();
        prepareInsideTime(inside);
        prepareTraverseTime(traverse);
        booleanQueryTime.add(inside, BooleanClause.Occur.SHOULD);
        booleanQueryTime.add(traverse, BooleanClause.Occur.SHOULD);
        query.add(booleanQueryTime, BooleanClause.Occur.MUST);
    }

    /**
     * @param query
     */
    private void prepareInsideTime(BooleanQuery query) {
        String t1 = (String) this.fTimeMap.get(this.fT1);
        String t2 = (String) this.fTimeMap.get(this.fT2);
        if (t1 != null && t2 != null) {
            // e.g. 2006-04-05 -> 20040405
            t1 = t1.replaceAll("-", "");
            t2 = t2.replaceAll("-", "");

            TermRangeQuery rangeQueryt0 = new TermRangeQuery(this.fT0, t1, t2, true, true);
            TermRangeQuery rangeQuery11 = new TermRangeQuery(this.fT1, t1, t2, true, true);
            TermRangeQuery rangeQuery12 = new TermRangeQuery(this.fT2, t1, t2, true, true);

            BooleanQuery booleanQueryTime = new BooleanQuery();
            BooleanQuery booleanQueryT1T2 = new BooleanQuery();
            booleanQueryT1T2.add(rangeQuery11, BooleanClause.Occur.MUST);
            booleanQueryT1T2.add(rangeQuery12, BooleanClause.Occur.MUST);
            booleanQueryTime.add(booleanQueryT1T2, BooleanClause.Occur.SHOULD);
            booleanQueryTime.add(rangeQueryt0, BooleanClause.Occur.SHOULD);
            query.add(booleanQueryTime, BooleanClause.Occur.MUST);
        }
    }

    /**
     * @param query
     */
    private void prepareIncludeTimeQuery(BooleanQuery query) {
        String t1 = (String) this.fTimeMap.get(this.fT1);
        String t2 = (String) this.fTimeMap.get(this.fT2);
        if (t1 != null && t2 != null) {
            // e.g. 2006-04-05 -> 20040405
            t1 = t1.replaceAll("-", "");
            t2 = t2.replaceAll("-", "");

            TermRangeQuery rangeQuery11 = new TermRangeQuery(this.fT1, "00000000", t1, true, true);
            TermRangeQuery rangeQuery12 = new TermRangeQuery(this.fT2, t2, this.fDateFormat.format(new Date()), true,
                    true);

            query.add(rangeQuery11, BooleanClause.Occur.MUST);
            query.add(rangeQuery12, BooleanClause.Occur.MUST);
        }

    }

    /**
     * @param query
     */
    private void prepareTraverseTime(BooleanQuery query) {
        String t1 = (String) this.fTimeMap.get(this.fT1);
        String t2 = (String) this.fTimeMap.get(this.fT2);
        if (t1 != null && t2 != null) {
            // e.g. 2006-04-05 -> 20040405
            t1 = t1.replaceAll("-", "");
            t2 = t2.replaceAll("-", "");

            TermRangeQuery rangeQuery11 = new TermRangeQuery(this.fT1, "00000000", t1, true, true);
            TermRangeQuery rangeQuery12 = new TermRangeQuery(this.fT2, t1, t2, true, true);

            TermRangeQuery rangeQuery21 = new TermRangeQuery(this.fT1, t1, t2, true, true);
            TermRangeQuery rangeQuery22 = new TermRangeQuery(this.fT2, t2, this.fDateFormat.format(new Date()), true,
                    true);

            BooleanQuery booleanQueryTime = new BooleanQuery();
            BooleanQuery first = new BooleanQuery();
            first.add(rangeQuery11, BooleanClause.Occur.MUST);
            first.add(rangeQuery12, BooleanClause.Occur.MUST);

            BooleanQuery second = new BooleanQuery();
            second.add(rangeQuery21, BooleanClause.Occur.MUST);
            second.add(rangeQuery22, BooleanClause.Occur.MUST);

            booleanQueryTime.add(first, BooleanClause.Occur.SHOULD);
            booleanQueryTime.add(second, BooleanClause.Occur.SHOULD);
            query.add(booleanQueryTime, BooleanClause.Occur.MUST);
        }
    }

    /**
     * @param output
     * @param c
     * @param indexField
     * @param value
     */
    private void addClause(BooleanQuery output, Clause c, String indexField, String value) {
        org.apache.lucene.search.Query query = null;
        QueryParser parser = new QueryParser(Version.LUCENE_30, indexField, new NutchDocumentAnalyzer(getConf()));
        try {
            BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE / 200);
            query = parser.parse(value);
        } catch (ParseException e) {
            LOGGER.warning("can not parse term: " + value);
            LOGGER.warning(e.getMessage());
        }

        if (query == null) {
            query = new TermQuery(new Term(indexField, value));
        }

        output.add(query, (c.isProhibited() ? BooleanClause.Occur.MUST_NOT : (c.isRequired() ? BooleanClause.Occur.MUST
                : BooleanClause.Occur.SHOULD)));
    }

    public void setConf(Configuration arg0) {
        this.fConfiguration = arg0;
        PluginRepository pluginRepository = PluginRepository.get(this.fConfiguration);
        PluginDescriptor pluginDescriptor = pluginRepository.getPluginDescriptor(IPlugSNSPlugin.PLUGIN_ID);

        /*
         * Plugin pluginInstance = null; try { pluginInstance =
         * pluginRepository.getPluginInstance(pluginDescriptor); } catch
         * (PluginRuntimeException e) { LOGGER.warning("query fails: " +
         * e.getMessage()); } IPlugSNSPlugin plugin = (IPlugSNSPlugin)
         * pluginInstance;
         */

        File file = new File(pluginDescriptor.getPluginPath(), "plugin.properties");
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(file));
        } catch (IOException e) {
            LOGGER.severe("Could not load plugin.properties of index-sns plugin!");
            e.printStackTrace();
        }

        this.fX1 = properties.getProperty(IPlugSNSPlugin.X1);
        this.fY1 = properties.getProperty(IPlugSNSPlugin.Y1);
        this.fX2 = properties.getProperty(IPlugSNSPlugin.X2);
        this.fY2 = properties.getProperty(IPlugSNSPlugin.Y2);
        this.fX1Min = DoublePadding.padding(Double.parseDouble(properties.getProperty(IPlugSNSPlugin.X1_LIMIT)));
        this.fX2Max = DoublePadding.padding(Double.parseDouble(properties.getProperty(IPlugSNSPlugin.X2_LIMIT)));
        this.fY1Min = DoublePadding.padding(Double.parseDouble(properties.getProperty(IPlugSNSPlugin.Y1_LIMIT)));
        this.fY2Max = DoublePadding.padding(Double.parseDouble(properties.getProperty(IPlugSNSPlugin.Y2_LIMIT)));

        this.fT0 = properties.getProperty(IPlugSNSPlugin.T0);
        this.fT1 = properties.getProperty(IPlugSNSPlugin.T1);
        this.fT2 = properties.getProperty(IPlugSNSPlugin.T2);
        this.fArea = properties.getProperty(IPlugSNSPlugin.AREA);
        this.fLocation = properties.getProperty(IPlugSNSPlugin.LOCATION);
        this.fTime = properties.getProperty(IPlugSNSPlugin.TIME);
        this.fCoord = properties.getProperty(IPlugSNSPlugin.COORD);

        this.fGeoMap = new HashMap<String, Object>();
        this.fTimeMap = new HashMap<String, Object>();
        this.fBuzzwordSet = new HashSet<String>();

        this.fDateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
        this.fDateFormat.applyPattern("yyyyMMdd");
    }

    public Configuration getConf() {
        return this.fConfiguration;
    }

}
