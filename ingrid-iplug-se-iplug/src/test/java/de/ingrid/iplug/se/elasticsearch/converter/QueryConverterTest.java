/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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

import de.ingrid.elasticsearch.ElasticConfig;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.elasticsearch.search.IQueryParsers;
import de.ingrid.elasticsearch.search.converter.DefaultFieldsQueryConverter;
import de.ingrid.elasticsearch.search.converter.MatchAllQueryConverter;
import de.ingrid.elasticsearch.search.converter.QueryConverter;
import de.ingrid.iplug.se.elasticsearch.Utils;
import org.elasticsearch.index.query.QueryBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.*;

public class QueryConverterTest {

    private static QueryConverter queryConverter;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        queryConverter = new QueryConverter();
        List<IQueryParsers> parsers = new ArrayList<>();
        parsers.add( new MatchAllQueryConverter() );

        ElasticConfig elasticConfig = new ElasticConfig();
        elasticConfig.isEnabled = true;
        elasticConfig.indexSearchDefaultFields = new String[]{"title", "content"};
        elasticConfig.additionalSearchDetailFields = new String[0];
        elasticConfig.remoteHosts = new String[] {"localhost:9300"};
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setToIndex("test_1");
        indexInfo.setToAlias("ingrid_test");
        elasticConfig.activeIndices = new IndexInfo[1];
        elasticConfig.activeIndices[0] = indexInfo;

        parsers.add( new DefaultFieldsQueryConverter(elasticConfig) );
        queryConverter.setQueryParsers( parsers );
    }

    @Test
    public void matchAll() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "" ) );
        assertThat( strip( result.toString() ), containsString("\"match_all\":{") );
    }
    
    @Test
    public void matchTerm() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wasser" ) );
        assertThat( strip( result.toString() ), containsString("\"query\":\"wasser\""));
        assertThat( strip( result.toString() ), containsString("\"title"));
        assertThat( strip( result.toString() ), containsString("\"content"));
        assertThat( strip( result.toString() ), containsString("\"operator\":\"AND\"") );
    }

    @Test
    public void matchTermsAND() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wasser wald" ) );
        assertThat( strip( result.toString() ), containsString("\"query\":\"wasserwald\"") );
        assertThat( strip( result.toString() ), containsString("\"operator\":\"AND\"") );
    }

    @Test
    public void matchTermsOR() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "wemove OR Deutschland" ) );
        assertThat( strip( result.toString() ), containsString("\"query\":\"wemoveDeutschland\""));
        assertThat( strip( result.toString() ), containsString("\"operator\":\"OR\"") );
    }

    @Test
    public void matchTermsANDOR() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "boden AND wasser OR wald" ) );
        assertThat( strip( result.toString() ), containsString("\"query\":\"bodenwasser\""));
        assertThat( strip( result.toString() ), containsString("\"query\":\"wald\""));
        assertThat( strip( result.toString() ), containsString("\"operator\":\"AND\"") );
        assertThat( strip( result.toString() ), containsString("\"operator\":\"OR\"") );
    }
    
    @Test
    public void matchTermsANDORParentheses() {
        QueryBuilder result = queryConverter.convert( Utils.getIngridQuery( "Ausland AND (wemove OR Deutschland)" ) );
        assertThat( strip( result.toString() ), containsString("\"query\":\"wemoveDeutschland\""));
        assertThat( strip( result.toString() ), containsString("\"query\":\"Ausland\""));
    }

    /**
     * Remove all new lines and spaces for easier matching.
     */
    private String strip(String result) {
        return result.replaceAll( "[\r|\n| ]", "" );
    }

}
