package de.ingrid.iplug.se.nutchController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;

@RunWith(PowerMockRunner.class)
// @PrepareForTest(JettyStarter.class)
public class NutchProcessTest {

    // @Mock JettyStarter jettyStarter;

    @Test
    public void testGenericNutchProcessor() throws Exception {
        FileSystem fs = FileSystems.getDefault();

        Path workingDir = fs.getPath("test").toAbsolutePath();

        if (Files.exists(workingDir)) {
            FileUtils.removeRecursive(workingDir);
        }
        Files.createDirectories(workingDir);

        Path conf = fs.getPath("test", "conf").toAbsolutePath();
        Path urls = fs.getPath("test", "urls").toAbsolutePath();
        Path logs = fs.getPath("test", "logs").toAbsolutePath();
        Files.createDirectories(logs);

        FileUtils.copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/conf").toAbsolutePath(), conf);
        FileUtils.copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);

        GenericNutchProcess p = new GenericNutchProcess();
        p.setWorkingDirectory(workingDir.toString());
        p.addClassPath(conf.toString());
        p.addClassPath("../../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local");
        p.addClassPathLibraryDirectory("../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local/lib");
        p.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + logs, "-Dhadoop.log.file=hadoop.log" });
        p.addCommand("org.apache.nutch.crawl.Injector", "crawldb", "../../ingrid-iplug-se-nutch/src/test/resources/urls/start");
        NutchController controller = new NutchController();
        Instance instance = new Instance();
        instance.setName("test");
        instance.setWorkingDirectory("test");
        controller.start(instance, p);
        Thread.sleep(500);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, p.getStatus());
        Thread.sleep(5000);
        assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, p.getStatus());
    }

    @Test
    public void testIngridCrawlNutchProcessor() throws Exception {
        FileSystem fs = FileSystems.getDefault();

        Path workingDir = fs.getPath("test").toAbsolutePath();

        if (Files.exists(workingDir)) {
            FileUtils.removeRecursive(workingDir);
        }
        Files.createDirectories(workingDir);

        Path conf = fs.getPath("test", "conf").toAbsolutePath();
        Path urls = fs.getPath("test", "urls").toAbsolutePath();
        Path logs = fs.getPath("test", "logs").toAbsolutePath();
        Files.createDirectories(logs);

        FileUtils.copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/conf").toAbsolutePath(), conf);
        FileUtils.copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);

        IngridCrawlNutchProcess p = new IngridCrawlNutchProcess();
        p.setWorkingDirectory(workingDir.toString());
        p.addClassPath(conf.toString());
        p.addClassPath("../../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local");
        p.addClassPathLibraryDirectory("../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local/lib");
        p.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + logs, "-Dhadoop.log.file=hadoop.log" });
        p.setDepth(1);
        p.setNoUrls(10);
        p.start();

        Settings settings = ImmutableSettings.settingsBuilder().put("path.data", "./").build();
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().clusterName("elasticsearch").data(true).settings(settings);
        nodeBuilder = nodeBuilder.local(false);
        Node node = nodeBuilder.node();

        long start = System.currentTimeMillis();
        Thread.sleep(500);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, p.getStatus());
        while ((System.currentTimeMillis() - start) < 300000) {
            Thread.sleep(1000);
            if (p.getStatus() != NutchProcess.STATUS.RUNNING) {
                break;
            }
        }
        if (p.getStatus() == NutchProcess.STATUS.RUNNING) {
            node.close();
            p.stopExecution();
            fail("Crawl took more than 5 min.");
        }
        assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, p.getStatus());
        node.close();
    }
}
