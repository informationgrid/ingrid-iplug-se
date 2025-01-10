/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ingrid.iplug.se.SEIPlug;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
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

    public GenericNutchProcess() {

        // if the configuration is used outside spring
        // this property could be null
        if (SEIPlug.conf.nutchCallJavaExecutable == null) {
            this.setExecutable("java");
        } else {
            this.setExecutable(SEIPlug.conf.nutchCallJavaExecutable);
        }
    }

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
        // Debug specific call
        String debugOption = System.getProperty("debugNutchCall");
        if (debugOption != null) {
            Pattern pattern = Pattern.compile(debugOption);
            Matcher matcher = pattern.matcher(commandAndOptions[0]);
            if (matcher.find()) {
                nutchCall = arrayConcat(nutchCall, new String[] { "-agentlib:jdwp=transport=dt_socket,address=7000,server=y,suspend=y" });
            }
        }
        nutchCall = arrayConcat(nutchCall, commandAndOptions);

        CommandLine cmdLine = new CommandLine(executable);
        cmdLine.addArguments(nutchCall);

        CommandResultHandler resultHandler;
        Executor executor = new DefaultExecutor();
        consoleOutput = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(consoleOutput));
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
