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
import java.io.FileFilter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.plugin.Extension;

public class GuiComponentDeployer extends Thread {

  public static enum InstanceType {
    DEFAULT, GENERAL
  }

  private static final Log LOG = LogFactory.getLog(GuiComponentDeployer.class);

  public static class ConfgurationTimerTask extends TimerTask {

    private final File _workingDir;
    private final BlockingQueue<String> _blockingQueue;
    private Set<String> _cache = new HashSet<String>();

    public ConfgurationTimerTask(File workingDir,
            BlockingQueue<String> blockingQueue) {
      _workingDir = workingDir;
      _blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
      File[] files = _workingDir.listFiles(new FileFilter() {
        @Override
        public boolean accept(File pathname) {
          File conf = new File(pathname, "conf");
          return conf.exists() && conf.isDirectory() ? true : false;
        }
      });
      if (files != null) {
        for (File file : files) {
          String name = file.getName();
          if (!_cache.contains(name)) {
            _cache.add(name);
            _blockingQueue.add(file.getName());
          }
        }
      }
    }
  }

  private final HttpServer _httpServer;
  private BlockingQueue<String> _blockingQueue = new LinkedBlockingQueue<String>();
  private final ConfigurationUtil _configurationUtil;

  public GuiComponentDeployer(HttpServer httpServer,
          ConfigurationUtil configurationUtil, File workingDirectory) {
    _httpServer = httpServer;
    _configurationUtil = configurationUtil;
    Timer timer = new Timer(true);
    timer.schedule(new ConfgurationTimerTask(workingDirectory, _blockingQueue),
            new Date(), 5000);
  }

  @Override
  public void run() {
    while (true) {
      try {
        String name = _blockingQueue.take();
        LOG.info("load configuration: " + name);
        Configuration configuration = _configurationUtil
                .loadConfiguration(name);
        InstanceType type = name.equals("general") ? InstanceType.GENERAL
                : InstanceType.DEFAULT;
        String folder = configuration.get("nutch.instance.folder");
        GuiComponentExtensionContainer instanceGuiComponentContainer = new GuiComponentExtensionContainer(
                configuration);
        List<Extension> extensions = getExtensions(type,
                instanceGuiComponentContainer);
        for (Extension extension : extensions) {
          File instanceFolder = new File(folder);
          NutchInstance nutchInstance = new NutchInstance(instanceFolder
                  .getName(), instanceFolder, configuration);
          LOG.info("deploy extension ["
                  + extension.getDescriptor().getPluginId()
                  + "] to nutch instance [" + nutchInstance.getInstanceName()
                  + "]");
          _httpServer.addGuiComponentExtension(extension, nutchInstance);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private List<Extension> getExtensions(InstanceType type,
          GuiComponentExtensionContainer instanceGuiComponentContainer) {
    List<Extension> extensions = null;
    switch (type) {
    case GENERAL:
      extensions = instanceGuiComponentContainer
              .getGeneralGuiComponentExtensions();
      break;
    default:
      extensions = instanceGuiComponentContainer
              .getInstanceGuiComponentExtensions();
      break;
    }
    return extensions;
  }
}
