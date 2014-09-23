package de.ingrid.iplug.se.nutchController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;

import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;

public class NutchControllerTest {

    @Test
    public void test() throws InterruptedException, IOException {
        
        FileUtils.removeRecursive(Paths.get("test-instances"));
        
        Configuration configuration = new Configuration();
        configuration.setInstancesDir("test-instances");
        SEIPlug.conf = configuration;
        
        Instance instance = new Instance();
        instance.setName("test");
        instance.setWorkingDirectory(SEIPlug.conf.getInstancesDir() + "/test");

        Path conf = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "conf").toAbsolutePath();
        Path urls = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "urls").toAbsolutePath();
        Path logs = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "logs").toAbsolutePath();
        Files.createDirectories(logs);

        FileUtils.copyDirectories(Paths.get("../ingrid-iplug-se-nutch/src/test/resources/conf").toAbsolutePath(), conf);
        FileUtils.copyDirectories(Paths.get("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);
        
        IngridCrawlNutchProcess process = NutchProcessFactory.getIngridCrawlNutchProcess(instance, 1, 100);

        NutchController nutchController = new NutchController();
        nutchController.start(instance, process);
        
        Settings settings = ImmutableSettings.settingsBuilder().put("path.data", SEIPlug.conf.getInstancesDir() + "/test").build();
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().clusterName("elasticsearch").data(true).settings(settings);
        nodeBuilder = nodeBuilder.local(false);
        Node node = nodeBuilder.node();

        long start = System.currentTimeMillis();
        Thread.sleep(500);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, nutchController.getNutchProcess(instance).getStatus());
        while ((System.currentTimeMillis() - start) < 300000) {
            Thread.sleep(1000);
            if (nutchController.getNutchProcess(instance).getStatus() != NutchProcess.STATUS.RUNNING) {
                break;
            }
        }
        if (nutchController.getNutchProcess(instance).getStatus() == NutchProcess.STATUS.RUNNING) {
            node.close();
            nutchController.stop(instance);
            fail("Crawl took more than 5 min.");
        }
        assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, nutchController.getNutchProcess(instance).getStatus());
        node.close();
        
        System.out.println(nutchController.getNutchProcess(instance).getStatusProvider().toString());

        FileUtils.removeRecursive(Paths.get("test-instances"));
    }

}
