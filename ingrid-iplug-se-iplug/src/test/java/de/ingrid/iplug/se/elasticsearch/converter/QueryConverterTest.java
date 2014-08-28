package de.ingrid.iplug.se.elasticsearch.converter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.iplug.se.elasticsearch.Utils;

public class QueryConverterTest {

    private static QueryConverter queryConverter;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        queryConverter = new QueryConverter();
        List<IQueryConverter> parsers = new ArrayList<IQueryConverter>();
        parsers.add( new MatchAllQueryConverter() );
        parsers.add( new DefaultFieldsQueryConverter() );
        queryConverter.setQueryParsers( parsers );
    }

    @Test
    public void matchAll() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"must\":{\"match_all\":{}}}}}}") );        
    }
    
    @Test
    public void matchTerm() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wasser" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"must\":{\"multi_match\":{\"query\":\"wasser\",\"fields\":[\"title\",\"abstract\"]}}}}}}") );        
    }
    
    @Test
    public void matchTermsAND() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wasser wald" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"must\":[{\"multi_match\":{\"query\":\"wasser\",\"fields\":[\"title\",\"abstract\"]}},{\"multi_match\":{\"query\":\"wald\",\"fields\":[\"title\",\"abstract\"]}}]}}}}") );        
    }
    
    @Test
    public void matchTermsOR() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wemove OR Deutschland" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"should\":[{\"bool\":{\"should\":{\"multi_match\":{\"query\":\"wemove\",\"fields\":[\"title\",\"abstract\"]}}}},{\"multi_match\":{\"query\":\"Deutschland\",\"fields\":[\"title\",\"abstract\"]}}]}}}}") );
    }
    
    @Test
    public void matchTermsANDOR() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "boden AND wasser OR wald" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"should\":[{\"bool\":{\"must\":[{\"multi_match\":{\"query\":\"boden\",\"fields\":[\"title\",\"abstract\"]}},{\"multi_match\":{\"query\":\"wasser\",\"fields\":[\"title\",\"abstract\"]}}]}},{\"multi_match\":{\"query\":\"wald\",\"fields\":[\"title\",\"abstract\"]}}]}}}}") );        
    }
    
    @Test
    public void matchTermsANDORParentheses() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "Ausland AND (wemove OR Deutschland)" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":[{\"bool\":{\"must\":{\"bool\":{\"should\":[{\"bool\":{\"should\":{\"multi_match\":{\"query\":\"wemove\",\"fields\":[\"title\",\"abstract\"]}}}},{\"multi_match\":{\"query\":\"Deutschland\",\"fields\":[\"title\",\"abstract\"]}}]}}}},{\"bool\":{\"must\":{\"multi_match\":{\"query\":\"Ausland\",\"fields\":[\"title\",\"abstract\"]}}}}]}}") );        
    }

    /**
     * Remove all new lines and spaces for easier matching.
     * @param result
     * @return
     */
    private String strip(String result) {
        return result.replaceAll( "[\r|\n| ]", "" );
    }

}
