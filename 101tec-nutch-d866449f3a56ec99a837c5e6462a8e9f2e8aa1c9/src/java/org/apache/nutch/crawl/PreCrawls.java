package org.apache.nutch.crawl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

  private static final String PRECRAWLS_ORDER = "crawl-prepare.order";

  private IPreCrawl[] _preCrawls;

  public PreCrawls(Configuration configuration) {
    String order = configuration.get(PRECRAWLS_ORDER);
    ObjectCache objectCache = ObjectCache.get(configuration);
    _preCrawls = (IPreCrawl[]) objectCache.getObject(IPreCrawl.X_POINT_ID);
    if (_preCrawls == null) {
      try {

        String[] orderedImpl = null;
        if (order != null && !order.trim().equals("")) {
          orderedImpl = order.split("\\s+");
        }

        ExtensionPoint point = PluginRepository.get(configuration)
            .getExtensionPoint(IPreCrawl.X_POINT_ID);
        if (point == null) {
          throw new RuntimeException(IPreCrawl.X_POINT_ID + " not found.");
        }
        Extension[] extensions = point.getExtensions();
        Map<String, IPreCrawl> preCrawlMap = new HashMap<String, IPreCrawl>();
        for (int i = 0; i < extensions.length; i++) {
          Extension extension = extensions[i];
          IPreCrawl preCrawl = (IPreCrawl) extension.getExtensionInstance();
          preCrawlMap.put(preCrawl.getClass().getName(), preCrawl);
        }
        IPreCrawl[] preCrawls = new IPreCrawl[preCrawlMap.size()];
        if (orderedImpl == null || orderedImpl.length == 0) {
          int counter = 0;
          for (IPreCrawl preCrawl : preCrawlMap.values()) {
            LOG.info("Adding " + preCrawl.getClass().getName());
            preCrawls[counter] = preCrawl;
            counter++;
          }
        } else {
          int counter = 0;
          for (String className : orderedImpl) {
            IPreCrawl preCrawl = preCrawlMap.get(className);
            preCrawls[counter] = preCrawl;
            counter++;
          }
        }
        objectCache.setObject(IPreCrawl.X_POINT_ID, preCrawls);
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
