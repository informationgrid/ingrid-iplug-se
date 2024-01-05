/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.elasticsearch;

import de.ingrid.admin.Config;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;


public class InstanceTest {

    @BeforeAll
    public static void setUp() throws Exception {
        SEIPlug.baseConfig = new Config();
        SEIPlug.baseConfig.index = "test";
        // SEIPlug.baseConfig.indexWithAutoId = true;
        // SEIPlug.baseConfig.indexSearchInTypes = new ArrayList<>();
        Utils.setupES();
    }

    @BeforeEach
    public void initTest() throws Exception {
        Utils.initIndex();
        Utils.indexManager.switchAlias("ingrid_test", SEIPlug.baseConfig.index, "test_1");
    }

    @AfterAll
    public static void tearDown() throws Exception {
        Utils.index.close();
        Utils.elastic.destroy();
    }

    /**
     * This is a test on multiple indices/nodes, which is not needed at the moment.
     *
     * @throws Exception
     */
    @Test
    public void searchForAll() throws Exception {
        Utils.prepareIndex(Utils.elastic, "data/webUrls1_b.json", "test_catalog", "default");

        // assertThat( baseConfig.indexSearchInTypes.size(), is( 0 ) );
        IndexInfo indexInfo = new IndexInfo();
        indexInfo.setToIndex("test_1");
        indexInfo.setToType("web");
        IndexInfo indexInfo2 = new IndexInfo();
        indexInfo2.setToIndex("test_catalog");
        indexInfo2.setToType("default");

        Utils.elasticConfig.activeIndices = new IndexInfo[]{indexInfo, indexInfo2};
        IngridQuery q = Utils.getIngridQuery("");
        IngridHits search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.length(), is(Long.valueOf(Utils.MAX_RESULTS + 2)));

        Utils.elasticConfig.activeIndices = new IndexInfo[]{indexInfo};
        search = Utils.index.search(q, 0, 10);
        assertThat(search.length(), is(Long.valueOf(Utils.MAX_RESULTS)));

        Utils.elasticConfig.activeIndices = new IndexInfo[]{indexInfo2};
        search = Utils.index.search(q, 0, 10);
        assertThat(search.length(), is(Long.valueOf(2)));

    }


}
