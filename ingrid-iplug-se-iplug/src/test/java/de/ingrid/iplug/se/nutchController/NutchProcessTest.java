/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
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

import static de.ingrid.iplug.se.elasticsearch.Utils.elastic;
import static de.ingrid.iplug.se.elasticsearch.Utils.elasticConfig;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import de.ingrid.admin.Config;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.iplug.se.elasticsearch.Utils;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.utils.statusprovider.StatusProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.fail;

public class NutchProcessTest {

    // @Mock JettyStarter jettyStarter;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        SEIPlug.baseConfig = new Config();
        SEIPlug.baseConfig.index = "test";
//        JettyStarter.baseConfig.indexWithAutoId = true;
        // JettyStarter.baseConfig.indexSearchInTypes = new ArrayList<>();
        SEIPlug.baseConfig.communicationProxyUrl = "/ingrid-group:unit-tests";
        Utils.setupES();
    }

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
        p.setStatusProvider(new StatusProvider(workingDir.toString()));
        p.setWorkingDirectory(workingDir.toString());
        p.addClassPath(conf.toString());
        p.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
        p.addClassPath(Paths.get("apache-nutch-runtime/runtime/local/lib/*").toAbsolutePath().toString());
        p.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + logs, "-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8" });
        p.addCommand("org.apache.nutch.crawl.Injector", "crawldb", Paths.get("../ingrid-iplug-se-nutch/src/test/resources/urls/start").toAbsolutePath().toString());
        NutchController controller = new NutchController();
        Instance instance = new Instance();
        instance.setName("test");
        instance.setWorkingDirectory("test");
        controller.start(instance, p);
        Thread.sleep(500);
        assertThat("Status is RUNNING", p.getStatus(), is(NutchProcess.STATUS.RUNNING));
        Thread.sleep(5000);
        assertThat("Status is FINISHED", p.getStatus(), is(NutchProcess.STATUS.FINISHED));
    }

    @Test
    public void testIngridCrawlNutchProcessor() throws Exception {

        Node node = null;

        try {

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

            Configuration configuration = new Configuration();
            configuration.setInstancesDir(".");
            configuration.databaseID = "iplug-se-dev";
            configuration.nutchCallJavaOptions = java.util.Arrays.asList("-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8");
            SEIPlug.conf = configuration;
            Properties elasticProperties = Utils.getElasticProperties();
            String elasticNetworkHost = (String) elasticProperties.get("network.host");

            // get an entity manager instance (initializes properties in the
            // DBManager)
            EntityManagerFactory emf = null;
            // for development use the settings from the persistence.xml
            emf = Persistence.createEntityManagerFactory(configuration.databaseID);
            DBManager.INSTANCE.intialize(emf);

            FileUtils.copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/conf").toAbsolutePath(), conf);

            NutchConfigTool nct = new NutchConfigTool(Paths.get(conf.toAbsolutePath().toString(), "nutch-site.xml"));
            nct.addOrUpdateProperty("elastic.host", elasticNetworkHost,"");
            nct.addOrUpdateProperty("elastic.port", "9300", "");
            nct.addOrUpdateProperty("elastic.cluster", "ingrid", "");
            nct.write();

            FileUtils.copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);

            Config config = new Config();
            config.plugdescriptionLocation = "conf/plugdescription.xml";
            IndexManager indexManager = new IndexManager(elastic, elasticConfig);
            indexManager.postConstruct();
            IngridCrawlNutchProcess p = new IngridCrawlNutchProcess(indexManager, new PlugDescriptionService(config));
            p.setWorkingDirectory(workingDir.toString());

            Instance instance = new Instance();
            instance.setWorkingDirectory(workingDir.toString());
            instance.setName("test");

            p.setInstance(instance);
            p.addClassPath(conf.toString());
            p.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
            p.addClassPath(Paths.get("apache-nutch-runtime/runtime/local/lib/*").toAbsolutePath().toString());
            p.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + logs, "-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8" });
            p.setDepth(1);
            p.setNoUrls(10);
            p.setStatusProvider(new StatusProvider(workingDir.toString()));
            p.start();


            Settings settings = Settings.builder()
                    .put("path.data", SEIPlug.conf.getInstancesDir() + "/test")
                    .put("transport.tcp.port", 9300)
                    .put("cluster.name", "ingrid")
                    .put("network.host", elasticNetworkHost)
                    .put("http.port", 9200).build();
            TransportClient transportClient = new PreBuiltTransportClient(settings);

            long start = System.currentTimeMillis();
            Thread.sleep(500);
            assertThat("Status is RUNNING", p.getStatus(), is(NutchProcess.STATUS.RUNNING));
            while ((System.currentTimeMillis() - start) < 600000) {
                Thread.sleep(1000);
                if (p.getStatus() != NutchProcess.STATUS.RUNNING) {
                    break;
                }
            }
            if (p.getStatus() == NutchProcess.STATUS.RUNNING) {
                p.stopExecution();
                fail("Crawl took more than 10 min.");
            }
            assertThat("Status is FINISHED", p.getStatus(), is(NutchProcess.STATUS.FINISHED));

        } finally {
            if (node != null)
                node.close();

        }
    }
}
