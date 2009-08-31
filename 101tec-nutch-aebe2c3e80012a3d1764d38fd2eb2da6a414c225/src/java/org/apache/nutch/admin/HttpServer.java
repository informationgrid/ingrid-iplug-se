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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.plugin.Extension;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

public class HttpServer extends Thread {

  public static final Log LOG = LogFactory.getLog(HttpServer.class);
  private final int _port;
  private Server _server = new Server();
  private Map<String, Object> _contextAttributes = new HashMap<String, Object>();

  public HttpServer(int port) {
    _port = port;
  }

  @Override
  public void run() {
    try {
      _server.start();
    } catch (Exception e) {
      LOG.error("can not start server.", e);
    }
  }

  public void startHttpServer() throws Exception {
    if (!_server.isStarted()) {
      SocketListener listener = new SocketListener();
      listener.setPort(_port);
      _server.addListener(listener);
      start();
    }
  }

  public void stopHttpServer() throws InterruptedException {
    if (_server.isStarted()) {
      _server.stop();
    }
  }

  public void addGuiComponentExtension(Extension extension,
      NutchInstance nutchInstance) throws Exception {
    IGuiComponent guiComponent = (IGuiComponent) extension
        .getExtensionInstance();
    guiComponent.configure(extension, nutchInstance);
    String pluginId = extension.getDescriptor().getPluginId();
    String contextPath = "/" + nutchInstance.getInstanceName();
    if (!pluginId.equals("admin-welcome")) {
      contextPath = contextPath + "/" + pluginId;
    }

    String webApp = new File(extension.getDescriptor().getPluginPath()
        + File.separator + "src/webapp" + File.separator).getCanonicalPath();

    LOG.info("add webapplication [" + webApp + "] with contextPath ["
        + contextPath + "] to webserver");
    WebApplicationContext context = _server.addWebApplication(contextPath,
        webApp);
    context.setClassLoader(extension.getDescriptor().getClassLoader());
    context.setAttribute("nutchInstance", nutchInstance);
    
    // add theme into view to load different css files
    String theme = System.getProperty("nutch.gui.theme", "default");
    context.setAttribute("theme", theme);
    
    
    Set<Entry<String, Object>> entrySet = _contextAttributes.entrySet();
    for (Entry<String, Object> entry : entrySet) {
      context.setAttribute(entry.getKey(), entry.getValue());
    }
    context.start();

    Set<String> contextNames = new TreeSet<String>();
    HttpContext[] contexts = _server.getContexts();
    for (HttpContext httpContext : contexts) {
      contextNames.add(httpContext.getName());
    }
    for (HttpContext httpContext : contexts) {
      httpContext.setAttribute("contextNames", contextNames);
    }

  }

  public void addContextAttribute(String key, Object value) {
    _contextAttributes.put(key, value);
  }
}
