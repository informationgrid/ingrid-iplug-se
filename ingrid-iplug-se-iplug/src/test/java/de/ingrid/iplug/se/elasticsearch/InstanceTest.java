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

import de.ingrid.elasticsearch.IndexInfo;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;

@RunWith(PowerMockRunner.class)
//@PrepareForTest(JettyStarter.class)
public class InstanceTest {

    @Mock JettyStarter jettyStarter;
    
    @BeforeClass
    public static void setUp() throws Exception {
        new JettyStarter( false );
        JettyStarter.getInstance().config.index = "test";
        JettyStarter.getInstance().config.indexWithAutoId = true;
        JettyStarter.getInstance().config.indexSearchInTypes = new ArrayList<>();
        Utils.setupES();
    }

    @Before
    public void initTest() throws Exception {
        Utils.initIndex( jettyStarter );
        Utils.indexManager.switchAlias( "ingrid_test", JettyStarter.getInstance().config.index, "test_1" );
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Utils.index.close();
        Utils.elastic.destroy();
    }

    /**
     * This is a test on multiple indices/nodes, which is not needed at the moment.
     * @throws Exception
     */
    @Test
    public void searchForAll() throws Exception {
        Utils.prepareIndex( Utils.elastic, "data/webUrls1_b.json", "test_catalog" );

        // assertThat( JettyStarter.getInstance().config.indexSearchInTypes.size(), is( 0 ) );
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setToIndex("test_1");
        IndexInfo indexInfo2 = new IndexInfo();
        indexInfo2.setToIndex("test_catalog");

        Utils.elasticConfig.activeIndices = new IndexInfo[] { indexInfo, indexInfo2 };
        IngridQuery q = Utils.getIngridQuery( "" );
        IngridHits search = Utils.index.search( q, 0, 10 );
        assertThat( search, not( is( nullValue() ) ) );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS + 2 ) ) );

        Utils.elasticConfig.activeIndices = new IndexInfo[] { indexInfo };
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.length(), is( Long.valueOf( Utils.MAX_RESULTS ) ) );

        Utils.elasticConfig.activeIndices = new IndexInfo[] { indexInfo2 };
        search = Utils.index.search( q, 0, 10 );
        assertThat( search.length(), is( Long.valueOf( 2 ) ) );
        
    }

    
}
