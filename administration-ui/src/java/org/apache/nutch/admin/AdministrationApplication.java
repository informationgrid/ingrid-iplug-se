package org.apache.nutch.admin;

import java.io.File;

public class AdministrationApplication {

  public static void main(String[] args) throws Exception {

    File workingDirectory = new File(args[0]);
    int port = Integer.parseInt(args[1]);
    HttpServer httpServer = new HttpServer(port);
    httpServer.startHttpServer();

    ConfigurationUtil configurationUtil = new ConfigurationUtil(
        workingDirectory);
    if (!configurationUtil.existsConfiguration("general")) {
      configurationUtil.createNewConfiguration("general");
    }
    if (!configurationUtil.existsConfiguration("welcome")) {
      configurationUtil.createNewConfiguration("welcome");
    }
    
    httpServer.addContextAttribute("configurationUtil", configurationUtil);

    GuiComponentDeployer componentDeployer = new GuiComponentDeployer(
        httpServer, configurationUtil, workingDirectory);
    componentDeployer.start();
  }
}
