/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;

public class NutchControllerTest {

    @Test
    public void test() throws InterruptedException, IOException, JsonSyntaxException, JsonIOException, SAXException, ParserConfigurationException, TransformerException {

        Node node = null;

        try {

            FileUtils.removeRecursive(Paths.get("test-instances"));

            Configuration configuration = new Configuration();
            configuration.setInstancesDir("test-instances");
            configuration.databaseID = "iplug-se-dev";
            configuration.nutchCallJavaOptions = java.util.Arrays.asList("-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8");
            SEIPlug.conf = configuration;

            // get an entity manager instance (initializes properties in the
            // DBManager)
            EntityManagerFactory emf = null;
            // for development use the settings from the persistence.xml
            emf = Persistence.createEntityManagerFactory(configuration.databaseID);
            DBManager.INSTANCE.intialize(emf);

            Instance instance = new Instance();
            instance.setName("test");
            instance.setWorkingDirectory(SEIPlug.conf.getInstancesDir() + "/test");

            Path conf = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "conf").toAbsolutePath();
            Path urls = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "urls").toAbsolutePath();
            Path logs = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "logs").toAbsolutePath();
            Files.createDirectories(logs);

            FileUtils.copyDirectories(Paths.get("apache-nutch-runtime/runtime/local/conf").toAbsolutePath(), conf);
            
            NutchConfigTool nct = new NutchConfigTool(Paths.get(conf.toAbsolutePath().toString(), "nutch-site.xml"));
            nct.addOrUpdateProperty("elastic.port", "54346", "");
            nct.write();
            
            FileUtils.copyDirectories(Paths.get("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);
            // TODO: copy dir with metadata-mapping

            IngridCrawlNutchProcess process = NutchProcessFactory.getIngridCrawlNutchProcess(instance, 2, 10, null);

            NutchController nutchController = new NutchController();
            nutchController.start(instance, process);

            Settings settings = ImmutableSettings.settingsBuilder().put("path.data", SEIPlug.conf.getInstancesDir() + "/test").put("transport.tcp.port", 54346).put("http.port", 54347).build();
            NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().clusterName("elasticsearch").data(true).settings(settings);
            nodeBuilder = nodeBuilder.local(false);
            node = nodeBuilder.node();

            long start = System.currentTimeMillis();
            Thread.sleep(500);
            assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, nutchController.getNutchProcess(instance).getStatus());
            while ((System.currentTimeMillis() - start) < 360000) {
                Thread.sleep(1000);
                if (nutchController.getNutchProcess(instance).getStatus() != NutchProcess.STATUS.RUNNING) {
                    break;
                }
            }
            if (nutchController.getNutchProcess(instance).getStatus() == NutchProcess.STATUS.RUNNING) {
                node.close();
                nutchController.stop(instance);
                fail("Crawl took more than 6 min.");
            }
            assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, nutchController.getNutchProcess(instance).getStatus());
            node.close();

            System.out.println(nutchController.getNutchProcess(instance).getStatusProvider().toString());

            FileUtils.removeRecursive(Paths.get("test-instances"));
        } finally {
            if (node != null)
                node.close();

        }
    }

    @Test
    public void testForceStop() throws InterruptedException, IOException, JsonSyntaxException, JsonIOException, SAXException, ParserConfigurationException, TransformerException {

        FileUtils.removeRecursive(Paths.get("test-instances"));

        Configuration configuration = new Configuration();
        configuration.setInstancesDir("test-instances");
        configuration.databaseID = "iplug-se-dev";
        configuration.nutchCallJavaOptions = java.util.Arrays.asList("-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8");
        SEIPlug.conf = configuration;

        Instance instance = new Instance();
        instance.setName("test");
        instance.setWorkingDirectory(SEIPlug.conf.getInstancesDir() + "/test");

        // get an entity manager instance (initializes properties in the
        // DBManager)
        EntityManagerFactory emf = null;
        // for development use the settings from the persistence.xml
        emf = Persistence.createEntityManagerFactory(configuration.databaseID);
        DBManager.INSTANCE.intialize(emf);

        Path conf = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "conf").toAbsolutePath();
        Path urls = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "urls").toAbsolutePath();
        Path logs = Paths.get(SEIPlug.conf.getInstancesDir(), "test", "logs").toAbsolutePath();
        Files.createDirectories(logs);

        FileUtils.copyDirectories(Paths.get("apache-nutch-runtime/runtime/local/conf").toAbsolutePath(), conf);
        FileUtils.copyDirectories(Paths.get("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);
        // TODO: copy dir with metadata-mapping

        IngridCrawlNutchProcess process = NutchProcessFactory.getIngridCrawlNutchProcess(instance, 1, 100, null);

        NutchController nutchController = new NutchController();
        nutchController.start(instance, process);

        Thread.sleep(5000);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, nutchController.getNutchProcess(instance).getStatus());
        nutchController.stop(instance);
        Thread.sleep(500);
        assertEquals("Status is CANCELLED", NutchProcess.STATUS.INTERRUPTED, nutchController.getNutchProcess(instance).getStatus());

        System.out.println(nutchController.getNutchProcess(instance).getStatusProvider().toString());

        FileUtils.removeRecursive(Paths.get("test-instances"));
    }

}
