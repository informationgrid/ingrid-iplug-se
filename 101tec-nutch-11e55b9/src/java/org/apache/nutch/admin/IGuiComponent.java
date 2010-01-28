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

import java.util.Locale;

import org.apache.nutch.plugin.Extension;

/**
 * Extension point definition for gui components.
 * 
 * The nutch administration gui is builded form components. These components are
 * plugin's that provide a implementation for the GuiComponent Extension point,
 * in that meaning that they provide the required files and configuration values
 * in the plugin.xml
 * 
 * Since the nutch administration gui is a web application the implementation of
 * the gui component extension point is not the implementation of a interface
 * but a set of configuration values in the plugin.xml and some jsp or jsp
 * snippets. The jsp files and jsp snippet shsould be storedin a fulder called
 * 'jsp' inside the plugin folder. Providing the attribute jspFolder with a
 * relative path to the plugin folder wllows to overwrite this standard folder
 * name.
 * 
 * The nutch administration gui can be used to configure and manage a set of
 * nutch instances. A nutch instance is a nutch that comes with it own
 * confiuration and has it own fetch/ crawl and index data. All nutch instances
 * use the same code basis. However there are gui components that can be
 * instance independent or instance dependent. For example a component to create
 * a new instance is independent from a specific instance but a plugin that
 * reindex a segment or providng crawlDb statistics is instance dependent since
 * these data are located in the instance folder.
 * 
 * To determiniate if a component should be availabe under a instance menu or
 * under a general menu or under both the attributes "isGeneralComponent" and
 * "isInstanceComponent" can be setted to true.
 * 
 * Components are displayed as tab's in the administration gui. So each
 * component has to provide its own name. Since the gui should be
 * internationalized the tab name can by queried providing a locale.
 * 
 * There is a <code>DefaultGuiComponent</code> that can be used by default to
 * handle the basic functionality of a gui component implemtation. A component
 * only need to setup the <code>DefaultGuiComponent</code> in the plugin.xml and
 * provide the attribute "tabName". This value is used as tab name key. The tab
 * name key is looked up in a properies file following the i18n java
 * specification that has also the name of the tab name key. That each plugin
 * has a different name of the property file is important since they will
 * somehow share classloaders in the webapplication and similar names could
 * provide errors. Beside that it is possible to implement a custom GuiComponent
 * that handles the tabName and all other functionality different.
 * 
 */
public interface IGuiComponent {

  /** name of the extension point. */
  public final static String X_POINT_ID = IGuiComponent.class.getName();

  /**
   * determinate if the component should be deployed under the general section
   * (e.g. create new instance ) so it is not related to a specific instance
   */
  public static final String IS_GENERAL_COMPONENT = "isGeneralComponent";

  /**
   * determinate if the component should be deployed under a instance section
   * (e.g. start a crawl is instance dependent)
   */
  public static final String IS_INSTANCE_COMPONENT = "isInstanceComponent";

  /**
   * determinate the folder inside the plugin folder that hosts the jsp and jsp
   * snippets
   */
  public static final String JSP_FOLDER = "jspFolder";

  /**
   * determinate the tabname key that is used by
   * <code>DefaultGuiComponent</code> to recieving i18n strings for the tab
   * names.
   */
  public static final String TAB_NAME = "tabName";

  /**
   * configure the component and is called once until gui startup
   * 
   * @param extension
   *          to provide access of the plugin.xml attributes
   * @param instance
   *          to provide access to the configuration properties
   */
  void configure(Extension extension, NutchInstance instance);

  /**
   * @param locale
   * @return the internationalized name displayed in the tab
   */
  String getLabel(String key, Locale locale);

  Extension getExtension();

  NutchInstance getNutchInstance();
}
