package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Wrapper for a generic nutch process execution. Multiple
 * nutch executable commands can be executed at once.
 * 
 */
public class GenericNutchProcess extends NutchProcess {

    private static Logger log = Logger.getLogger(GenericNutchProcess.class);

    private List<String[]> commands = new ArrayList<String[]>();

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
     * Add commands to be executed.
     * 
     * @param command
     */
    public void addCommand(String... command) {
        this.commands.add(command);
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

}