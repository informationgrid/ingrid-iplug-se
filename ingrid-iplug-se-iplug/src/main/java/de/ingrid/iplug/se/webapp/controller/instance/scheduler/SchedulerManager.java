package de.ingrid.iplug.se.webapp.controller.instance.scheduler;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.iplug.IPostCrawlProcessor;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.utils.ElasticSearchUtils;

@Service
public class SchedulerManager {
    private Map<String, Runner> scheduler = new HashMap<String, Runner>();

    private final PatternPersistence patternService;

    private CrawlDataPersistence crawlDataPers;

    private NutchController nutchController;

    @Autowired
    private IPostCrawlProcessor[] postCrawlProcessors;

    private class Runner {
        public Runner(Scheduler s, SchedulingRunnable sr) {
            this.scheduler = s;
            this.runnable = sr;
        }

        public Scheduler scheduler;

        public SchedulingRunnable runnable;

        public String runningId = null;
    }

    @Autowired
    public SchedulerManager(PatternPersistence patternPers, CrawlDataPersistence crawlDataPers, NutchController nutchController, ElasticsearchNodeFactoryBean elasticSearch) throws Exception {
        this.patternService = patternPers;
        this.crawlDataPers = crawlDataPers;
        this.nutchController = nutchController;

        // create for each instance a scheduler
        for (File instance : getInstances()) {
            String name = instance.getName();

            addInstance(name);

            // check if elastic search index is present and create it otherwise
            // this can be missing if old database has been migrated or an
            // instance dir
            // has been added manually
            Client client = elasticSearch.getObject().client();
            boolean typeExists = ElasticSearchUtils.typeExists(name, client);
            if (!typeExists) {
                ElasticSearchUtils.createIndexType(name, client);
            }

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
        SchedulingRunnable schedulerRun = new SchedulingRunnable(name, crawlDataPers, nutchController, postCrawlProcessors);
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
