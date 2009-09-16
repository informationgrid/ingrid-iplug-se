package de.ingrid.iplug.se.urlmaintenance;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Plugin;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRuntimeException;

public class UrlMaintenancePlugin extends Plugin {

  public static final String PLUGIN_ID = "admin-urlmaintenance";

  public UrlMaintenancePlugin(PluginDescriptor pDescriptor, Configuration conf) {
    super(pDescriptor, conf);
  }

  @Override
  public void startUp() throws PluginRuntimeException {

  }

}
