package de.ingrid.iplug.se.nutchController;

import static org.junit.Assert.*;

import org.junit.Test;

public class NutchProcessTest {

    // @Test
    public void test() throws InterruptedException {
        NutchProcess p = new NutchProcess();
        p.setWorkingDirectory("test");
        p.addClassPath("../../ingrid-iplug-se-nutch/src/test/resources/conf");
        p.addClassPath("../../ingrid-iplug-se-nutch/build/apache-nutch-1.8/runtime/local");
        p.addClassPathLibraryDirectory("../ingrid-iplug-se-nutch/build/apache-nutch-1.8/runtime/local/lib");
        p.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=logs", "-Dhadoop.log.file=hadoop.log" });
        p.addCommand("org.apache.nutch.crawl.Injector", "crawldb", "../../ingrid-iplug-se-nutch/src/test/resources/urls");
        p.start();
        Thread.sleep(100);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, p.getStatus());
        Thread.sleep(5000);
        assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, p.getStatus());
    }

}
