package org.apache.nutch.admin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.nutch.plugin.Extension;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

public class HttpServer {

  private final int _port;
  private Server _server = new Server();
  private Map<String, Object> _contextAttributes = new HashMap<String, Object>();

  public HttpServer(int port) {
    _port = port;
  }

  public void startHttpServer() throws Exception {
    if (!_server.isStarted()) {
      SocketListener listener = new SocketListener();
      listener.setPort(_port);
      _server.addListener(listener);
      _server.start();
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
    String contextPath = nutchInstance.getInstanceName() + "/"
        + extension.getDescriptor().getPluginId();
    String webApp = extension.getDescriptor().getPluginPath() + File.separator
        + "src/webapp" + File.separator;
    WebApplicationContext context = _server.addWebApplication(
        contextPath, webApp);
    context.setClassLoader(extension.getDescriptor().getClassLoader());
    context.setAttributes(_contextAttributes);
    context.start();
  }

  public void addContextAttribute(String key, Object value) {
    _contextAttributes.put(key, value);
  }
}
