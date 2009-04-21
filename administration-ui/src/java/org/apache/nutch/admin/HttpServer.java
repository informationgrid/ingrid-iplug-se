package org.apache.nutch.admin;

import java.io.File;

import org.apache.nutch.plugin.Extension;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;

public class HttpServer {

  private final int _port;
  private Server _server = new Server();

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
    WebApplicationContext webApplication = _server.addWebApplication(
        contextPath, webApp);
    webApplication.setClassLoader(extension.getDescriptor().getClassLoader());
    webApplication.start();
  }
}
