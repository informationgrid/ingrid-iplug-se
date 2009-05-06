package org.apache.nutch.crawl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.ExtensionPoint;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.plugin.PluginRuntimeException;
import org.apache.nutch.util.ObjectCache;

public class PreCrawls {

  public final static Log LOG = LogFactory.getLog(PreCrawls.class);

  private IPreCrawl[] _preCrawls;

  public PreCrawls(Configuration configuration) {
    ObjectCache objectCache = ObjectCache.get(configuration);
    _preCrawls = (IPreCrawl[]) objectCache.getObject(IPreCrawl.X_POINT_ID);
    if (_preCrawls == null) {
      try {
        ExtensionPoint point = PluginRepository.get(configuration)
            .getExtensionPoint(IPreCrawl.X_POINT_ID);
        if (point == null) {
          throw new RuntimeException(IPreCrawl.X_POINT_ID + " not found.");
        }
        Extension[] extensions = point.getExtensions();
        IPreCrawl[] tmpPreCrawls = new IPreCrawl[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
          Extension extension = extensions[i];
          IPreCrawl preCrawl = (IPreCrawl) extension.getExtensionInstance();
          LOG.info("Adding " + preCrawl.getClass().getName());
          tmpPreCrawls[i] = preCrawl;
        }
        objectCache.setObject(IPreCrawl.X_POINT_ID, tmpPreCrawls);
      } catch (PluginRuntimeException e) {
        throw new RuntimeException(e);
      }
      _preCrawls = (IPreCrawl[]) objectCache.getObject(IPreCrawl.X_POINT_ID);
    }
  }

  public void preCrawl(Path crawlDir) throws IOException {
    for (IPreCrawl preCrawl : _preCrawls) {
      preCrawl.preCrawl(crawlDir);
    }
  }

}
