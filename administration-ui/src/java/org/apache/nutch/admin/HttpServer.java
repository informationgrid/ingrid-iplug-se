package org.apache.nutch.admin;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
      e.printStackTrace();
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
      contextPath = contextPath + "-" + pluginId;
    }

    String webApp = new File(extension.getDescriptor().getPluginPath()
        + File.separator + "src/webapp" + File.separator).getCanonicalPath();

    LOG.info("add webapplication [" + webApp + "] with contextPath ["
        + contextPath + "] to webserver");
    WebApplicationContext context = _server.addWebApplication(contextPath,
        webApp);
    context.setClassLoader(extension.getDescriptor().getClassLoader());
    context.setAttributes(_contextAttributes);
    context.start();

    HttpContext[] contexts = _server.getContexts();
    Set<String> contextNames = new HashSet<String>();
    for (HttpContext httpContext : contexts) {
      contextNames.add(httpContext.getName());
    }
    _contextAttributes.put("contextNames", contextNames);
  }

  public void addContextAttribute(String key, Object value) {
    _contextAttributes.put(key, value);
  }
}
