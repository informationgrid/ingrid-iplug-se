package de.ingrid.iplug.se.nutchController;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.webapp.container.Instance;

@Service
public class NutchController {

    Map<String, NutchProcess> instances = new HashMap<String, NutchProcess>();

    /**
     * Start a process.
     * 
     * @param instance The instance the process should execute in.
     * @param process The {@link NutchProcess} to start.
     * @throws Exception
     */
    public synchronized void start(Instance instance, NutchProcess process) {

        if (instances.containsKey(instance.getName())) {
            NutchProcess p = instances.get(instance.getName());
            if (p.getStatus() == NutchProcess.STATUS.RUNNING) {
                return;
            }
        }

        instances.put(instance.getName(), process);
        process.start();
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
     * Stops a process for the specified instance.
     * 
     * @param instance
     */
    public synchronized void stop(Instance instance) {
        NutchProcess command = instances.get(instance.getName());
        if (command.getStatus() == NutchProcess.STATUS.RUNNING) {
            command.stopExecution();
        }
    }

}