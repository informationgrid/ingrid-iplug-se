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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.plugin.Extension;
import org.apache.nutch.plugin.PluginClassLoader;

/**
 * A default implementation of the Gui Component Extension point. The tab name
 * is extracted from the plugin.xml extension point attributes "tabName" and
 * internationalized by property files availbe inside the plugin folder
 * 
 */
public class DefaultGuiComponent implements IGuiComponent {

  public static final Log LOG = LogFactory.getLog(DefaultGuiComponent.class);

  private Map<Locale, ResourceBundle> _resourceBundles = new HashMap<Locale, ResourceBundle>();

  private Extension _extension;

  private NutchInstance _nutchInstance;

  public void configure(Extension extension, NutchInstance instance) {
    _extension = extension;
    _nutchInstance = instance;
  }

  public String getLabel(String key, Locale locale) {
    String value = key;
    try {
      ResourceBundle labels = _resourceBundles.get(locale);
      if (labels == null) {
        PluginClassLoader classLoader = _extension.getDescriptor()
            .getClassLoader();
        labels = ResourceBundle.getBundle(_extension.getAttribute("bundle"),
            locale, classLoader);
        _resourceBundles.put(locale, labels);
      }
      value = labels.getString(key);
    } catch (Exception e) {
      LOG.warn("can not load lable", e);
    }
    return value;
  }

  /**
   * @return Returns the fConfiguration.
   */
  public NutchInstance getNutchInstance() {
    return _nutchInstance;
  }

  /**
   * @return Returns the fExtension.
   */
  public Extension getExtension() {
    return _extension;
  }

}
