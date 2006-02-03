/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.se;

import java.io.File;

import org.apache.nutch.util.NutchConf;

import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

import junit.framework.TestCase;

public class TestNutchSearch extends TestCase {

    public void testSearch() throws Exception {
        File file = new File("./testIndex");
        NutchConf conf = new NutchConf();

        conf.set("plugin.folders",
                "/Users/joa23/Documents/workspace/nutch-trunk/src/plugin");
        NutchSearcher searcher = new NutchSearcher(file, "testId", conf);
        IngridQuery query = QueryStringParser.parse("partner:bund");
        IngridHits hits = searcher.search(query, 0, 100);
        assertTrue(hits.size() > 0);
    }
}
