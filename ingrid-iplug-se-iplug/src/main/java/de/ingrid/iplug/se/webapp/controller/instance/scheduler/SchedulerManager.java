/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.webapp.controller.instance.scheduler;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.elasticsearch.IndexManager;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.iplug.IPostCrawlProcessor;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import it.sauronsoftware.cron4j.Scheduler;

@Service
public class SchedulerManager {
    private Map<String, Runner> scheduler = new HashMap<String, Runner>();

    private final PatternPersistence patternService;

    private CrawlDataPersistence crawlDataPers;

    private NutchController nutchController;

    private IPostCrawlProcessor[] postCrawlProcessors;

    private NutchProcessFactory nutchProcessFactory;
    
    private class Runner {
        public Runner(Scheduler s, SchedulingRunnable sr) {
            this.scheduler = s;
            this.runnable = sr;
        }

        public Scheduler scheduler;

        public SchedulingRunnable runnable;

        public String runningId = null;
    }

    /**
     * Initialize the Schedule Manager. The index must be created here, since there are no DocumentProducers, which would have
     * lead to index creation by IndexRunnable-class.
     * @param patternPers
     * @param crawlDataPers
     * @param nutchController
     * @param indexManager
     * @throws Exception
     */
    @Autowired
    public SchedulerManager(PatternPersistence patternPers, CrawlDataPersistence crawlDataPers, NutchController nutchController, IndexManager indexManager, NutchProcessFactory nutchProcessFactory, IPostCrawlProcessor[] postCrawlProcessors) throws Exception {
        this.patternService = patternPers;
        this.crawlDataPers = crawlDataPers;
        this.nutchController = nutchController;
        this.nutchProcessFactory = nutchProcessFactory;
        this.postCrawlProcessors = postCrawlProcessors;

        // create for each instance a scheduler
        for (File instance : getInstances()) {
            String name = instance.getName();

            addInstance(name);

            // check if elastic search index is present and create it otherwise
            // this can be missing if old database has been migrated or an
            // instance dir has been added manually
            String indexName = JettyStarter.getInstance().config.index;
            
            boolean indexExists = indexManager.indexExists( indexName );
            if (!indexExists) indexManager.createIndex( indexName );
            
            // if schedule pattern exists then schedule the scheduler
            schedule(name);

        }
    }

    private File[] getInstances() {
        File[] instanceDirs = null;

        String dir = SEIPlug.conf.getInstancesDir();
        if (Files.isDirectory(Paths.get(dir))) {
            FileFilter directoryFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            };
            File instancesDirObject = new File(dir);
            instanceDirs = instancesDirObject.listFiles(directoryFilter);
        }

        if (instanceDirs == null)
            return new File[0];

        return instanceDirs;
    }

    public void addInstance(String name) {
        SchedulingRunnable schedulerRun = new SchedulingRunnable(name, crawlDataPers, nutchController, postCrawlProcessors, nutchProcessFactory);
        Scheduler schedulerClass = new Scheduler();

        Runner runner = new Runner(schedulerClass, schedulerRun);

        scheduler.put(name, runner);
        schedulerClass.start();
    }

    public void schedule(String instanceName) {
        // get pattern if any otherwise just return
        if (!patternService.existsPattern(instanceName)) {
            return;
        }

        Pattern pattern;
        try {
            pattern = patternService.loadPattern(instanceName);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // schedule with the pattern
        Runner runner = scheduler.get(instanceName);
        if (runner.runningId == null) {
            runner.runningId = runner.scheduler.schedule(pattern.getPattern(), runner.runnable);
        } else {
            runner.scheduler.reschedule(runner.runningId, pattern.getPattern());
        }

    }

    public void deschedule(String name) {
        Runner runner = scheduler.get(name);
        runner.scheduler.deschedule(runner.runningId);
        runner.runningId = null;
    }

}
