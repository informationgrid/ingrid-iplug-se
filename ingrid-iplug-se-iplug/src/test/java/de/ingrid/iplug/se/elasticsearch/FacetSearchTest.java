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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JettyStarter.class)
public class FacetSearchTest  {

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
        Utils.indexManager.switchAlias( JettyStarter.getInstance().config.index, "test_1" );
    }
    
    @AfterClass
    public static void tearDown() throws Exception {
        Utils.index.close();
        Utils.elastic.destroy();
    }
    
    @Test
    public void facetteSearchAll() {
        IngridQuery ingridQuery = Utils.getIngridQuery( "" );
        Utils.addDefaultFacets( ingridQuery );
        IngridHits search = Utils.index.search( ingridQuery , 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Utils.MAX_RESULTS ) );
        
        IngridDocument facets = (IngridDocument) search.get("FACETS");
        assertThat( facets.size(), is( 6 ) );
        assertThat( facets.getLong( "partner:bund" ), is( 6l ) );
        assertThat( facets.getLong( "partner:bw" ), is( 3l ) );
        assertThat( facets.getLong( "partner:bb" ), is( 1l ) );
        assertThat( facets.getLong( "partner:th" ), is( 1l ) );
        assertThat( facets.getLong( "after:April2014" ), is( 2l ) );
        assertThat( facets.getLong( "datatype:bundPDFs" ), is( 1l ) );
    }
    
    @Test
    public void facetteSearchTerm() {
        IngridQuery ingridQuery = Utils.getIngridQuery( "wemove" );
        Utils.addDefaultFacets( ingridQuery );
        IngridHits search = Utils.index.search( ingridQuery , 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.getHits().length, is( 4 ) );
        
        IngridDocument facets = (IngridDocument) search.get("FACETS");
        assertThat( facets.size(), is( 5 ) );
        assertThat( facets.getLong( "partner:bund" ), is( 2l ) );
        assertThat( facets.getLong( "partner:bb" ), is( 1l ) );
        assertThat( facets.getLong( "partner:th" ), is( 1l ) );
        assertThat( facets.getLong( "after:April2014" ), is( 1l ) );
        assertThat( facets.getLong( "datatype:bundPDFs" ), is( 1l ) );
    }
}
