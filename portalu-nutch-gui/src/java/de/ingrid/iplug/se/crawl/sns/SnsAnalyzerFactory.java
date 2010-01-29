package de.ingrid.iplug.se.crawl.sns;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.JobConf;

public class SnsAnalyzerFactory {

  private static final Log LOGGER = LogFactory.getLog(SnsAnalyzerFactory.class);

  private static Map<String, SnsAnalyzer> _analyzers = new HashMap<String, SnsAnalyzer>();

  public static SnsAnalyzer getAnalyzer(String segmentName) {
    SnsAnalyzer analyzer = (SnsAnalyzer) _analyzers.get(segmentName);
    return analyzer;
  }

  public static SnsAnalyzer createAnalyzer(String analyzerKey, JobConf jobConf) throws IOException {
    SnsAnalyzer snsAnalyzer = new SnsAnalyzer(jobConf);
    LOGGER.info("put new analyzer: " + analyzerKey);
    _analyzers.put(analyzerKey, snsAnalyzer);
    LOGGER.info("currently '" + _analyzers.size() + "' analyzers cached: " + _analyzers);
    return snsAnalyzer;
  }

  public static void stopAnalyzer(String analyzerKey) throws IOException {
    SnsAnalyzer analyzer = _analyzers.remove(analyzerKey);
    if (analyzer != null) {
      LOGGER.info("stop analyzer: " + analyzerKey);
      analyzer.close();
    }
  }
}
