/**
 * 
 */
package de.ingrid.iplug.sns.nutch.query;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
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

//  public void testBuzzword() throws Exception {
//    Query query = new Query(this.fConfiguration);
//    query.addRequiredTerm("h2o");
//    query.addRequiredTerm("www", "datatype");
//    query.addNonRequiredTerm("on", "incl_meta");
//    System.out.println("NutchQuery: " + query.toString());
//    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
//    BooleanQuery booleanQuery = queryFilters.filter(query);
//    System.out.println("LuceneQuery :" + booleanQuery);
//  }
}
