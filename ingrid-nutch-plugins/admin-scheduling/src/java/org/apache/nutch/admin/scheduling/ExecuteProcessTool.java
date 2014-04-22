package org.apache.nutch.admin.scheduling;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExecuteProcessTool {

    private static final Log LOG = LogFactory.getLog(ExecuteProcessTool.class);

    private static final long TIMEOUT_IN_SECONDS = 300;

    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();

    public static boolean execute(String command, String argument) {
        int returnCode = -1;
        LOG.info("Execute: " + command + " " + argument + ". Kill process after max. " + TIMEOUT_IN_SECONDS + " sec.");
        try {
            final Process process = Runtime.getRuntime().exec(command + " " + argument);
    
            try {
                returnCode = timedCall(new Callable<Integer>() {
                    public Integer call() throws Exception {
                        return new Integer(process.waitFor());
                    }
                }, TIMEOUT_IN_SECONDS, TimeUnit.SECONDS);
                LOG.info("Finished executing with exit code: " + returnCode);
            } catch (TimeoutException e) {
                LOG.error("Script execution timeout out after " + TIMEOUT_IN_SECONDS + " seconds.");
            } catch (Exception e) {
                LOG.info("Script execution terminated with errors.", e);
            } finally {
                process.destroy();
            }
        } catch (Exception e) {
            LOG.error("Error executing script: " + command, e);
        }
        return returnCode != 0 ? false : true;
    }

    private static <T> T timedCall(Callable<T> c, long timeout, TimeUnit timeUnit) throws InterruptedException,
            ExecutionException, TimeoutException {
        FutureTask<T> task = new FutureTask<T>(c);
        THREAD_POOL.execute(task);
        return task.get(timeout, timeUnit);
    }

}
