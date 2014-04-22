/**
 * 
 */
package de.ingrid.iplug.sns.nutch;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Plugin;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.plugin.PluginRuntimeException;

/**
 * @author marko
 * 
 */
public class IPlugSNSPlugin extends Plugin {

  /**
   * 
   */
  public static final String PLUGIN_ID = "index-sns";

  /**
   * 
   */
  public static final String USERNAME = "username";

  /**
   * 
   */
  public static final String PASSWORD = "password";

  /**
   * 
   */
  public static final String LANGUAGE = "language";

  /**
   * 
   */
  public static final String MAX_FOR_WORD_ANALYZING = "maxWordForAnalyzing";

  /**
   * 
   */
  public static final String BUZZWORD = "buzzword";

  /**
   * 
   */
  public static final String AREA = "area";

  /**
   * 
   */
  public static final String LOCATION = "location";

  /**
   * 
   */
  public static final String T0 = "t0";

  /**
   * 
   */
  public static final String T1 = "t1";

  /**
   * 
   */
  public static final String T2 = "t2";

  /**
   * 
   */
  public static final String X1 = "x1";

  /**
   * 
   */
  public static final String Y1 = "y1";

  /**
   * 
   */
  public static final String X2 = "x2";

  /**
   * 
   */
  public static final String Y2 = "y2";

  /**
   * 
   */
  public static final String COORD = "coord";

  /**
   * 
   */
  public static final String TIME = "time";

  /**
   * 
   */
  public static final String X1_LIMIT = "x1_limit";

  /**
   * 
   */
  public static final String X2_LIMIT = "x2_limit";

  /**
   * 
   */
  public static final String Y1_LIMIT = "y1_limit";

  /**
   * 
   */
  public static final String Y2_LIMIT = "y2_limit";

  private Properties fProperties;

  /**
   * @param pDescriptor
   * @param configuration
   */
  public IPlugSNSPlugin(PluginDescriptor pDescriptor, Configuration configuration) {
    super(pDescriptor, configuration);
  }

  public void startUp() throws PluginRuntimeException {

    try {
      String pluginPath = PluginRepository.get(this.conf).getPluginDescriptor(PLUGIN_ID).getPluginPath();
      File file = new File(pluginPath, "plugin.properties");
      this.fProperties = new Properties();
      this.fProperties.load(new FileInputStream(file));
    } catch (Exception e) {
      throw new PluginRuntimeException(e.getMessage());
    }

  }

  public void shutDown() throws PluginRuntimeException {
    super.shutDown();
  }

  /**
   * @return Returns the fProperties.
   */
  public Properties getProperties() {
    return this.fProperties;
  }
}
