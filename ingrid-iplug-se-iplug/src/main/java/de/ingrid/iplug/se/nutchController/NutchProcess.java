package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Wrapper for a nutch process execution. A NutchProcess can contain multiple
 * nutch executable commands.
 * 
 */
public class NutchProcess extends Thread {

    private static Logger log = Logger.getLogger(NutchProcess.class);

    public enum STATUS {
        CREATED, RUNNING, INTERRUPTED, FINISHED
    };

    private String[] classPath = new String[] {};
    private String[] javaOptions = new String[] {};

    private long timeout = ExecuteWatchdog.INFINITE_TIMEOUT;

    private String executable = "java";

    private File workingDirectory = null;

    private List<String[]> commands = new ArrayList<String[]>();

    private CommandResultHandler resultHandler = null;

    private STATUS status = STATUS.CREATED;

    @Override
    public void run() {
        status = STATUS.RUNNING;
        for (String[] command : commands) {
            if (status != STATUS.RUNNING)
                break;
            try {
                resultHandler = execute(command);
                resultHandler.waitFor();
                if (resultHandler.getExitValue() != 0) {
                    status = STATUS.INTERRUPTED;
                }
            } catch (InterruptedException e) {
                status = STATUS.INTERRUPTED;
                if (resultHandler.getWatchdog().killedProcess()) {
                    log.info("Process was killed by watchdog.");
                } else {
                    log.error("Process was unexpectably killed.", e);
                }
            } catch (IOException e) {
                status = STATUS.INTERRUPTED;
                if (resultHandler.getWatchdog().killedProcess()) {
                    log.info("Process was killed by watchdog.");
                } else {
                    log.error("Process was unexpectably killed.", e);
                }
            }
        }
        if (status == STATUS.RUNNING) {
            status = STATUS.FINISHED;
        }
    }

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
     * Add commands to be executed.
     * 
     * @param command
     */
    public void addCommand(String... command) {
        this.commands.add(command);
    }

    /**
     * Returns the status of this command chain.
     * 
     * @return
     */
    public STATUS getStatus() {
        return this.status;
    }

    private CommandResultHandler execute(String[] commandAndOptions) throws IOException {
        String cp = StringUtils.join(classPath, File.pathSeparator);

        String[] nutchCall = new String[] { "-cp", cp };
        nutchCall = arrayConcat(nutchCall, javaOptions);
        nutchCall = arrayConcat(nutchCall, commandAndOptions);

        CommandLine cmdLine = new CommandLine(executable);
        cmdLine.addArguments(nutchCall);

        CommandResultHandler resultHandler;
        Executor executor = new DefaultExecutor();
        if (workingDirectory != null) {
            executor.setWorkingDirectory(workingDirectory);
        } else {
            executor.setWorkingDirectory(new File("."));
        }
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
        executor.setWatchdog(watchdog);
        resultHandler = new CommandResultHandler(watchdog);
        if (log.isDebugEnabled()) {
            log.debug("Call: " + StringUtils.join(cmdLine.toStrings(), " "));
        }
        executor.execute(cmdLine, resultHandler);
        return resultHandler;
    }

    private <T> T[] arrayConcat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    private String[] getJarFiles(String dir) {
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

    private class CommandResultHandler extends DefaultExecuteResultHandler {

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

}
