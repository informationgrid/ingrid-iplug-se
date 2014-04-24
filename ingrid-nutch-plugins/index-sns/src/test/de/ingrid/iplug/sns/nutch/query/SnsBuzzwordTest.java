/**
 * 
 */
package de.ingrid.iplug.sns.nutch.query;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.nutch.searcher.Query;
import de.ingrid.nutch.searcher.QueryFilters;

/**
 * @author mb, Ralf
 */
public class SnsBuzzwordTest extends TestCase {
    private Configuration fConfiguration;

    protected void setUp() throws Exception {
        this.fConfiguration = NutchConfiguration.create();
        String userDir = System.getProperty("user.dir");
        String pluginPath = new File(userDir, "ingrid-nutch/src/plugin").getAbsolutePath();
        String pluginPathOS = new File(userDir, "apache-nutch-1.8/src/plugin").getAbsolutePath();
        this.fConfiguration.setStrings("plugin.folders", pluginPath, pluginPathOS);
        this.fConfiguration.set("plugin.includes",
                "protocol-http|urlfilter-regex|parse-(text|html|js)|index-sns|query-(basic|site|url)");
    }

    protected void tearDown() throws Exception {
    }

    public void testBuzzword() throws Exception {
        Query query = new Query(this.fConfiguration);
        query.addRequiredTerm("h2o");
        // TODO rwe: fix this! query.addRequiredTerm("www", "datatype");
        query.addNonRequiredTerm("on", "incl_meta");
        System.out.println("NutchQuery: " + query.toString());
        QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
        BooleanQuery booleanQuery = queryFilters.filter(query);
        System.out.println("LuceneQuery :" + booleanQuery);
        assertEquals("+(url:h2o^4.0 anchor:h2o^2.0 content:h2o title:h2o^1.5 host:h2o^2.0 buzzword:h2o)", booleanQuery
                .toString());
    }

    public void testLocation() throws Exception {
        Query query = new Query(this.fConfiguration);
        query.addRequiredTerm("K�ln");
        query.addRequiredTerm("Bonn", "location");
        System.out.println("NutchQuery: " + query.toString());
        QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
        BooleanQuery booleanQuery = queryFilters.filter(query);
        System.out.println("LuceneQuery :" + booleanQuery);
        assertEquals(
                "+(url:K�ln^4.0 anchor:K�ln^2.0 content:K�ln title:K�ln^1.5 host:K�ln^2.0) +location:bonn",
                booleanQuery.toString());
    }
}
