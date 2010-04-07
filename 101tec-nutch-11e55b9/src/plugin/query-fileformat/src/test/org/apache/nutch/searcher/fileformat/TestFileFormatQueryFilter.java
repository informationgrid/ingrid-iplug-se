package org.apache.nutch.searcher.fileformat;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryFilters;
import org.apache.nutch.util.NutchConfiguration;

public class TestFileFormatQueryFilter extends TestCase {

  private Configuration fConfiguration;

  protected void setUp() throws Exception {
    this.fConfiguration = NutchConfiguration.create();
    String userDir = System.getProperty("user.dir");
    String pluginPath = new File(userDir, "src/plugin").getAbsolutePath();
    this.fConfiguration.set("plugin.folders", pluginPath);
    this.fConfiguration
        .set("plugin.includes",
            "protocol-http|urlfilter-regex|parse-(text|html|js)|query-(basic|fileformat)");
  }

  protected void tearDown() throws Exception {
    this.fConfiguration.set("plugin.folders", "plugins");
    this.fConfiguration
        .set(
            "plugin.includes",
            "protocol-http|urlfilter-regex|parse-(text|html|js)|index-basic|query-(basic|site|url)");
  }

  public void testFilter() throws Exception {
    Query query = Query.parse("test fileformat:pdf",
        this.fConfiguration);
    BooleanQuery booleanQuery = new QueryFilters(this.fConfiguration)
        .filter(query);
    assertNotNull(booleanQuery);
    String queryString = booleanQuery.toString();
    assertTrue(queryString.indexOf("+subType:pdf") > -1);
  }
  
  public void testPPT() throws Exception {
    Query query = Query.parse("test fileformat:mspowerpoint",
        this.fConfiguration);
    BooleanQuery booleanQuery = new QueryFilters(this.fConfiguration)
        .filter(query);
    assertNotNull(booleanQuery);
    String queryString = booleanQuery.toString();
    assertTrue(queryString.indexOf("+subType:vnd.ms-powerpoint") > -1);
  }
}
