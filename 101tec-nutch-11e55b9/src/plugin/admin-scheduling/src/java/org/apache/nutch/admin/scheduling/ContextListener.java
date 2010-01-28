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
package org.apache.nutch.admin.scheduling;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.nutch.admin.NutchInstance;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ContextListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent arg0) {

  }

  @Override
  public void contextInitialized(ServletContextEvent contextEvent) {
    NutchInstance nutchInstance = (NutchInstance) contextEvent
        .getServletContext().getAttribute("nutchInstance");
    WebApplicationContext webApplicationContext = WebApplicationContextUtils
        .getWebApplicationContext(contextEvent.getServletContext());
    PatternPersistence patternPersistence = (PatternPersistence) webApplicationContext
        .getBean("patternPersistence");
    CrawlDataPersistence crawlDataPersistence = (CrawlDataPersistence) webApplicationContext
        .getBean("crawlDataPersistence");

    try {
      patternPersistence.setWorkingDirectory(nutchInstance.getInstanceFolder());
      crawlDataPersistence.setWorkingDirectory(nutchInstance
          .getInstanceFolder());
    } catch (Exception e) {
      throw new RuntimeException("can not load pattern.", e);
    }
  }

}
