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
 * @author mb
 * 
 */
public class SnsQueryGeoFilterTest extends TestCase {

  private Configuration fConfiguration;

  protected void setUp() throws Exception {
    this.fConfiguration = NutchConfiguration.create();
    File pluginPath = new File("ingrid-nutch/src/plugin");
    File pluginPathOS = new File("apache-nutch-1.8/src/plugin");
    this.fConfiguration.setStrings("plugin.folders", pluginPath.getPath(), pluginPathOS.getPath());
    this.fConfiguration.set("plugin.includes",
            "protocol-http|urlfilter-regex|parse-(text|html|js)|index-sns|query-(basic|site|url)");
  }

  protected void tearDown() throws Exception {
    this.fConfiguration.set("plugin.folders", "plugins");
    this.fConfiguration
        .set(
            "plugin.includes",
            "protocol-http|urlfilter-regex|parse-(text|html|js)|index-basic|query-(basic|site|url)");
  }

  /**
   * @throws Exception
   */
  public void testInside() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("inside", "coord");
    query.addRequiredTerm("7.01", "x1");
    query.addRequiredTerm("8.01", "x2");
    query.addRequiredTerm("50.21", "y1");
    query.addRequiredTerm("52", "y2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+(url:foo^4.0 anchor:foo^2.0 content:foo title:foo^1.5 host:foo^2.0) +x1:[0000000007,01 TO 0000000008,01] +x2:[0000000007,01 TO 0000000008,01] +y1:[0000000050,21 TO 0000000052] +y2:[0000000050,21 TO 0000000052]",
        booleanQuery.toString());
  }

  /**
   * @throws Exception
   */
  public void testInclude() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("include", "coord");
    query.addRequiredTerm("7.01", "x1");
    query.addRequiredTerm("8.01", "x2");
    query.addRequiredTerm("50.21", "y1");
    query.addRequiredTerm("52", "y2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+(url:foo^4.0 anchor:foo^2.0 content:foo title:foo^1.5 host:foo^2.0) +x1:[0000000005,3 TO 0000000007,01] +x2:[0000000008,01 TO 0000000014,77] +y1:[0000000046,76 TO 0000000050,21] +y2:[0000000052 TO 0000000054,73]",
        booleanQuery.toString());
  }

  /**
   * @throws Exception
   */
  public void testIntersect() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("intersect", "coord");
    query.addRequiredTerm("7.01", "x1");
    query.addRequiredTerm("8.01", "x2");
    query.addRequiredTerm("50.21", "y1");
    query.addRequiredTerm("52", "y2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
  }
  
  public void testNoSnsData() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("intersect", "coord");
    query.addRequiredTerm("7.01", "x1");
    query.addRequiredTerm("8.01", "x2");
    query.addRequiredTerm("50.21", "y1");
    query.addRequiredTerm("52", "y2");
    query.addRequiredTerm("on", "incl_meta");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
  }
}
