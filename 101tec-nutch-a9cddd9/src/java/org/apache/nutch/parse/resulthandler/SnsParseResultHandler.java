package org.apache.nutch.parse.resulthandler;

import java.io.IOException;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseResult;
import org.apache.nutch.protocol.Content;

import de.ingrid.iplug.se.crawl.sns.SnsAnalyzer;
import de.ingrid.iplug.se.crawl.sns.SnsAnalyzerFactory;

public class SnsParseResultHandler {

  public static final Log LOG = LogFactory.getLog(SnsParseResultHandler.class);

  public void process(Content content, ParseResult parseResult) {
    String segmentName = content.getMetadata().get(Nutch.SEGMENT_NAME_KEY);
    SnsAnalyzer analyzer = SnsAnalyzerFactory.getAnalyzer(segmentName);

    if (analyzer == null) {
      LOG.error("Internal error: No SnsAnalyser for segement name '" + segmentName + "' found.");
      return;
    }
    for (Entry<Text, Parse> entry : parseResult) {
      Parse value = entry.getValue();
      analyzer.analyze(new Text(content.getUrl()), value.getText());
    }
  }

  public void beginParsing(String segment, JobConf jobConf) throws IOException {
    LOG.info("Starting SnsParseResultHandlers for segment name '" + segment + "'.");
    try {
      SnsAnalyzerFactory.createAnalyzer(segment, jobConf);
    } catch (Throwable e) {
      LOG.error("unable to create SnsAnalyzer", e);
      throw new IOException(e);
    }
  }

  public void stopParsing(String segment) throws IOException {
    LOG.info("Stopping SnsParseResultHandlers for segment name '" + segment + "'.");
    SnsAnalyzerFactory.stopAnalyzer(segment);
  }

}