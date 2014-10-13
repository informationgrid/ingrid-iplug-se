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
package de.ingrid.iplug.se.webapp.controller.instance.scheduler;

import java.io.IOException;

import org.apache.log4j.Logger;

import de.ingrid.iplug.se.iplug.IPostCrawlProcessor;
import de.ingrid.iplug.se.nutchController.IngridCrawlNutchProcess;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcess.STATUS;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.instance.InstanceController;

public class SchedulingRunnable implements Runnable {

    private static final Logger LOG = Logger.getLogger(SchedulingRunnable.class);

    private final CrawlDataPersistence _crawlDataPersistence;

    private final String _instanceName;

    private NutchController _nutchController;

    private IngridCrawlNutchProcess _process = null;

    private IPostCrawlProcessor[] postCrawlProcessors;

    public SchedulingRunnable(String instanceName, CrawlDataPersistence crawlDataPersistence, NutchController nutchController, IPostCrawlProcessor[] postCrawlProcessors) {
        this._crawlDataPersistence = crawlDataPersistence;
        this._instanceName = instanceName;
        this._nutchController = nutchController;
        this.postCrawlProcessors = postCrawlProcessors;
    }

    @Override
    public void run() {

        CrawlData crawlData = null;
        try {
            crawlData = _crawlDataPersistence.loadCrawlData(this._instanceName);
        } catch (Exception e) {
            LOG.error("can not load crawl data.", e);
            return;
        }
        LOG.info("try to get lock for directory: " + crawlData.getWorkingDirectory().getAbsolutePath());

        // only execute if last process has not run yet or is not running
        if (_process == null || _process.getStatus() != STATUS.RUNNING) {
            LOG.info("success.");
            LOG.info("lock the scheduled crawl: " + crawlData.getWorkingDirectory().getAbsolutePath());

            try {
                FileUtils.prepareCrawl(this._instanceName);
            } catch (IOException e) {
                LOG.error("Files could not be prepared for the crawl!");
                e.printStackTrace();
                return;
            }

            Instance instanceData = InstanceController.getInstanceData(_instanceName);
            _process = NutchProcessFactory.getIngridCrawlNutchProcess(instanceData, crawlData.getDepth(), crawlData.getTopn(), postCrawlProcessors);

            // run crawl process
            _nutchController.start(instanceData, _process);

        } else {
            LOG.info("fails...");
            LOG.info("crawl is locked: " + crawlData.getWorkingDirectory().getAbsolutePath());
        }

    }

}
