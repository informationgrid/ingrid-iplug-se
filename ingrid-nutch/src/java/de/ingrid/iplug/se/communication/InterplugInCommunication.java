package de.ingrid.iplug.se.communication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.nutch.plugin.PluginClassLoader;

/**
 * This class is used help to exchange simple objects between different plugins.
 * <p>
 * <i> Why this is necessary?</i><br/>
 * When a plugin will be loaded, the standard classloader will be replaced by
 * the {@linkplain PluginClassLoader} and all classes for the plugin will be
 * loaded by this replaced loader. The <code>PluginClassLoader</code> sees only
 * the jar build for the plugin and the third party reference libs loaded under
 * lib folder of relevant plugin. Due to the parent classloader of
 * <code>PluginClassLoader</code>, all classes referred in the classpath on
 * startup the main classes are reached too.<br/>
 * Soo, the problem is when one plugin contains a class and wants to share a
 * class instance to another plugin. The owner plugin will create the sharing
 * class directly by its <code>PluginClassLoader</code> but the consumer plugin
 * can not reach it. The consumer's <code>PluginClassLoader</code> does not see
 * the requested class it can only load it by the parent class loader. But the
 * loading with different classloader will always end in oddities like
 * ClassCastExceptions or new creation of singleton objects.
 * </p>
 * <p>
 * <i>E.G.:</i>
 * <table>
 * <tr>
 * <td>
 * plugin A: admin-url-maintenance loads the DatabaseExport-class by its own
 * PluginClassLoader instance.</td>
 * <tr/>
 * <tr>
 * <td>
 * plugin B: admin-crawl need the DatabaseExport and loads the
 * DatabaseExport-class by the parent class loader of its own PluginClassLoader
 * instance.</td>
 * </tr>
 * <tr>
 * <td>
 * Result: Even a sharing of a singleton DatabaseObject would cause
 * 'java.lang.ClassCastException:
 * de.ingrid.iplug.se.urlmaintenance.DatabaseExport cannot be cast to
 * de.ingrid.iplug.se.urlmaintenance.DatabaseExport'.</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * <i>Idee to solve problem</i><br/>
 * So, the strategy to solve this problem is only to share simple objects linke
 * 'String', 'List', etc. between plugins, that are only reached by the parent
 * class loader.
 * </p>
 * 
 * @author ralfwehner
 */
public class InterplugInCommunication<T> {

  private static InterplugInCommunication<String> _stringListCommunication = null;
  
  private final Map<String, List<T>> _objectContent;
  private Lock _contentLock = new ReentrantLock(true);

  public InterplugInCommunication() {
    super();
    _objectContent = new HashMap<String, List<T>>();
  }

  public static InterplugInCommunication<String> getInstanceForStringLists() {
    if (_stringListCommunication == null) {
      _stringListCommunication = new InterplugInCommunication<String>();
    }
    return _stringListCommunication;
  }

  public void setObjectContent(String key, List<T> content) {
    _contentLock.lock();
    try {
      _objectContent.put(key, content);
    } finally {
      _contentLock.unlock();
    }
  }

  public List<T> getObjectContent(String key) {
    return _objectContent.get(key);
  }
}
