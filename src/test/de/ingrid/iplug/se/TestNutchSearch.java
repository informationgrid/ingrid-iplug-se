/*
 * Copyright (c) 1997-2005 by media style GmbH
 * 
 * $Source: /cvs/asp-search/src/java/com/ms/aspsearch/PermissionDeniedException.java,v $
 */

package de.ingrid.iplug.se;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.utils.IngridHits;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.QueryStringParser;

public class TestNutchSearch extends TestCase {

    public void testSearch() throws Exception {
        File file = new File("./testIndex");
        Configuration conf = NutchConfiguration.create();

        conf.set("plugin.folders",
                "/Users/joa23/Documents/workspace/nutch-trunk/src/plugin");
        NutchSearcher searcher = new NutchSearcher(file, "testId", conf);
        IngridQuery query = QueryStringParser.parse("partner:bund");
        IngridHits hits = searcher.search(query, 0, 100);
        assertTrue(hits.size() > 0);
    }
}
