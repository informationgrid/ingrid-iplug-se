package de.ingrid.iplug.se.nutchController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;

public class NutchUrlTesterTest {

    @Test
    public void test() throws IOException, InterruptedException {

        FileUtils.removeRecursive(Paths.get("test-instances"));

        Configuration configuration = new Configuration();
        configuration.setInstancesDir("test-instances");
        configuration.nutchCallJavaOptions = java.util.Arrays.asList("-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8");
        SEIPlug.conf = configuration;

        Instance instance = new Instance();
        instance.setName("test");
        instance.setWorkingDirectory(SEIPlug.conf.getInstancesDir() + "/test");

        Path conf = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "conf").toAbsolutePath();
        Files.createDirectories(conf);

        FileUtils.copyDirectories(Paths.get("apache-nutch-runtime/runtime/local/conf").toAbsolutePath(), conf);

        NutchProcess process = NutchProcessFactory.getUrlTesterProcess(instance, "http://www.google.de");

        process.start();

        long start = System.currentTimeMillis();
        Thread.sleep(100);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, process.getStatus());
        while ((System.currentTimeMillis() - start) < 300000) {
            Thread.sleep(1000);
            if (process.getStatus() != NutchProcess.STATUS.RUNNING) {
                break;
            }
        }
        assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, process.getStatus());
        System.out.println(process.getConsoleOutput());
        assertTrue("Console Output is not empty", process.getConsoleOutput().length() > 0);

        FileUtils.removeRecursive(Paths.get("test-instances"));
    }

}
