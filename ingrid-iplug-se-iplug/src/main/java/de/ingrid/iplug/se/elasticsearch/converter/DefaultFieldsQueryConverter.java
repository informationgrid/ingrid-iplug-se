package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;
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
                    //for (String field : content) {
                        //((BoolQueryBuilder)subQuery).should( QueryBuilders.wildcardQuery( field, t ) );
                        ((BoolQueryBuilder)subQuery).should( QueryBuilders.queryString( t ) );
//                        try {
//                            TokenStream tokenStream = (new GermanAnalyzer(Version.LUCENE_CURRENT)).tokenStream( null, "Fa?en");//t );
//                            CharTermAttribute termAttr = tokenStream.addAttribute(CharTermAttribute.class);
//                            tokenStream.reset();
//                            while (tokenStream.incrementToken()) {
//                                System.out.println( termAttr.toString() );
//                            }
//                            tokenStream.end();
//                            tokenStream.close();
//                        } catch (IOException e) {
//                            // TODO Auto-generated catch block
//                            e.printStackTrace();
//                        }
                    //}
                    
                } else {
                
                    subQuery = QueryBuilders.multiMatchQuery( t, content );
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
                
            queryBuilder.must( bq );
        }
    }
    
//    @Bean
//    public DefaultFieldsQueryConverter defaultFieldsQueryConverter() {
//        return this;
//    }

}
