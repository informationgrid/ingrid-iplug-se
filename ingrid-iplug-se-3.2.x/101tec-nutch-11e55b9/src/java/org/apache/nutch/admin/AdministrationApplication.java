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

public class AdministrationApplication {

  public static void main(String[] args) throws Exception {

    if (args.length < 2) {
      System.err.println("Usage: "
              + AdministrationApplication.class.getSimpleName()
              + " <workingDirectory> <port> [--secure]");
      return;
    }
    File workingDirectory = new File(args[0]);
    int port = Integer.parseInt(args[1]);
    boolean secure = args.length > 2 ? args[2].equals("--secure") : false;
    HttpServer httpServer = new HttpServer(port, secure);
    httpServer.startHttpServer();

    ConfigurationUtil configurationUtil = new ConfigurationUtil(
            workingDirectory);
    if (!configurationUtil.existsConfiguration("general")) {
      configurationUtil.createNewConfiguration("general");
    }

    httpServer.addContextAttribute("configurationUtil", configurationUtil);

    GuiComponentDeployer componentDeployer = new GuiComponentDeployer(
            httpServer, configurationUtil, workingDirectory);
    componentDeployer.start();
  }
}
