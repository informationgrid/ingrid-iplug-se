package org.apache.nutch.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.util.NutchConfiguration;

public class GuiComponentExtensionContainerTest extends TestCase {

  public void testGeneralGuiComponent() throws Exception {
    Configuration configuration = NutchConfiguration.create();
    configuration.set("plugin.folders", "src/plugin");
    GuiComponentExtensionContainer guiComponentContainer = new GuiComponentExtensionContainer(
            configuration);
    List<Extension> list = guiComponentContainer
            .getGeneralGuiComponentExtensions();
    assertTrue(list.size() > 1);
    Set<String> set = new HashSet<String>();
    for (Extension extension : list) {
      set.add(extension.getDescriptor().getPluginId());
    }
    assertTrue(set.contains("admin-instance"));
  }

  public void testInstanceGuiComponent() throws Exception {
    Configuration configuration = NutchConfiguration.create();
    configuration.set("plugin.folders", "src/plugin");
    GuiComponentExtensionContainer guiComponentContainer = new GuiComponentExtensionContainer(
            configuration);
    List<Extension> list = guiComponentContainer
            .getInstanceGuiComponentExtensions();
    assertTrue(list.size() > 1);
  }

}
