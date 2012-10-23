package org.apache.nutch.parse.dummy;

import org.apache.nutch.protocol.Content;
import org.apache.nutch.parse.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

public class DummyParser implements Parser {
  public static final Log LOG = LogFactory.getLog("org.apache.nutch.parse.dummy");
  
  private Configuration conf;

  public ParseResult getParse(Content content) {
    LOG.debug("!!!IGNORE CONTENT!!!");
    //ParseData parseData = new ParseData(ParseStatus.STATUS_NOTPARSED, "",
    //        new Outlink[0], content.getMetadata());
    return new ParseResult(content.getUrl()); 
    //new ParseImpl("", parseData);
  }

  public void setConf(Configuration conf) {
    this.conf = conf;
  }

  public Configuration getConf() {
    return this.conf;
  }
}