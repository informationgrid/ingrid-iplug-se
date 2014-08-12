package de.ingrid.iplug.se.elasticsearch;

import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

public class ExampleQuery {

    public static IngridQuery byTerm( String term ) {
        try {
            return QueryStringParser.parse( term );
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
//        IngridQuery query = new IngridQuery( true, false, 0, term );
//        if (!term.isEmpty()) {
//            query.addTerm( new TermQuery( true, false, term ) );
//        }
//        return query;
    }
}
