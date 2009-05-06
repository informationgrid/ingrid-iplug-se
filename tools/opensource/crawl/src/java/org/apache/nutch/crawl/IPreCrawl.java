package org.apache.nutch.crawl;

import java.io.IOException;

import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.plugin.Pluggable;

public interface IPreCrawl extends Pluggable, Configurable {

  public final static String X_POINT_ID = IPreCrawl.class.getName();

  void preCrawl(Path crawlDir) throws IOException;
}
