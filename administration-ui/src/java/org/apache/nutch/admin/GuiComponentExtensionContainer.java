package org.apache.nutch.admin;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.ExtensionPoint;
import org.apache.nutch.plugin.PluginRepository;

public class GuiComponentExtensionContainer {

  private final Configuration _configuration;

  public GuiComponentExtensionContainer(Configuration configuration) {
    _configuration = configuration;
  }

  public List<Extension> getGeneralGuiComponentExtensions() {
    return getGuiComponentExtensions(IGuiComponent.IS_GENERAL_COMPONENT);
  }

  public List<Extension> getInstanceGuiComponentExtensions() {
    return getGuiComponentExtensions(IGuiComponent.IS_INSTANCE_COMPONENT);
  }

  private List<Extension> getGuiComponentExtensions(String attribute) {
    List<Extension> list = new ArrayList<Extension>();
    PluginRepository pluginRepository = PluginRepository.get(_configuration);
    ExtensionPoint extensionPoint = pluginRepository
        .getExtensionPoint(IGuiComponent.X_POINT_ID);
    if (extensionPoint == null) {
      throw new RuntimeException("x-point " + IGuiComponent.X_POINT_ID
          + " not found, check your plugin folder");
    }
    Extension[] extensions = extensionPoint.getExtensions();
    for (int i = 0; i < extensions.length; i++) {
      Extension extension = extensions[i];
      if (extension.getAttribute(attribute) != null
          && extension.getAttribute(attribute).toLowerCase().equals("true")) {
        list.add(extension);
      }
    }
    return list;
  }

}
