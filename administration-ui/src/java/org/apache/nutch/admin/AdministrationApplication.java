package org.apache.nutch.admin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Extension;

public class AdministrationApplication {

  public static void main(String[] args) throws Exception {

    File workingDirectory = new File(args[0]);
    int port = Integer.parseInt(args[1]);
    HttpServer httpServer = new HttpServer(port);
    httpServer.startHttpServer();

    ConfigurationUtil configurationUtil = new ConfigurationUtil(
        workingDirectory);

    Configuration generalConfiguration = null;
    if (configurationUtil.existsConfiguration("general")) {
      generalConfiguration = configurationUtil.loadConfiguration("general");
    } else {
      generalConfiguration = configurationUtil
          .createNewConfiguration("general");
    }
    List<Configuration> configurations = new ArrayList<Configuration>();
    configurations.add(generalConfiguration);
    addGuiComponent(httpServer, configurations, true);
    configurations = configurationUtil.loadAll("general");
    addGuiComponent(httpServer, configurations, false);

  }

  private static void addGuiComponent(HttpServer httpServer,
      List<Configuration> configurations, boolean general) throws Exception {
    for (Configuration configuration : configurations) {
      String folder = configuration.get("nutch.instance.folder");
      GuiComponentExtensionContainer instanceGuiComponentContainer = new GuiComponentExtensionContainer(
          configuration);
      List<Extension> extensions = general ? instanceGuiComponentContainer
          .getGeneralGuiComponentExtensions() : instanceGuiComponentContainer
          .getInstanceGuiComponentExtensions();
      for (Extension extension : extensions) {
        File instanceFolder = new File(folder);
        NutchInstance nutchInstance = new NutchInstance(instanceFolder
            .getName(), instanceFolder, configuration);
        httpServer.addGuiComponentExtension(extension, nutchInstance);
      }
    }
  }
}
