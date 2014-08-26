package de.ingrid.iplug.se.elasticsearch.converter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;

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

}
