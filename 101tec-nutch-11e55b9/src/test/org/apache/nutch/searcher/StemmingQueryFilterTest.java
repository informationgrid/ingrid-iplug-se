/**
 * 
 */
package org.apache.nutch.searcher;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.lucene.search.BooleanQuery;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.indexer.IndexingFilters;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.lucene.LuceneWriter;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.QueryFilters;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.iplug.se.NutchSearcher;
import de.ingrid.utils.IngridHits;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.query.IngridQuery;
import de.ingrid.utils.queryparser.ParseException;
import de.ingrid.utils.queryparser.QueryStringParser;

import junit.framework.TestCase;

/**
 * @author aw
 */
public class StemmingQueryFilterTest extends TestCase {

  private Configuration fConfiguration;

  private String fToday;

  protected void setUp() throws Exception {
    this.fConfiguration = NutchConfiguration.create();
    File pluginPath = new File("portalu-nutch-gui/src/plugin");
    File pluginPathOS = new File("101tec-nutch-11e55b9/src/plugin");
    this.fConfiguration.setStrings("nutch.instance.folder", "101tec-nutch-11e55b9/src/test/instances");
    this.fConfiguration.setStrings("plugin.folders", pluginPath.getPath(), pluginPathOS.getPath());
    this.fConfiguration
        .set(
            "plugin.includes",
            "protocol-http|urlfilter-regex|parse-(text|html|js)|index-basic|query-(basic|site|url)|analysis-de");
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
  public void testStemmingOfAllFieldsQuery() throws Exception {
    // check index
    /*NutchDocument document = new NutchDocument();
    ParseText text = new ParseText("Wir spielen alle mit der XBox und warten gespannt auf die neue Generation. Welches Spiel wird das Beste?");
    Metadata md = new Metadata();
    ParseData data = new ParseData(ParseStatus.STATUS_SUCCESS, "XBox für alle", null, md);
    ParseImpl parseImpl = new ParseImpl(text, data);
    Text utf8 = new Text("http://www.meine-xbox-url.de/xbox");
    IndexingFilters filters = new IndexingFilters(this.fConfiguration);
    document = filters.filter(document, parseImpl, utf8, new CrawlDatum(), null);
    assertNotNull(document);
    
    LuceneWriter lw = new LuceneWriter();
    lw.open((JobConf) this.fConfiguration, "index2");
    
    lw.write(document);*/
      
    // check search
    Query query = new Query(this.fConfiguration);
    query.addRequiredTerm("wasser");
    System.out.println("NutchQuery: " + query.toString());
    QueryFilters queryFilters = new QueryFilters(this.fConfiguration);
    BooleanQuery booleanQuery = queryFilters.filter(query);
    System.out.println("LuceneQuery :" + booleanQuery);
    assertEquals(
        "+((+(url:wass^4.0 anchor:wass^2.0 content:wass title:wass^1.5 host:wass^2.0)))",
        booleanQuery.toString());
    
    
    // access real index
    File instances = new File("101tec-nutch-11e55b9/src/test/instances/test");
    NutchSearcher searcher = new NutchSearcher(instances, "/ingrid-group:test-iplug", this.fConfiguration);
    searcher.updateFacetManager();
    
    searchExpectsNumResults(searcher, "spielen", 1);
    searchExpectsNumResults(searcher, "spiel", 1);
    searchExpectsNumResults(searcher, "spielende", 1);
    searchExpectsNumResults(searcher, "spielball", 0);
    
  }

    private void searchExpectsNumResults(NutchSearcher searcher, String query, int numExpected) throws Exception {
        IngridQuery ingridQuery = QueryStringParser.parse(query);
        IngridHits hits = searcher.search(ingridQuery, 0, 10);
        assertEquals(numExpected, hits.getHits().length);    
    }
  
}
