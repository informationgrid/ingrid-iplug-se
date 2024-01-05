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
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

public class LocationSearchTest {

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        SEIPlug.baseConfig = new Config();
        SEIPlug.baseConfig.index = "test";
//        SEIPlug.baseConfig.indexWithAutoId = true;
        // SEIPlug.baseConfig.indexSearchInTypes = new ArrayList<String>();
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
     * Check for bounding boxes that are truly inside a given one.
     */
    @Test
    public void searchForLocationInside() {
        // surrounds
        IngridQuery q = Utils.getIngridQuery("x1:9.49 y1:51.39 x2:11.45 y2:53.10 coord:inside");
        IngridHits search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // exact
        q = Utils.getIngridQuery("x1:9.89 y1:51.89 x2:11.15 y2:52.70 coord:inside");
        search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // completely inside mostly on the edge
        q = Utils.getIngridQuery("x1:9.9 y1:51.89 x2:11.15 y2:52.70 coord:inside");
        search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(0));

        // intersecting
        q = Utils.getIngridQuery("x1:8 y1:48 x2:10.5 y2:52.10 coord:inside");
        search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(0));

        // completely inside
        q = Utils.getIngridQuery("x1:10 y1:52 x2:11 y2:52.4 coord:inside");
        search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(0));

        // completely outside
        q = Utils.getIngridQuery("x1:3 y1:51 x2:6 y2:52.4 coord:inside");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(0));
    }

    /**
     * Check for bounding boxes that are truly intersecting!
     */
    @Test
    public void searchForLocationIntersect() {
        // surrounds
        IngridQuery q = Utils.getIngridQuery("x1:9.49 y1:51.39 x2:11.45 y2:53.10 coord:intersect");
        IngridHits search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(0));

        // exact
        q = Utils.getIngridQuery("x1:9.89 y1:51.89 x2:11.15 y2:52.70 coord:intersect");
        search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // completely inside mostly on the edge
        q = Utils.getIngridQuery("x1:9.9 y1:51.89 x2:11.15 y2:52.70 coord:intersect");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // intersecting
        q = Utils.getIngridQuery("x1:8 y1:48 x2:10.5 y2:52.10 coord:intersect");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // completely inside
        q = Utils.getIngridQuery("x1:10 y1:52 x2:11 y2:52.4 coord:intersect");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(0));

        // completely outside
        q = Utils.getIngridQuery("x1:3 y1:51 x2:6 y2:52.4 coord:intersect");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(0));
    }

    /**
     * Check for bounding boxes that truly includes the given one!
     */
    @Test
    public void searchForLocationInclude() {
        // surrounds
        IngridQuery q = Utils.getIngridQuery("x1:9.49 y1:51.39 x2:11.45 y2:53.10 coord:include");
        IngridHits search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(0));

        // exact
        q = Utils.getIngridQuery("x1:9.89 y1:51.89 x2:11.15 y2:52.70 coord:include");
        search = Utils.index.search(q, 0, 10);
        assertThat(search, not(is(nullValue())));
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // completely inside mostly on the edge
        q = Utils.getIngridQuery("x1:9.9 y1:51.89 x2:11.15 y2:52.70 coord:include");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // intersecting
        q = Utils.getIngridQuery("x1:8 y1:48 x2:10.5 y2:52.10 coord:include");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(0));

        // completely inside
        q = Utils.getIngridQuery("x1:10 y1:52 x2:11 y2:52.4 coord:include");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(1));
        Utils.checkHitsForIDs(search.getHits(), 2);

        // completely outside
        q = Utils.getIngridQuery("x1:3 y1:51 x2:6 y2:52.4 coord:include");
        search = Utils.index.search(q, 0, 10);
        assertThat(search.getHits().length, is(0));
    }
}
