/**
 *
 */
package de.ingrid.iplug.sns.nutch.index;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.indexer.IndexingException;
import org.apache.nutch.indexer.IndexingFilter;
import org.apache.nutch.indexer.NutchDocument;
import org.apache.nutch.indexer.lucene.LuceneWriter;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.plugin.Plugin;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.plugin.PluginRuntimeException;

import de.ingrid.iplug.se.crawl.sns.CompressedSnsData;
import de.ingrid.iplug.se.crawl.sns.SnsParseImpl;
import de.ingrid.iplug.sns.nutch.IPlugSNSPlugin;

public class SnsIndexingFilter implements IndexingFilter {

  private static final Log LOGGER = LogFactory.getLog(SnsIndexingFilter.class);

  private String fBuzzword;

  private String fT0;

  private String fT1;

  private String fT2;

  private String fArea;

  private String fLocation;

  private String fX1;

  private String fY1;

  private String fX2;

  private String fY2;

  private Configuration fConfiguration;

  private boolean fIndexOn = false;

  public SnsIndexingFilter() {
    super();
  }

  @Override
  public NutchDocument filter(NutchDocument document, Parse parse, Text url, CrawlDatum datum, Inlinks inlinks)
      throws IndexingException {

    if (!(parse instanceof SnsParseImpl)) {
      return document;
    }

    if (this.fIndexOn) {
      CompressedSnsData snsData = ((SnsParseImpl) parse).getSnsData();
      try {
        indexBuzzwords(snsData.getBuzzwords(), document);
        indexCoordinates(snsData, document);
        indexDate(snsData, document);
      } catch (Exception e) {
        LOGGER.warn("Indexing of sns datas failed", e);
      }
    }
    System.out.println("SnsIndexingFilter.filter()");
    for (String n : document.getFieldNames()) {
      System.out.println(document.getFieldValue(n));
    }
    return document;
  }

  /**
   * @param snsData
   * @param document
   * @throws Exception
   */
  private void indexDate(CompressedSnsData snsData, NutchDocument document) throws Exception {

    // t0
    Set<Text> t0s = snsData.getT0s();
    for (Iterator<Text> iterator = t0s.iterator(); iterator.hasNext();) {
      Text t0 = iterator.next();
      document.add(fT0, t0.toString());
    }

    // t1 & t2
    Set<Text> t1t2s = snsData.getT1T2s();
    for (Iterator<Text> iterator = t1t2s.iterator(); iterator.hasNext();) {
      Text t1t2 = iterator.next();
      String time = t1t2.toString();
      String[] splits = time.split("\t");
      document.add(fT1, splits[0]);
      document.add(fT2, splits[1]);
    }

  }

  /**
   * @param snsData
   * @param document
   * @throws Exception
   */
  private void indexCoordinates(CompressedSnsData snsData, NutchDocument document) throws Exception {

    // Topic-Ids
    Set<Text> topicList = snsData.getTopicIds();
    for (Iterator<Text> iterator = topicList.iterator(); iterator.hasNext();) {
      Text topicId = iterator.next();
      document.add(fArea, topicId.toString());
    }

    // Community-Codes (will be stored in areaid-Fields!)
    Set<Text> comunityCodeList = snsData.getCommunityCodes();
    for (Iterator<Text> iterator = comunityCodeList.iterator(); iterator.hasNext();) {
      Text communityCode = iterator.next();
      document.add(fArea, communityCode.toString());
    }

    // Location
    Set<Text> locations = snsData.getLocations();
    for (Text location : locations) {
      document.add(fLocation, location.toString());
    }

    // x1, x2, x3, x4
    boolean coordinatesFound = snsData.isCoordinatesFound();
    if (coordinatesFound) {
      Text x1 = snsData.getX1();
      Text x2 = snsData.getX2();
      Text y1 = snsData.getY1();
      Text y2 = snsData.getY2();
      document.add(fX1, x1.toString());
      document.add(fX2, x2.toString());
      document.add(fY1, y1.toString());
      document.add(fY2, y2.toString());
    }
  }

  /**
   * @param buzzwords
   * @param document
   * @throws Exception
   */
  private void indexBuzzwords(Set<Text> buzzwords, NutchDocument document) throws Exception {
    for (Iterator<Text> iterator = buzzwords.iterator(); iterator.hasNext();) {
      Text buzzword = iterator.next();
      document.add(fBuzzword, buzzword.toString().toLowerCase());
    }
  }

  public void setConf(Configuration conf) {
    this.fConfiguration = conf;
    PluginRepository pluginRepository = PluginRepository.get(this.fConfiguration);
    PluginDescriptor pluginDescriptor = pluginRepository.getPluginDescriptor(IPlugSNSPlugin.PLUGIN_ID);

    Plugin pluginInstance = null;
    try {
      pluginInstance = pluginRepository.getPluginInstance(pluginDescriptor);
    } catch (PluginRuntimeException e) {
      LOGGER.warn("indexing fails: " + e.getMessage());
    }
    IPlugSNSPlugin plugin = (IPlugSNSPlugin) pluginInstance;

    Properties properties = plugin.getProperties();
    this.fBuzzword = properties.getProperty(IPlugSNSPlugin.BUZZWORD);

    this.fX1 = properties.getProperty(IPlugSNSPlugin.X1);
    this.fY1 = properties.getProperty(IPlugSNSPlugin.Y1);
    this.fX2 = properties.getProperty(IPlugSNSPlugin.X2);
    this.fY2 = properties.getProperty(IPlugSNSPlugin.Y2);

    this.fT0 = properties.getProperty(IPlugSNSPlugin.T0);
    this.fT1 = properties.getProperty(IPlugSNSPlugin.T1);
    this.fT2 = properties.getProperty(IPlugSNSPlugin.T2);
    this.fArea = properties.getProperty(IPlugSNSPlugin.AREA);
    this.fLocation = properties.getProperty(IPlugSNSPlugin.LOCATION);
    this.fIndexOn = "true".equals(System.getProperty("index.sns")) ? true : false;
    if (!this.fIndexOn) {
      this.fIndexOn = this.fConfiguration.getBoolean("index.sns", false);
    }
  }

  public Configuration getConf() {
    return this.fConfiguration;
  }

  @Override
  public void addIndexBackendOptions(Configuration conf) {
    // t0, t1 and t2 is stored, indexed and un-tokenized
    LuceneWriter.addFieldOptions(fT0, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
    LuceneWriter.addFieldOptions(fT1, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
    LuceneWriter.addFieldOptions(fT2, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);

    // area is stored, indexed and tokenized
    LuceneWriter.addFieldOptions(fArea, LuceneWriter.STORE.YES, LuceneWriter.INDEX.TOKENIZED, conf);

    // location is stored, indexed and tokenized
    LuceneWriter.addFieldOptions(fLocation, LuceneWriter.STORE.YES, LuceneWriter.INDEX.TOKENIZED, conf);

    // x1, x2, y1, y2 is stored, indexed and un-tokenized
    LuceneWriter.addFieldOptions(fX1, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
    LuceneWriter.addFieldOptions(fX2, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
    LuceneWriter.addFieldOptions(fY1, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
    LuceneWriter.addFieldOptions(fY2, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);

    // buzzword is stored, indexed and un-tokenized
    LuceneWriter.addFieldOptions(fBuzzword, LuceneWriter.STORE.YES, LuceneWriter.INDEX.UNTOKENIZED, conf);
  }

}
