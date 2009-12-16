/**
 * 
 */
package de.ingrid.iplug.sns.nutch.query;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryFilters;
import org.apache.nutch.util.NutchConfiguration;

import junit.framework.TestCase;

/**
 * @author mb
 * 
 */
public class SnsQueryTimeFilterTest extends TestCase {

  private Configuration fConfiguration;

  private String fToday;

  protected void setUp() throws Exception {
    this.fConfiguration = NutchConfiguration.create();
    String userDir = System.getProperty("user.dir");
    String pluginPath = new File(userDir, "portalu-nutch-gui/src/plugin,101tec-nutch-a9cddd9/src/plugin").getAbsolutePath();
    this.fConfiguration.set("plugin.folders", pluginPath);
    this.fConfiguration
        .set(
            "plugin.includes",
            "protocol-http|urlfilter-regex|parse-(text|html|js)|index-sns|query-(basic|site|url)");
    SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat
        .getInstance();
    dateFormat.applyPattern("yyyyMMdd");
    this.fToday = dateFormat.format(new Date());
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
  public void testInsideTimeQuery() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("2006-01-01", "t1");
    query.addRequiredTerm("2006-02-01", "t2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+(url:foo^0.0 anchor:foo^0.0 content:foo title:foo^0.0 host:foo^0.0) +((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201])",
        booleanQuery.toString());
  }

  /**
   * @throws Exception
   */
  public void testIncludeTimeQuery() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("include", "time");
    query.addRequiredTerm("2006-01-01", "t1");
    query.addRequiredTerm("2006-02-01", "t2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);

    assertEquals(
        "+(url:foo^0.0 anchor:foo^0.0 content:foo title:foo^0.0 host:foo^0.0) +((+((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201])) (+t1:[00000000 TO 20060101] +t2:[20060201 TO "
            + this.fToday + "]))", booleanQuery.toString());
  }

  /**
   * @throws Exception
   */
  public void testWithoutTimeDefinition() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("2006-01-01", "t1");
    query.addRequiredTerm("2006-02-01", "t2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+(url:foo^0.0 anchor:foo^0.0 content:foo title:foo^0.0 host:foo^0.0) +((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201])",
        booleanQuery.toString());
  }

  public void testWithoutT() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
  }
  /**
   * @throws Exception
   */
  public void testTimeStamp() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("2006-01-01", "t0");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+(url:foo^0.0 anchor:foo^0.0 content:foo title:foo^0.0 host:foo^0.0) +t0:20060101",
        booleanQuery.toString());
  }

  /**
   * @throws Exception
   */
  public void testIntersectTimeQuery() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("intersect", "time");
    query.addRequiredTerm("2006-01-01", "t1");
    query.addRequiredTerm("2006-02-01", "t2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+(url:foo^0.0 anchor:foo^0.0 content:foo title:foo^0.0 host:foo^0.0) +((+((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201])) (+((+t1:[00000000 TO 20060101] +t2:[20060101 TO 20060201]) (+t1:[20060101 TO 20060201] +t2:[20060201 TO "
            + this.fToday + "]))))", booleanQuery.toString());
  }

  /**
   * @throws Exception
   */
  public void testIntersectOrIncludeTimeQuery() throws Exception {
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("foo");
    query.addRequiredTerm("intersect", "time");
    query.addRequiredTerm("include", "time");
    query.addRequiredTerm("2006-01-01", "t1");
    query.addRequiredTerm("2006-02-01", "t2");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+(url:foo^0.0 anchor:foo^0.0 content:foo title:foo^0.0 host:foo^0.0) +((+((+t1:[20060101 TO 20060201] +t2:[20060101 TO 20060201]) t0:[20060101 TO 20060201])) (+t1:[00000000 TO 20060101] +t2:[20060201 TO "
            + this.fToday
            + "]) (+((+t1:[00000000 TO 20060101] +t2:[20060101 TO 20060201]) (+t1:[20060101 TO 20060201] +t2:[20060201 TO "
            + this.fToday + "]))))", booleanQuery.toString());
  }
  
  
  
  
}
