/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.elasticsearch.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.index.query.functionscore.fieldvaluefactor.FieldValueFactorFunctionBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.query.ClauseQuery;
import de.ingrid.utils.query.IngridQuery;

public class QueryConverter {
    
    private static Logger log = Logger.getLogger( QueryConverter.class );
    
    @Autowired
    private List<IQueryConverter> _queryConverter;

    public QueryConverter() {
        _queryConverter = new ArrayList<IQueryConverter>();
    }

    public void setQueryParsers(List<IQueryConverter> parsers) {
        this._queryConverter = parsers;
    }

    public BoolQueryBuilder convert(IngridQuery ingridQuery) {
        
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        
        ClauseQuery[] clauses = ingridQuery.getClauses();
        for (ClauseQuery clauseQuery : clauses) {
            final BoolQueryBuilder res = convert(clauseQuery);
            if (clauseQuery.isRequred()) {
                if (clauseQuery.isProhibited())
                    qb.mustNot( res );
                else
                    qb.must( res );
            } else {
                qb.should( res );
            }
        }
        parse(ingridQuery, qb);
        
        return qb;
        
    }
    
    public void parse(IngridQuery ingridQuery, BoolQueryBuilder booleanQuery) {
        if (log.isDebugEnabled()) {
            log.debug("incoming ingrid query:" + ingridQuery.toString());
        }
        for (IQueryConverter queryConverter : _queryConverter) {
            if (log.isDebugEnabled()) {
                log.debug("incoming boolean query:" + booleanQuery.toString());
            }
            queryConverter.parse(ingridQuery, booleanQuery);
            if (log.isDebugEnabled()) {
                log.debug(queryConverter.toString() + ": resulting boolean query:" + booleanQuery.toString());
            }
        }
    }

    /**
     * Wrap a score modifier around the query, which uses a field from the document
     * to boost the score.
     * @param query is the query to apply the score modifier on
     * @return a new query which contains the score modifier and the given query
     */
    public QueryBuilder addScoreModifier(QueryBuilder query) {
        // describe the function to manipulate the score
        FieldValueFactorFunctionBuilder scoreFunc = ScoreFunctionBuilders.fieldValueFactorFunction( SEIPlug.conf.esBoostField );
        scoreFunc.modifier( SEIPlug.conf.esBoostModifier );
        scoreFunc.factor( SEIPlug.conf.esBoostFactor );
        
        // create the wrapper query to apply the score function to the query
        FunctionScoreQueryBuilder funcScoreQuery = new FunctionScoreQueryBuilder( query );
        funcScoreQuery.add( scoreFunc );
        funcScoreQuery.boostMode( SEIPlug.conf.esBoostMode );
        return funcScoreQuery;
    }

}
