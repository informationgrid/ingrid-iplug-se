package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.webapp.container.Instance;

@Service
public class NutchController {

    Map<String, NutchProcess> instances = new HashMap<String, NutchProcess>();

    /**
     * Start a crawl of an instance.
     * The instance must contain at least the name and the working directory, so 
     * that the crawl can be started.
     * @param instance
     * @throws Exception
     */
    public synchronized void crawl(Instance instance) throws Exception {

        if (instances.containsKey(instance.getName())) {
            NutchProcess command = instances.get(instance.getName());
            if (command.getStatus() == NutchProcess.STATUS.RUNNING) {
                return;
            }
        }

        NutchProcess command = new NutchProcess();
        command.addClassPath(instance.getWorkingDirectory() + File.separator + "conf");
        command.addClassPathLibraryDirectory("build/apache-nutch-1.8/runtime/local");
        command.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=logs", "-Dhadoop.log.file=hadoop.log" });
        command.setWorkingDirectory(instance.getWorkingDirectory());
        command.addCommand("org.apache.nutch.crawl.Injector", "test/crawldb", "src/test/resources/urls");
        instances.put(instance.getName(), command);
        command.start();
    }

    /**
     * Returns the status of the nutch process for this instance.
     * 
     * @param instance
     * @return
     */
    public synchronized NutchProcess.STATUS getStatus(Instance instance) {
        if (instances.containsKey(instance.getName())) {
            NutchProcess command = instances.get(instance.getName());
            return command.getStatus();
        }
        return null;
    }

    /**
     * Stops a crawl process for the specified instance.
     * 
     * @param instance
     */
    public synchronized void stopCrawl(Instance instance) {
        NutchProcess command = instances.get(instance.getName());
        if (command.getStatus() == NutchProcess.STATUS.RUNNING) {
            command.stopExecution();
        }
    }

}