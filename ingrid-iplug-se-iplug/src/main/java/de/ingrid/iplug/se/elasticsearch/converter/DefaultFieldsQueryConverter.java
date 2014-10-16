package de.ingrid.iplug.se.elasticsearch.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder.Operator;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.query.TermQuery;

@Service
public class DefaultFieldsQueryConverter implements IQueryConverter {
    
    private static final String[] content = {"title", "content"};

    @Override
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder) {
        TermQuery[] terms = ingridQuery.getTerms();

        BoolQueryBuilder bq = null;//QueryBuilders.boolQuery();
        
        if (terms.length > 0) {
            List<String> termsAnd = new ArrayList<String>();
            List<String> termsOr = new ArrayList<String>();
            for (TermQuery term : terms) {
                String t = term.getTerm();
                QueryBuilder subQuery = null;

                // if it's a phrase
                if (t.contains( " " )) {
                    subQuery = QueryBuilders.boolQuery();
                    for (String field : content) {
                        ((BoolQueryBuilder)subQuery).should( QueryBuilders.matchPhraseQuery( field, t ) );
                    }
                // in case a term was not identified as a wildcard-term, e.g. "Deutsch*"
                } else if (t.contains( "*" )) {
                    subQuery = QueryBuilders.boolQuery();
                    ((BoolQueryBuilder)subQuery).should( QueryBuilders.queryString( t ) );
                    
                } else if (term.isProhibited()) {
                    subQuery = QueryBuilders.multiMatchQuery( t, content );
                    
                } else {
                
                    // only add term to the correct list, so that the whole terms will be matched correctly
                    // even with stopwords filtered!!!
                    if (term.isRequred()) {
                        termsAnd.add( t );
                    } else { 
                        termsOr.add( t );
                    }
                    
                    continue;
                }
                
                if (term.isRequred()) {
                    if (bq == null) bq = QueryBuilders.boolQuery();
                    if (term.isProhibited()) {
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
            }
            
            if (!termsAnd.isEmpty()) {
                String join = StringUtils.join( termsAnd, " " );
                MultiMatchQueryBuilder subQuery = QueryBuilders.multiMatchQuery( join, content ).operator( Operator.AND );
                if (bq == null) bq = QueryBuilders.boolQuery();
                bq.should( subQuery );
            }
            if (!termsOr.isEmpty()) {
                String join = StringUtils.join( termsOr, " " );
                MultiMatchQueryBuilder subQuery = QueryBuilders.multiMatchQuery( join, content ).operator( Operator.OR );
                if (bq == null) bq = QueryBuilders.boolQuery();
                bq.should( subQuery );
            }
                
            queryBuilder.must( bq );
        }
    }
    
//    @Bean
//    public DefaultFieldsQueryConverter defaultFieldsQueryConverter() {
//        return this;
//    }

}
