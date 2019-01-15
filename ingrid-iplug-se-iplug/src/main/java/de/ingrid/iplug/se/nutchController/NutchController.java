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
     * Returns the nutch process for this instance if any.
     * 
     * @param instance The instance or null if not found.
     * @return
     */
    public synchronized NutchProcess getNutchProcess(Instance instance) {
        if (instances.containsKey(instance.getName())) {
            NutchProcess process = instances.get(instance.getName());
            return process;
        }
        return null;
    }
    

    /**
     * Stops a process for the specified instance.
     * 
     * @param instance
     * @throws Exception 
     */
    public synchronized void stop(Instance instance) throws Exception {
        NutchProcess command = instances.get(instance.getName());
        if (command != null && command.getStatus() == NutchProcess.STATUS.RUNNING) {
            command.stopExecution();
        } else if (command == null) { 
            // in case no command is executed at the moment
            // see "REDMINE-569" (Cannot delete failed crawl on startup.)
            StatusProvider sp = new StatusProvider(instance.getWorkingDirectory());
            IngridCrawlNutchProcessCleaner ingridCrawlNutchProcessCleaner = new IngridCrawlNutchProcessCleaner(sp);
            // cleanup crawl
            ingridCrawlNutchProcessCleaner.cleanup(new File(instance.getWorkingDirectory()).toPath());
            // clear previously set states
            sp.clear();
            sp.write();
        }
    }

}
