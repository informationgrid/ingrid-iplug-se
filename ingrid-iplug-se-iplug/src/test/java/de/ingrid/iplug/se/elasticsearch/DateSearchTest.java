/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import de.ingrid.admin.Config;
import de.ingrid.elasticsearch.ElasticConfig;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PrepareForTest(JettyStarter.class)
public class DateSearchTest  {

    @Mock JettyStarter jettyStarter;

    @Mock ElasticConfig elasticConfig;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        JettyStarter.baseConfig = new Config();
        JettyStarter.baseConfig.index = "test";
        JettyStarter.baseConfig.indexWithAutoId = true;
        JettyStarter.baseConfig.indexSearchInTypes = new ArrayList<>();
        Utils.setupES();
    }
    
    @Before
    public void initTest() throws Exception {
        Utils.initIndex( jettyStarter );
        Utils.indexManager.switchAlias( "ingrid_test", JettyStarter.baseConfig.index, "test_1" );
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        Utils.index.close();
        Utils.elastic.destroy();
    }
    
    @Test
    public void searchForDates() {
        // all results are completely inside the time range
        IngridQuery q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-30" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 7 );
        
        // at least one result is on the edge of the time range
        q = Utils.getIngridQuery( "t1:2014-04-01 t2:2014-04-15" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 2 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 7 );
        
        // one result is only partially in the time range and will not be listed
        q = Utils.getIngridQuery( "t1:2014-04-03 t2:2014-04-18" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 7 );
        
        // we only search for concrete date, which is not found
        // although a time range includes this date it will not be found since
        // option "include" is off!
        q = Utils.getIngridQuery( "t0:2014-04-07" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // we only search for concrete date, which is found
        q = Utils.getIngridQuery( "t0:2014-04-04" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 7 );
    }
    
    @Test
    public void searchForDatesIntersect() {
        // intersect two time ranges (with intersection below t2!) where one is completely inside and a date
        IngridQuery q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-30 time:intersect" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7 );
        
        // intersect two time ranges (with intersection above t1!) and a date
        q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-10 time:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7 );
        
        // all docs containing a given date as a start/end date 
        q = Utils.getIngridQuery( "t0:2014-04-07 time:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with a single date 
        q = Utils.getIngridQuery( "t0:2014-04-04 time:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 7 );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with a starting date 
        q = Utils.getIngridQuery( "t0:2014-04-01 time:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // all docs containing a given date as a start/end date
        // -> this will find a document with an end date 
        q = Utils.getIngridQuery( "t0:2014-04-15 time:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
    }
    
    @Test
    public void searchForDatesInclude() {
        IngridQuery q = Utils.getIngridQuery( "t1:2014-03-30 t2:2014-04-30 time:include" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        // document 5 is only intersecting and so not in the results!!!
        Utils.checkHitsForIDs( search.getHits(), 2, 7, 8 );
        
        // all docs intersecting one date 
        q = Utils.getIngridQuery( "t0:2014-04-07 time:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 8 );
        
        // all docs intersecting one date
        // -> match also doc with exact end date (doc: 5)
        q = Utils.getIngridQuery( "t0:2014-04-09 time:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 3 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 8 );
        
        // all docs intersecting one date
        // -> match a doc with a single date
        q = Utils.getIngridQuery( "t0:2014-04-04 time:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
    }
    
    @Test
    public void searchForDatesIntersectAndInclude() {
        IngridQuery q = Utils.getIngridQuery( "((t0:2014-04-04 time:intersect) OR (t0:2014-04-04 time:include))" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
        
        q = Utils.getIngridQuery( "((t1:2014-03-30 t2:2014-04-30 time:intersect) OR (t1:2014-03-30 t2:2014-04-30 time:include))" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        Utils.checkHitsForIDs( search.getHits(), 2, 5, 7, 8 );
        
    }
}
