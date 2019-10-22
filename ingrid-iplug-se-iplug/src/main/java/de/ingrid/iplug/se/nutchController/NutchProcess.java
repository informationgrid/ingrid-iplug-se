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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.log4j.Logger;

import de.ingrid.utils.statusprovider.StatusProvider;
import de.ingrid.utils.statusprovider.StatusProvider.Classification;

/**
 * Wrapper for a nutch process execution. A NutchProcess can contain multiple
 * nutch executable commands.
 * 
 */
public abstract class NutchProcess extends Thread {

    public static enum STATES {
        ABORT, ERROR;
    };

    private static Logger log = Logger.getLogger(NutchProcess.class);

    public enum STATUS {
        CREATED, RUNNING, INTERRUPTED, FINISHED
    };

    String[] classPath = new String[] {};
    String[] javaOptions = new String[] {};

    long timeout = ExecuteWatchdog.INFINITE_TIMEOUT;

    String executable = "java";

    File workingDirectory = null;

    CommandResultHandler resultHandler = null;

    STATUS status = STATUS.CREATED;

    StatusProvider statusProvider = null;

    ByteArrayOutputStream consoleOutput = null;

    /**
     * Stops the execution of the commands and kills the process
     * 
     * @throws InterruptedException
     * 
     */
    public void stopExecution() {
        if (status == STATUS.RUNNING) {
            status = STATUS.INTERRUPTED;
            if (resultHandler != null && !resultHandler.hasResult()) {
                this.statusProvider.addState(STATES.ABORT.name(), "The process has been aborted by a user", Classification.WARN);
                resultHandler.getWatchdog().destroyProcess();
                try {
                    resultHandler.waitFor(60 * 1000);
                } catch (InterruptedException e) {
                    log.error("Could not end process after 60 sec. CAUTION ZOMBIE PROCESS!!");
                }
            }
        }
        log.info("Process execution stopped.");
    }

    /**
     * Add class path.
     * 
     * @param cp
     */
    public void addClassPath(String cp) {
        classPath = arrayConcat(classPath, new String[] { cp });
    }

    /**
     * Add all libraries in a directory to the class path.
     * 
     * @param dir
     */
    public void addClassPathLibraryDirectory(String dir) {
        String[] libs = getJarFiles(dir);
        classPath = arrayConcat(classPath, libs);
    }

    /**
     * Set the timeout for the process in ms. Per default, no timeout is set.
     * 
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Set the executable of this process (defaults to 'java').
     * 
     * @param executable
     */
    public void setExecutable(String executable) {
        this.executable = executable;
    }

    /**
     * Set the working directory, defaults to '.'.
     * 
     * @param dir
     */
    public void setWorkingDirectory(String dir) {
        this.workingDirectory = new File(dir);
    }

    /**
     * Add JAVA options.
     * 
     * @param opt
     */
    public void addJavaOptions(String[] opt) {
        javaOptions = arrayConcat(javaOptions, opt);
    }

    /**
     * Returns the status of this command chain.
     * 
     * @return
     */
    public STATUS getStatus() {
        return this.status;
    }

    <T> T[] arrayConcat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    String[] getJarFiles(String dir) {
        File file = new File(dir);
        String[] files = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                File f = new File(current, name);
                return !f.isDirectory() && name.endsWith(".jar");
            }
        });

        for (int i = 0; i < files.length; i++) {
            files[i] = (new File(dir, files[i])).getAbsolutePath();
        }

        return files;
    }

    class CommandResultHandler extends DefaultExecuteResultHandler {

        private ExecuteWatchdog watchdog;

        public CommandResultHandler(final ExecuteWatchdog watchdog) {
            this.watchdog = watchdog;
        }

        @Override
        public void onProcessComplete(final int exitValue) {
            super.onProcessComplete(exitValue);
            log.info("The Process executed successfully.");
        }

        @Override
        public void onProcessFailed(final ExecuteException e) {
            super.onProcessFailed(e);
            if (watchdog != null && watchdog.killedProcess()) {
                System.err.println("The process timed out");
            } else {
                System.err.println("The process failed: " + e.getMessage());
            }
        }

        public ExecuteWatchdog getWatchdog() {
            return this.watchdog;
        }
    }

    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }

    public StatusProvider getStatusProvider() {
        return this.statusProvider;
    }

    public String getConsoleOutput() {
        if (consoleOutput != null) {
            return consoleOutput.toString();
        } else {
            return "";
        }
    }

}
