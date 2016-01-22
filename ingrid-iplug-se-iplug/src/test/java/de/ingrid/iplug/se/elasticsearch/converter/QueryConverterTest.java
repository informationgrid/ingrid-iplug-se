/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.index.query.QueryBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.IQueryParsers;
import de.ingrid.admin.elasticsearch.converter.DefaultFieldsQueryConverter;
import de.ingrid.admin.elasticsearch.converter.MatchAllQueryConverter;
import de.ingrid.admin.elasticsearch.converter.QueryConverter;
import de.ingrid.iplug.se.elasticsearch.Utils;

public class QueryConverterTest {

    private static QueryConverter queryConverter;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JettyStarter( false );
        queryConverter = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<IQueryParsers>();
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
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"should\":{\"multi_match\":{\"query\":\"wasser\",\"fields\":[\"title\",\"content\"],\"type\":\"cross_fields\",\"operator\":\"AND\"}}}}}}") );        
    }
    
    @Test
    public void matchTermsAND() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wasser wald" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"should\":{\"multi_match\":{\"query\":\"wasserwald\",\"fields\":[\"title\",\"content\"],\"type\":\"cross_fields\",\"operator\":\"AND\"}}}}}}") );        
    }
    
    @Test
    public void matchTermsOR() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wemove OR Deutschland" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"should\":{\"bool\":{\"should\":{\"multi_match\":{\"query\":\"wemoveDeutschland\",\"fields\":[\"title\",\"content\"],\"type\":\"cross_fields\",\"operator\":\"OR\"}}}}}}") );
    }
    
    @Test
    public void matchTermsANDOR() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "boden AND wasser OR wald" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":{\"bool\":{\"should\":[{\"multi_match\":{\"query\":\"bodenwasser\",\"fields\":[\"title\",\"content\"],\"type\":\"cross_fields\",\"operator\":\"AND\"}},{\"multi_match\":{\"query\":\"wald\",\"fields\":[\"title\",\"content\"],\"type\":\"cross_fields\",\"operator\":\"OR\"}}]}}}}") );        
    }
    
    @Test
    public void matchTermsANDORParentheses() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "Ausland AND (wemove OR Deutschland)" ) );
        assertThat( strip( result.toString() ), is("{\"bool\":{\"must\":[{\"bool\":{\"should\":{\"bool\":{\"should\":{\"multi_match\":{\"query\":\"wemoveDeutschland\",\"fields\":[\"title\",\"content\"],\"type\":\"cross_fields\",\"operator\":\"OR\"}}}}}},{\"bool\":{\"should\":{\"multi_match\":{\"query\":\"Ausland\",\"fields\":[\"title\",\"content\"],\"type\":\"cross_fields\",\"operator\":\"AND\"}}}}]}}") );        
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
