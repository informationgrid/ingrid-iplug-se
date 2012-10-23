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
package org.apache.nutch.admin.crawl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.CrawlTool;

public class StartCrawlRunnable implements Runnable {

  private final CrawlTool _crawlTool;
  private final Integer _topN;
  private final Integer _depth;
  private static final Log LOG = LogFactory.getLog(StartCrawlRunnable.class);

  public StartCrawlRunnable(final CrawlTool crawlTool, Integer topN,
          Integer depth) {
    _crawlTool = crawlTool;
    _topN = topN;
    _depth = depth;
  }

  @Override
  public void run() {
    FileSystem fileSystem = _crawlTool.getFileSystem();
    Path crawlDir = _crawlTool.getCrawlDir();
    Path lockPath = new Path(crawlDir, "crawl.running");
    Path urlPath = new Path(crawlDir, "urls/start/urls.txt");
    boolean alreadyRunning = false;
    try {
      alreadyRunning = fileSystem.exists(lockPath);
      if (!alreadyRunning) {
        fileSystem.createNewFile(lockPath);
        _crawlTool.preCrawl();
        // if there's any url to fetch then start crawling
        if (fileSystem.exists(urlPath))
            _crawlTool.crawl(_topN, _depth);
        else
            LOG.warn("It seems that there are no URLs to crawl!");
      } else {
          LOG.warn("crawl is already running");
      }
    } catch (IOException e) {
      LOG.warn("can not start crawl.", e);
    } finally {
      if (!alreadyRunning) {
        try {
          fileSystem.delete(lockPath, false);
        } catch (Throwable e) {
          LOG.warn("Can not delete lock file.", e);
        }
      }
    }
  }

}
