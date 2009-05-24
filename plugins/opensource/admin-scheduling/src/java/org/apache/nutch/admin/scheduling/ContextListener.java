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
