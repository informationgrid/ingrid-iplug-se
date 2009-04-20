package org.apache.nutch.admin;

import java.io.File;

import org.apache.hadoop.conf.Configuration;

/**
 * Container for a set of information related to a configuration instance of
 * nutch
 */
public class NutchInstance {

  private File _instanceFolder;

  private Configuration _configuration;

  private String _instanceName;

  public NutchInstance(String name, File folder, Configuration instanceConf) {
    _instanceName = name;
    _instanceFolder = folder;
    _configuration = instanceConf;
  }

  /**
   * @return the name of the instance
   */
  public String getInstanceName() {
    return _instanceName;
  }

  /**
   * @return configuration of this instance
   */
  public Configuration getConfiguration() {
    return _configuration;
  }

  /**
   * @return the folder the instance life in
   */
  public File getInstanceFolder() {
    return _instanceFolder;
  }

}
