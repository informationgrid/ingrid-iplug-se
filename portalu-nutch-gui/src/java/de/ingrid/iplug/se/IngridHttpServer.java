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
package de.ingrid.iplug.se;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.nutch.plugin.Extension;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.webapp.WebAppContext;

import de.ingrid.iplug.se.security.ShibbolethRealm;
import de.ingrid.nutch.admin.HttpServer;
import de.ingrid.nutch.admin.IGuiComponent;
import de.ingrid.nutch.admin.NutchInstance;

public class IngridHttpServer extends HttpServer {

    public static final Log LOG = LogFactory.getLog(IngridHttpServer.class);

    private final int _port;

    private Server _server = null;

    private Map<String, Object> _contextAttributes = new HashMap<String, Object>();

    private final boolean _secure;

    public IngridHttpServer(int port, boolean secure) {
        super(port, secure);
        _port = port;
        _secure = secure;
    }

    @Override
    public void run() {
        try {
            _server.start();
        } catch (Exception e) {
            LOG.error("can not start server.", e);
        }
    }

    @Override
    public void startHttpServer() throws Exception {
        if (_server == null) {
            _server = new Server(_port);
            _server.setUserRealms(new UserRealm[]{new ShibbolethRealm(_secure)});
            _server.setSessionIdManager(new HashSessionIdManager());
            _server.start();
        }
    }

    @Override
    public void stopHttpServer() throws InterruptedException {
        if (_server.isStarted()) {
            try {
                _server.stop();
            } catch (Exception e) {
                throw new InterruptedException();
            }
        }
    }

    @Override
    public void addGuiComponentExtension(Extension extension, NutchInstance nutchInstance) throws Exception {
        IGuiComponent guiComponent = (IGuiComponent) extension.getExtensionInstance();
        guiComponent.configure(extension, nutchInstance);
        String pluginId = extension.getDescriptor().getPluginId();
        String contextPath = "/" + nutchInstance.getInstanceName();
        if (!pluginId.equals("admin-welcome")) {
            // if (pluginId.equals("admin-urlmaintenance")) {
            // contextPath = "/" + pluginId;
            // } else
            contextPath = contextPath + "/" + pluginId;
        }

        String webApp = new File(extension.getDescriptor().getPluginPath() + File.separator + "src/webapp"
                + File.separator).getCanonicalPath();

        LOG.info("add webapplication [" + webApp + "] with contextPath [" + contextPath + "] to webserver");
        WebAppContext context = new WebAppContext();
        context.setContextPath(contextPath);
        context.setWar(webApp);        
        _server.setHandler(context);
        
        context.setClassLoader(extension.getDescriptor().getClassLoader());
        context.setAttribute("nutchInstance", nutchInstance);

        // add theme into view to load different css files
        String theme = System.getProperty("nutch.gui.theme", "default");
        String title = System.getProperty("nutch.gui.title", "iPlug-SE");
        context.setAttribute("theme", theme);
        context.setAttribute("title", title);
        context.setAttribute("securityEnabled", _secure);
//        ((HashSessionManager) context.getServletHandler().getSessionManager()).setCrossContextSessionIDs(true);

        Set<Entry<String, Object>> entrySet = _contextAttributes.entrySet();
        for (Entry<String, Object> entry : entrySet) {
            context.setAttribute(entry.getKey(), entry.getValue());
        }
        context.start();

        Set<String> contextNames = new TreeSet<String>();
        Handler[] contexts = _server.getHandlers();
        for (Handler httpContext : contexts) {
            contextNames.add(((WebAppContext)httpContext).getDisplayName());
        }
        for (Handler httpContext : contexts) {
            ((WebAppContext)httpContext).setAttribute("contextNames", contextNames);
        }

    }

    @Override
    public void addContextAttribute(String key, Object value) {
        _contextAttributes.put(key, value);
    }
}
