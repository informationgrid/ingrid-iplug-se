/**
 * 
 */
package de.ingrid.iplug.sns.nutch.index;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilters;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.util.NutchConfiguration;

import de.ingrid.iplug.se.crawl.sns.CompressedSnsData;
import de.ingrid.iplug.se.crawl.sns.SnsParseImpl;

/**
 * @author marko, Ralf
 */
public class SnsIndexingPluginTest extends TestCase {

  private Configuration fConfiguration;

  protected void setUp() throws Exception {
    this.fConfiguration = NutchConfiguration.create();
    String userDir = System.getProperty("user.dir");
    String pluginPath = new File(userDir, "ingrid-nutch/src/plugin").getAbsolutePath();
    String pluginPathOS = new File(userDir, "ingrid-nutch-1.8/src/plugin").getAbsolutePath();
    this.fConfiguration.setStrings("plugin.folders", pluginPath, pluginPathOS);
    this.fConfiguration.set("plugin.includes",
        "protocol-http|urlfilter-regex|parse-(text|html|js)|index-sns|query-(basic|site|url)");
  }

  protected void tearDown() throws Exception {
  }

  public void testComunityCode() throws IndexingException {
    NutchDocument document = new NutchDocument();
    Set<Text> communityCodes = new HashSet<Text>();
    communityCodes.add(new Text("a"));
    CompressedSnsData compressedSnsData = new CompressedSnsData();
    compressedSnsData.setCommunityCodes(communityCodes);
    Set<Text> topids = new HashSet<Text>();
    topids.add(new Text("b"));
    compressedSnsData.setTopicIds(topids);
    SnsParseImpl snsParseImpl = new SnsParseImpl(/* text */null, null/* data */, compressedSnsData);
    document = new IndexingFilters(this.fConfiguration).filter(document, snsParseImpl, null/* utf8 */, null, null);
    List<Object> fieldValues = document.getField("areaid").getValues();
    assertEquals(2, fieldValues.size());
    assertEquals("b", fieldValues.get(0));
    assertEquals("a", fieldValues.get(1));
  }

  public void testLocationCode() throws IndexingException {
      Set<Text> locationCodes = new HashSet<Text>();
      locationCodes.add(new Text("location-a"));
      locationCodes.add(new Text("location-b"));
      CompressedSnsData compressedSnsData = new CompressedSnsData();
      compressedSnsData.setLocations(locationCodes);
      SnsParseImpl snsParseImpl = new SnsParseImpl(null, null, compressedSnsData);
      NutchDocument document = new NutchDocument();
      document = new IndexingFilters(this.fConfiguration).filter(document, snsParseImpl, null, null, null);
      List<Object> fieldValues = document.getField("location").getValues();
      assertEquals(2, fieldValues.size());
      assertEquals("location-a", fieldValues.get(1));
      assertEquals("location-b", fieldValues.get(0));
    }

  public void testBuzzwords() throws Exception {
    NutchDocument document = new NutchDocument();
    CompressedSnsData snsData = new CompressedSnsData();
    snsData.setBuzzwords(new HashSet<Text>(Arrays.asList(new Text("buzzA"), new Text("buzzB"))));
    SnsParseImpl snsParseImpl = new SnsParseImpl(null, null, snsData);
    document = new IndexingFilters(this.fConfiguration).filter(document, snsParseImpl, null, null, null);
    assertEquals("buzza", document.getField("buzzword").getValues().get(0));
    assertEquals("buzzb", document.getField("buzzword").getValues().get(1));
  }

  public void testGeographic() throws Exception {
    NutchDocument document = new NutchDocument();
    CompressedSnsData snsData = new CompressedSnsData();
    snsData.setX1(new Text("X1"));
    snsData.setX2(new Text("X2"));
    snsData.setY1(new Text("Y1"));
    snsData.setY2(new Text("Y2"));
    snsData.setCoordinatesFound(true);
    SnsParseImpl snsParseImpl = new SnsParseImpl(null, null, snsData);
    document = new IndexingFilters(this.fConfiguration).filter(document, snsParseImpl, null, null, null);
    assertEquals("X1", document.getFieldValue("x1"));
    assertEquals("X2", document.getFieldValue("x2"));
    assertEquals("Y1", document.getFieldValue("y1"));
    assertEquals("Y2", document.getFieldValue("y2"));
  }

  public void testDates() throws Exception {
    NutchDocument document = new NutchDocument();
    CompressedSnsData snsData = new CompressedSnsData();
    snsData.setT0(new HashSet<Text>(Arrays.asList(new Text("T01-0"), new Text("T01-1"))));
    snsData.setT1T2(new HashSet<Text>(Arrays.asList(new Text("T1a\tT2a"), new Text("T1b\tT2b"))));
    SnsParseImpl snsParseImpl = new SnsParseImpl(null, null, snsData);
    document = new IndexingFilters(this.fConfiguration).filter(document, snsParseImpl, null, null, null);
    assertNotNull(document);
    assertEquals("T01-0", document.getField("t0").getValues().get(1));
    assertEquals("T01-1", document.getField("t0").getValues().get(0));
    assertEquals("T1b", document.getField("t1").getValues().get(0));
    assertEquals("T2b", document.getField("t2").getValues().get(0));
    assertEquals("T1a", document.getField("t1").getValues().get(1));
    assertEquals("T2a", document.getField("t2").getValues().get(1));
  }

  public void testOnlyCallWithParseImpl() throws Exception {
    NutchDocument document = new NutchDocument();
    ParseText text = new ParseText("Wasserverschmutzung magdeburg");
    ParseData data = new ParseData(ParseStatus.STATUS_SUCCESS, "test title", null, null);
    ParseImpl parseImpl = new ParseImpl(text, data);
    Text utf8 = new Text("http://lucene.apache.org/nutch");
    document = new IndexingFilters(this.fConfiguration).filter(document, parseImpl, utf8, null, null);
    assertNotNull(document);
  }
}
