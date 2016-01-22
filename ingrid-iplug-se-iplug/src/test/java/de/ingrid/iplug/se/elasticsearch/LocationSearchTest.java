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
package de.ingrid.iplug.se.elasticsearch;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.ElasticSearchUtils;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JettyStarter.class)
public class LocationSearchTest  {

    @Mock JettyStarter jettyStarter;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        new JettyStarter( false );
        JettyStarter.getInstance().config.index = "test";
        JettyStarter.getInstance().config.indexWithAutoId = true;
        JettyStarter.getInstance().config.indexSearchInTypes = new ArrayList<String>();
        Utils.setupES();
    }
    
    @Before
    public void initTest() throws Exception {
        Utils.initIndex( jettyStarter );
        ElasticSearchUtils.switchAlias( Utils.elastic.getObject().client(), "test_1" );
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        Utils.index.close();
        Utils.elastic.destroy();
    }

    /**
     * Check for bounding boxes that are truly inside a given one.
     */
    @Test
    public void searchForLocationInside() {
        // surrounds
        IngridQuery q = Utils.getIngridQuery( "x1:9.49 y1:51.39 x2:11.45 y2:53.10 coord:inside" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // exact
        q = Utils.getIngridQuery( "x1:9.89 y1:51.89 x2:11.15 y2:52.70 coord:inside" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // completely inside mostly on the edge
        q = Utils.getIngridQuery( "x1:9.9 y1:51.89 x2:11.15 y2:52.70 coord:inside" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // intersecting
        q = Utils.getIngridQuery( "x1:8 y1:48 x2:10.5 y2:52.10 coord:inside" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // completely inside
        q = Utils.getIngridQuery( "x1:10 y1:52 x2:11 y2:52.4 coord:inside" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // completely outside
        q = Utils.getIngridQuery( "x1:3 y1:51 x2:6 y2:52.4 coord:inside" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 0 ) );
    }
    
    /**
     * Check for bounding boxes that are truly intersecting!
     */
    @Test
    public void searchForLocationIntersect() {
        // surrounds
        IngridQuery q = Utils.getIngridQuery( "x1:9.49 y1:51.39 x2:11.45 y2:53.10 coord:intersect" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // exact
        q = Utils.getIngridQuery( "x1:9.89 y1:51.89 x2:11.15 y2:52.70 coord:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // completely inside mostly on the edge
        q = Utils.getIngridQuery( "x1:9.9 y1:51.89 x2:11.15 y2:52.70 coord:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // intersecting
        q = Utils.getIngridQuery( "x1:8 y1:48 x2:10.5 y2:52.10 coord:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // completely inside
        q = Utils.getIngridQuery( "x1:10 y1:52 x2:11 y2:52.4 coord:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 0 ) );
        
        // completely outside
        q = Utils.getIngridQuery( "x1:3 y1:51 x2:6 y2:52.4 coord:intersect" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 0 ) );
    }

    /**
     * Check for bounding boxes that truly includes the given one!
     */
    @Test
    public void searchForLocationInclude() {
        // surrounds
        IngridQuery q = Utils.getIngridQuery( "x1:9.49 y1:51.39 x2:11.45 y2:53.10 coord:include" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 0 ) );
        
        // exact
        q = Utils.getIngridQuery( "x1:9.89 y1:51.89 x2:11.15 y2:52.70 coord:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // completely inside mostly on the edge
        q = Utils.getIngridQuery( "x1:9.9 y1:51.89 x2:11.15 y2:52.70 coord:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // intersecting
        q = Utils.getIngridQuery( "x1:8 y1:48 x2:10.5 y2:52.10 coord:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 0 ) );
        
        // completely inside
        q = Utils.getIngridQuery( "x1:10 y1:52 x2:11 y2:52.4 coord:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 1 ) );
        Utils.checkHitsForIDs( search.getHits(), 2 );
        
        // completely outside
        q = Utils.getIngridQuery( "x1:3 y1:51 x2:6 y2:52.4 coord:include" );
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.getHits().length, is( 0 ) );
    }
}
