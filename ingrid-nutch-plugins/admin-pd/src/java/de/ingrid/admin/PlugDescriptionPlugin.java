package de.ingrid.admin;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Plugin;
import org.apache.nutch.plugin.PluginDescriptor;
import org.apache.nutch.plugin.PluginRuntimeException;

public class PlugDescriptionPlugin extends Plugin {

  public static final String PLUGIN_ID = "admin-pd";

  public PlugDescriptionPlugin(final PluginDescriptor pDescriptor, final Configuration conf) {
    super(pDescriptor, conf);
  }

  @Override
  public void startUp() throws PluginRuntimeException {

  }
}
