package de.ingrid.iplug.se.elasticsearch.converter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

import de.ingrid.utils.query.FieldQuery;
import de.ingrid.utils.query.IngridQuery;

/**
 * This class is a specialized field converter, which transforms special fields
 * for date and location into a correct query.
 * 
 * @author André
 *
 */
public class FieldQueryIGCConverter implements IQueryConverter {
    
    private final static Logger log = Logger.getLogger( FieldQueryIGCConverter.class );
    
    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        FieldQuery[] fields = ingridQuery.getFields();

        BoolQueryBuilder bq = null;
        
        Map<String,Object> geoMap = new HashMap<String,Object>(fields.length);
        Map<String,Object> timeMap = new HashMap<String,Object>(fields.length);
        
        for (FieldQuery fieldQuery : fields) {
            QueryBuilder subQuery = null;
            
            String indexField = fieldQuery.getFieldName();
            String value = fieldQuery.getFieldValue().toLowerCase();
            if (indexField.equals("x1")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("x2")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("y1")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("y2")) {
                geoMap.put(indexField, value);
            } else if (indexField.equals("coord")) {
                List<String> list = (List<String>) geoMap.get(indexField);
                if (list == null) {
                    list = new LinkedList<String>();
                }
                list.add(value);
                geoMap.put(indexField, list);
            } else if ("t0".equals(indexField)) {
                timeMap.put(indexField, value);
            } else if ("t1".equals(indexField)) {
                timeMap.put(indexField, value);
            } else if ("t2".equals(indexField)) {
                timeMap.put(indexField, value);
            } else if ("time".equals(indexField)) {
                List<String> list = (List<String>) timeMap.get(indexField);
                if (list == null) {
                    list = new LinkedList<String>();
                }
                list.add(value);
                timeMap.put(indexField, list);
            } else if ("incl_meta".equals(indexField) && "on".equals(value)) {
                // TODO: booleanQuery.add(new TermQuery(new Term(query.getFieldName(), query.getFieldValue().toLowerCase())), Occur.SHOULD);
            } else {
            
                subQuery = QueryBuilders.matchQuery( fieldQuery.getFieldName(), fieldQuery.getFieldValue() );
                queryBuilder.must( applyAndOrRules( fieldQuery, bq, subQuery ) );
            }
        }
        
        if (null == geoMap.get("coord")) {
            final List<String> list = new LinkedList<String>();
            list.add("exact");
            geoMap.put("coord", list);
        }
        //prepareGeo(bq, geoMap);
        prepareTime(queryBuilder, timeMap);
    }
    
    
//    private void prepareGeo(BoolQueryBuilder booleanQuery, Map<String,Object> geoMap) {
//        List<String> list = (List<String>) geoMap.get("coord");
//        if (list != null) {
//            BooleanQuery.setMaxClauseCount(10240);
//            Iterator<String> iterator = list.iterator();
//            while (iterator.hasNext()) {
//                String value = iterator.next();
//                if ("inside".equals(value)) {
//                    // innerhalb
//                    prepareInsideGeoQuery(booleanQuery, geoMap);
//                } else if ("intersect".equals(value)) {
//                    // schneiden
//                    prepareIntersectGeoQuery(booleanQuery, geoMap);
//                } else if ("include".equals(value)) {
//                    // enthalten
//                    prepareIncludeGeoQuery(booleanQuery, geoMap);
//                } else {
//                    prepareExactGeoQuery(booleanQuery, geoMap);
//                }
//            }
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("resulting query:" + booleanQuery.toString());
//        }
//    }
//
//    /** Hits BBox INCLUDE the passed BBox */
//    private static void prepareIncludeGeoQuery(BoolQueryBuilder booleanQuery, Map<String,Object> geoMap) {
//        String x1 = (String) geoMap.get("x1");
//        String x2 = (String) geoMap.get("x2");
//        String y1 = (String) geoMap.get("y1");
//        String y2 = (String) geoMap.get("y2");
//
//        if (x1 != null && x2 != null && y1 != null && y2 != null) {
//            // At least one x1 AND one y1 AND one x2 AND one y2 OUTSIDE passed BBox, borders are ok
//            Query x1Below = NumericRangeQuery.newDoubleRange("x1",
//                    new Double(-360.0), new Double(x1), true, true);
//            Query x2Above = NumericRangeQuery.newDoubleRange("x2",
//                    new Double(x2), new Double(360.0), true, true);
//            Query y1Below = NumericRangeQuery.newDoubleRange("y1",
//                    new Double(-360.0), new Double(y1), true, true);
//            Query y2Above = NumericRangeQuery.newDoubleRange("y2",
//                    new Double(y2), new Double(360.0), true, true);
//
//            booleanQuery.must( x1Below )
//                        .must( x2Above )
//                        .must( y1Below )
//                        .must( y2Above );
//        }
//    }
//
//    private static void prepareExactGeoQuery(BoolQueryBuilder booleanQuery, Map<String,Object> geoMap) {
//        String x1 = (String) geoMap.get("x1");
//        String x2 = (String) geoMap.get("x2");
//        String y1 = (String) geoMap.get("y1");
//        String y2 = (String) geoMap.get("y2");
//
//        if (x1 != null && x2 != null && y1 != null && y2 != null) {
//            Query x1EqualsX1 = NumericRangeQuery.newDoubleRange("x1",
//                    new Double(x1), new Double(x1), true, true);
//            Query x2EqualsX2 = NumericRangeQuery.newDoubleRange("x2",
//                    new Double(x2), new Double(x2), true, true);
//            Query y1EqualsY1 = NumericRangeQuery.newDoubleRange("y1",
//                    new Double(y1), new Double(y1), true, true);
//            Query y2EqualsY2 = NumericRangeQuery.newDoubleRange("y2",
//                    new Double(y2), new Double(y2), true, true);
//
//            booleanQuery.must( x1EqualsX1 )
//                        .must( x2EqualsX2 )
//                        .must( y1EqualsY1 )
//                        .must( y2EqualsY2 );
//        }
//    }
//
//    /** Hits BBox INTERSECT the passed BBox */
//    private static void prepareIntersectGeoQuery(BoolQueryBuilder booleanQuery, Map<String,Object> geoMap) {
//        String x1 = (String) geoMap.get("x1");
//        String x2 = (String) geoMap.get("x2");
//        String y1 = (String) geoMap.get("y1");
//        String y2 = (String) geoMap.get("y2");
//
//        if (x1 != null && x2 != null && y1 != null && y2 != null) {
//
//            // NOT ALL OUTSIDE (this would be coord:include)
//            // at least one x1 OR one y1 OR one x2 OR one y2 INSIDE passed BBox, borders are ok
//            Query x1Inside = NumericRangeQuery.newDoubleRange("x1",
//                    new Double(x1), new Double(x2), true, true);
//            Query x2Inside = NumericRangeQuery.newDoubleRange("x2",
//                    new Double(x1), new Double(x2), true, true);
//            Query y1Inside = NumericRangeQuery.newDoubleRange("y1",
//                    new Double(y1), new Double(y2), true, true);
//            Query y2Inside = NumericRangeQuery.newDoubleRange("y2",
//                    new Double(y1), new Double(y2), true, true);
//            BooleanQuery isInside = new BooleanQuery();
//            isInside.add(x1Inside, Occur.SHOULD);
//            isInside.add(x2Inside, Occur.SHOULD);
//            isInside.add(y1Inside, Occur.SHOULD);
//            isInside.add(y2Inside, Occur.SHOULD);
//            booleanQuery.must( isInside );
//
//            // NOT ALL INSIDE (this would be coord:inside)
//            // at least one x1 OR one y1 OR one x2 OR one y2 OUTSIDE passed BBox, borders are ok
//            Query x1Below = NumericRangeQuery.newDoubleRange("x1",
//                    new Double(-360.0), new Double(x1), true, true);
//            Query x2Above = NumericRangeQuery.newDoubleRange("x2",
//                    new Double(x2), new Double(360.0), true, true);
//            Query y1Below = NumericRangeQuery.newDoubleRange("y1",
//                    new Double(-360.0), new Double(y1), true, true);
//            Query y2Above = NumericRangeQuery.newDoubleRange("y2",
//                    new Double(y2), new Double(360.0), true, true);
//            BooleanQuery isOutside = new BooleanQuery();
//            isOutside.add(x1Below, Occur.SHOULD);
//            isOutside.add(x2Above, Occur.SHOULD);
//            isOutside.add(y1Below, Occur.SHOULD);
//            isOutside.add(y2Above, Occur.SHOULD);
//            booleanQuery.must( isOutside );
//
//            // guarantee that not all x are in area left or all x are in area right
//
//            // at least one x1 is left of right border, border itself is ok
//            Query x1LeftX2 = NumericRangeQuery.newDoubleRange("x1",
//                    new Double(-360.0), new Double(x2), true, true);
//            booleanQuery.must( x1LeftX2 );
//            // at least one x2 is right of left border, border itself is ok
//            Query x2RightX1 = NumericRangeQuery.newDoubleRange("x2",
//                    new Double(x1), new Double(360.0), true, true);
//            booleanQuery.must( x2RightX1 );
//
//            // guarantee that not all y are in area below or all y are in area above
//
//            // at least one y1 is below upper border, border itself is ok
//            Query y1BelowY2 = NumericRangeQuery.newDoubleRange("y1",
//                    new Double(-360.0), new Double(y2), true, true);
//            booleanQuery.must( y1BelowY2 );
//            // at least one y2 is above lower border, border itself is ok
//            Query y2AboveY1 = NumericRangeQuery.newDoubleRange("y2",
//                    new Double(y1), new Double(360.0), true, true);
//            booleanQuery.must( y2AboveY1 );
//        }
//    }
//
//    private static void prepareInsideGeoQuery(BoolQueryBuilder booleanQuery, Map<String,Object> geoMap) {
//        String x1 = (String) geoMap.get("x1");
//        String x2 = (String) geoMap.get("x2");
//        String y1 = (String) geoMap.get("y1");
//        String y2 = (String) geoMap.get("y2");
//
//        if (x1 != null && x2 != null && y1 != null && y2 != null) {
//            // NO x1 or y1 or x2 or y2 OUTSIDE passed BBox, borders are ok
//            Query x1Below = NumericRangeQuery.newDoubleRange("x1",
//                    new Double(-360.0), new Double(x1), true, false);
//            Query x2Above = NumericRangeQuery.newDoubleRange("x2",
//                    new Double(x2), new Double(360.0), false, true);
//            Query y1Below = NumericRangeQuery.newDoubleRange("y1",
//                    new Double(-360.0), new Double(y1), true, false);
//            Query y2Above = NumericRangeQuery.newDoubleRange("y2",
//                    new Double(y2), new Double(360.0), false, true);
//            booleanQuery.mustNot( x1Below )
//                        .mustNot( x2Above )
//                        .mustNot( y1Below )
//                        .mustNot( y2Above );
//
//            // NOTICE: WE NEED A MUST (or SHOULD) ! MUST_NOT ALONE IS NOT SUFFICIENT FOR Lucene Query !
//            // http://lucene.apache.org/java/2_9_0/api/all/org/apache/lucene/search/BooleanClause.Occur.html#MUST_NOT
//            // "Note that it is not possible to search for queries that only consist of a MUST_NOT clause."
//            booleanQuery.must( new MatchAllDocsQuery() );
//        }
//    }

    private static void prepareTime(BoolQueryBuilder query, Map<String,Object> timeMap) {
        if (log.isDebugEnabled()) {
            log.debug("start prepareTime with t0=" + timeMap.get("t0") + ", t1:" + timeMap.get("t1") + ", t2:"
                    + timeMap.get("t2"));
        }

        List<String> list = (List<String>) timeMap.get("time");
        if (list == null) {
            // nothing selected -> default inside
            prepareInsideTime(query, timeMap);
        } else {
            Iterator<String> iterator = list.iterator();
            while (iterator.hasNext()) {
                String value = iterator.next();
                if ("intersect".equals(value)) {
                    // innerhalb oder schneidet
                    prepareInsideOrIntersectTime(query, timeMap);
                } else if ("include".equals(value)) {
                    // innerhalb oder umschliesst
                    prepareInsideOrIncludeQuery(query, timeMap);
                } else {
                    prepareInsideTime(query, timeMap);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("resulting query:" + query.toString());
        }
    }

    private static void prepareInsideOrIncludeQuery(BoolQueryBuilder query, Map<String,Object> timeMap) {
        BoolQueryBuilder booleanQueryTime = QueryBuilders.boolQuery();
        BoolQueryBuilder inside = QueryBuilders.boolQuery();
        BoolQueryBuilder include = QueryBuilders.boolQuery();
        prepareInsideTime(inside, timeMap);
        prepareIncludeTimeQuery(include, timeMap);
        if (include.hasClauses()) {
            booleanQueryTime.should( include );
        }
        if (inside.hasClauses()) {
            booleanQueryTime.should( inside );
        }

        if (booleanQueryTime.hasClauses()) {
            query.must( booleanQueryTime );
        }
    }

    private static void prepareInsideOrIntersectTime(BoolQueryBuilder query, Map<String,Object> timeMap) {
        BoolQueryBuilder booleanQueryTime = QueryBuilders.boolQuery();
        BoolQueryBuilder inside = QueryBuilders.boolQuery();
        BoolQueryBuilder traverse = QueryBuilders.boolQuery();
        prepareInsideTime(inside, timeMap);
        if (inside.hasClauses()) {
            booleanQueryTime.should(inside);
        }
        prepareTraverseTime(traverse, timeMap);
        if (traverse.hasClauses()) {
            booleanQueryTime.should(traverse);
        }
        if (booleanQueryTime.hasClauses()) {
            query.must(booleanQueryTime);
        }
    }

    private static void prepareInsideTime(BoolQueryBuilder query, Map<String,Object> timeMap) {
        String t0 = (String) timeMap.get("t0");
        String t1 = (String) timeMap.get("t1");
        String t2 = (String) timeMap.get("t2");
        if (t1 != null && t2 != null) {
            // also find single dates!
            RangeQueryBuilder rangeQueryT0 = QueryBuilders.rangeQuery( "t0" )
                    .from( t1 )
                    .to( t2 )
                    .includeLower( true )
                    .includeUpper( true );
            RangeQueryBuilder rangeQueryT1 = QueryBuilders.rangeQuery( "t1" )
                    .from( t1 )
                    .to( t2 )
                    .includeLower( true )
                    .includeUpper( true );
            RangeQueryBuilder rangeQueryT2 = QueryBuilders.rangeQuery( "t2" )
                    .from( t1 )
                    .to( t2 )
                    .includeLower( true )
                    .includeUpper( true );
            
            BoolQueryBuilder timeSpanQuery = QueryBuilders.boolQuery();
            timeSpanQuery.must( rangeQueryT1 )
                     .must( rangeQueryT2 );
            
            query.should( timeSpanQuery )
                 .should( rangeQueryT0 );
        } else if (t0 != null) {
            RangeQueryBuilder rangeQueryT0 = QueryBuilders.rangeQuery( "t0" )
                    .from( t0 )
                    .to( t0 )
                    .includeLower( true )
                    .includeUpper( true );
            query.must( rangeQueryT0 );
        }
    }

    private static void prepareIncludeTimeQuery(BoolQueryBuilder query, Map<String,Object> timeMap) {
        String t0 = (String) timeMap.get("t0");
        String t1 = (String) timeMap.get("t1");
        String t2 = (String) timeMap.get("t2");
        if (t1 != null && t2 != null) {
            RangeQueryBuilder rangeQueryToT0 = QueryBuilders.rangeQuery( "t1" )
                    .to( t1 )
                    .includeUpper( true );
            RangeQueryBuilder rangeQueryFromT0 = QueryBuilders.rangeQuery( "t2" )
                    .from( t2 )
                    .includeLower( true );
            
            query.must( rangeQueryToT0 )
                 .must( rangeQueryFromT0 );
            
        } else if (null != t0) {
            RangeQueryBuilder rangeQueryToT0 = QueryBuilders.rangeQuery( "t1" )
                    .to( t0 )
                    .includeUpper( true );
            RangeQueryBuilder rangeQueryFromT0 = QueryBuilders.rangeQuery( "t2" )
                    .from( t0 )
                    .includeLower( true );
            
            query.must( rangeQueryToT0 )
                 .must( rangeQueryFromT0 );
        }

    }

    private static void prepareTraverseTime(BoolQueryBuilder query, Map<String,Object> timeMap) {
        String t0 = (String) timeMap.get("t0");
        String t1 = (String) timeMap.get("t1");
        String t2 = (String) timeMap.get("t2");
        if (t1 != null && t2 != null) {
            RangeQueryBuilder rangeQuery11 = QueryBuilders.rangeQuery( "t1" )
                    .to( t1 )
                    .includeLower( true )
                    .includeUpper( true );

            RangeQueryBuilder rangeQuery12 = QueryBuilders.rangeQuery( "t2" )
                    .from( t1 )
                    .to( t2 )
                    .includeLower( true )
                    .includeUpper( true );

            RangeQueryBuilder rangeQuery21 = QueryBuilders.rangeQuery( "t1" )
                    .from( t1 )
                    .to( t2 )
                    .includeLower( true )
                    .includeUpper( true );
            
            RangeQueryBuilder rangeQuery22 = QueryBuilders.rangeQuery( "t2" )
                    .from( t2 )
                    .includeLower( true )
                    .includeUpper( true );

            BoolQueryBuilder booleanQueryTime = QueryBuilders.boolQuery();
            BoolQueryBuilder first = QueryBuilders.boolQuery()
                    .must( rangeQuery11 )
                    .must( rangeQuery12 );
            
            BoolQueryBuilder second = QueryBuilders.boolQuery()
                    .must( rangeQuery21 )
                    .must( rangeQuery22 );

            booleanQueryTime.should(first)
                            .should(second);
            query.must( booleanQueryTime );
        } else if (null != t0) {
//            Term termT0 = new Term("t0", t0);
//            Term termT1 = new Term("t1", t0);
//            Term termT2 = new Term("t2", t0);

            BoolQueryBuilder booleanQueryTime = QueryBuilders.boolQuery();
            booleanQueryTime.should( QueryBuilders.termQuery( "t0", t0 ) )
                            .should( QueryBuilders.termQuery( "t1", t0 ) )
                            .should( QueryBuilders.termQuery( "t2", t0 ) );
            query.must( booleanQueryTime );
        }
    }
    
    
    
    private QueryBuilder applyAndOrRules(IngridQuery fieldQuery, BoolQueryBuilder bq, QueryBuilder subQuery) {
        if (fieldQuery.isRequred()) {
            if (bq == null) bq = QueryBuilders.boolQuery();
            if (fieldQuery.isProhibited()) {
                bq.mustNot( subQuery );
            } else {                        
                bq.must( subQuery );
            }
            
        } else {
            // if it's an OR-connection then the currently built query must become a sub-query
            // so that the AND/OR connection is correctly transformed. In case there was an
            // AND-connection before, the transformation would become:
            // OR( (term1 AND term2), term3)
            if (bq == null) {
                bq = QueryBuilders.boolQuery();
                bq.should( subQuery );
                
            } else {
                BoolQueryBuilder parentBq = QueryBuilders.boolQuery();
                parentBq.should( bq ).should( subQuery );
                bq = parentBq;
            }
        }
        
        return bq;
    }

}
