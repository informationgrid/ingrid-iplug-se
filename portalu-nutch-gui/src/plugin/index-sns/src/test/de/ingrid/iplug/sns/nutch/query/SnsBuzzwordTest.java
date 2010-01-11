/**
 * 
 */
package de.ingrid.iplug.sns.nutch.query;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryFilters;
import org.apache.nutch.util.NutchConfiguration;

/**
 * @author mb, Ralf
 */
public class SnsBuzzwordTest extends TestCase {
    private Configuration fConfiguration;

    protected void setUp() throws Exception {
        this.fConfiguration = NutchConfiguration.create();
        String userDir = System.getProperty("user.dir");
        String pluginPath = new File(userDir, "portalu-nutch-gui/src/plugin").getAbsolutePath();
        String pluginPathOS = new File(userDir, "101tec-nutch-a9cddd9/src/plugin").getAbsolutePath();
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
        query.addRequiredTerm("Kšln");
        query.addRequiredTerm("Bonn", "location");
        System.out.println("NutchQuery: " + query.toString());
        QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
        BooleanQuery booleanQuery = queryFilters.filter(query);
        System.out.println("LuceneQuery :" + booleanQuery);
        assertEquals(
                "+(url:Kšln^4.0 anchor:Kšln^2.0 content:Kšln title:Kšln^1.5 host:Kšln^2.0) +location:bonn",
                booleanQuery.toString());
    }
}
