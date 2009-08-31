/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
