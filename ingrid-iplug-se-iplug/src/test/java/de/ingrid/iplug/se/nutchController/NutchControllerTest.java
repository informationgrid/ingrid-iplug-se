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

import de.ingrid.admin.Config;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.elasticsearch.Utils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import static de.ingrid.iplug.se.elasticsearch.Utils.elastic;
import static de.ingrid.iplug.se.elasticsearch.Utils.elasticConfig;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.fail;

public class NutchControllerTest {

    @BeforeEach
    public void beforeTest() throws Exception {
        FileUtils.removeRecursive(Paths.get("test-instances"));
        SEIPlug.baseConfig = new Config();
        SEIPlug.baseConfig.index = "se-test";
        // JettyStarter.baseConfig.indexSearchInTypes = new ArrayList<String>();
        // JettyStarter.baseConfig.indexSearchInTypes.add( "test" );
        // Attention: this config property is used in ElasticConfig and Config!
        // During runtime both classes will read the config file and be initialized correctly
        SEIPlug.baseConfig.communicationProxyUrl = "/ingrid-group:unit-tests";
        SEIPlug.baseConfig.communicationLocation = "conf/communication.xml";
        Utils.setupES();
    }

    @AfterEach
    public void afterTest() throws Exception {
        elastic.getClient().shutdown();
    }

    @Test
    public void test() throws Exception {

        Configuration configuration = new Configuration();
        configuration.setInstancesDir( "test-instances" );
        configuration.databaseID = "iplug-se-dev";
        configuration.dependingFields = new ArrayList<>();
        configuration.nutchCallJavaOptions = java.util.Arrays.asList( "-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8" );
        SEIPlug.conf = configuration;
        Properties elasticProperties = Utils.getElasticProperties();
//        String[] elasticNetworkHost = elasticProperties.get("elastic.remoteHosts").toString().split(":");
        System.out.println("Remote hosts: " + elasticProperties.get("elastic.remoteHosts"));

        // get an entity manager instance (initializes properties in the
        // DBManager)
        EntityManagerFactory emf = null;
        // for development use the settings from the persistence.xml
        emf = Persistence.createEntityManagerFactory( configuration.databaseID );
        DBManager.INSTANCE.intialize( emf );

        Instance instance = new Instance();
        instance.setName( "test" );
        instance.setWorkingDirectory( SEIPlug.conf.getInstancesDir() + "/test" );

        Path conf = Paths.get( SEIPlug.conf.getInstancesDir(), "test", "conf" ).toAbsolutePath();
        Path urls = Paths.get( SEIPlug.conf.getInstancesDir(), "test", "urls" ).toAbsolutePath();
        Path logs = Paths.get( SEIPlug.conf.getInstancesDir(), "test", "logs" ).toAbsolutePath();
        Files.createDirectories( logs );

        FileUtils.copyDirectories(Paths.get("../ingrid-iplug-se-nutch/src/test/resources/conf").toAbsolutePath(), conf);

        NutchConfigTool nct = new NutchConfigTool(Paths.get(conf.toAbsolutePath().toString(), "nutch-site.xml"));
        nct.addOrUpdateProperty("elastic.host", "elasticsearch_iplug-se_test","");
        nct.addOrUpdateProperty("elastic.port", "9200", "");
        nct.addOrUpdateProperty("elastic.cluster", "ingrid", "");
        nct.write();

        FileUtils.copyDirectories( Paths.get( "../ingrid-iplug-se-nutch/src/test/resources/urls" ).toAbsolutePath(), urls );
        // TODO: copy dir with metadata-mapping

        NutchProcessFactory npf = new NutchProcessFactory();
        npf.setStatusProviderService( new StatusProviderService() );
        npf.setElasticConfig( elasticConfig );

        Config config = new Config();
        config.plugdescriptionLocation = "conf/plugdescription.xml";
        IndexManager indexManager = new IndexManager(elastic, elasticConfig);
        indexManager.init();
        IngridCrawlNutchProcess process = npf.getIngridCrawlNutchProcess(instance, 2, 10, null, indexManager, new PlugDescriptionService(config));

        NutchController nutchController = new NutchController();
        nutchController.start( instance, process );

        long start = System.currentTimeMillis();
        Thread.sleep( 500 );
        assertThat( "Status is RUNNING", nutchController.getNutchProcess( instance ).getStatus(), is(NutchProcess.STATUS.RUNNING) );
        while ((System.currentTimeMillis() - start) < 6660000) {
            Thread.sleep( 1000 );
            if (nutchController.getNutchProcess( instance ).getStatus() != NutchProcess.STATUS.RUNNING) {
                break;
            } else {
                System.out.println(nutchController.getNutchProcess( instance ).getConsoleOutput());
            }
        }
        if (nutchController.getNutchProcess( instance ).getStatus() == NutchProcess.STATUS.RUNNING) {
            nutchController.stop( instance );
            fail( "Crawl took more than 6 min." );
        }
        assertThat( "Status is FINISHED", nutchController.getNutchProcess( instance ).getStatus(), is(NutchProcess.STATUS.FINISHED) );

        System.out.println( nutchController.getNutchProcess( instance ).getStatusProvider().toString() );

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );
    }

    @Test
    public void testForceStop() throws Exception {

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );

        Configuration configuration = new Configuration();
        configuration.setInstancesDir( "test-instances" );
        configuration.databaseID = "iplug-se-dev";
        configuration.dependingFields = new ArrayList<>();
        configuration.nutchCallJavaOptions = java.util.Arrays.asList( "-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8" );
        SEIPlug.conf = configuration;

        Instance instance = new Instance();
        instance.setName( "test" );
        instance.setWorkingDirectory( SEIPlug.conf.getInstancesDir() + "/test" );

        // get an entity manager instance (initializes properties in the
        // DBManager)
        EntityManagerFactory emf = null;
        // for development use the settings from the persistence.xml
        emf = Persistence.createEntityManagerFactory( configuration.databaseID );
        DBManager.INSTANCE.intialize( emf );

        Path conf = Paths.get( SEIPlug.conf.getInstancesDir(), "test", "conf" ).toAbsolutePath();
        Path urls = Paths.get( SEIPlug.conf.getInstancesDir(), "test", "urls" ).toAbsolutePath();
        Path logs = Paths.get( SEIPlug.conf.getInstancesDir(), "test", "logs" ).toAbsolutePath();
        Files.createDirectories( logs );

        FileUtils.copyDirectories( Paths.get( "apache-nutch-runtime/runtime/local/conf" ).toAbsolutePath(), conf );
        FileUtils.copyDirectories( Paths.get( "../ingrid-iplug-se-nutch/src/test/resources/urls" ).toAbsolutePath(), urls );
        // TODO: copy dir with metadata-mapping

        NutchProcessFactory npf = new NutchProcessFactory();
        npf.setStatusProviderService( new StatusProviderService() );
        npf.setElasticConfig( elasticConfig );

        Config config = new Config();
        config.plugdescriptionLocation = "conf/plugdescription.xml";
        IndexManager indexManager = new IndexManager(elastic, elasticConfig);
        indexManager.init();
        IngridCrawlNutchProcess process = npf.getIngridCrawlNutchProcess(instance, 1, 100, null, indexManager, new PlugDescriptionService(config));

        NutchController nutchController = new NutchController();
        nutchController.start( instance, process );

        Thread.sleep( 5000 );
        assertThat( "Status is RUNNING", nutchController.getNutchProcess( instance ).getStatus(), is(NutchProcess.STATUS.RUNNING) );
        nutchController.stop( instance );
        Thread.sleep( 500 );
        assertThat( "Status is CANCELLED", nutchController.getNutchProcess( instance ).getStatus(), is(NutchProcess.STATUS.INTERRUPTED) );

        System.out.println( nutchController.getNutchProcess( instance ).getStatusProvider().toString() );

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );
    }

}
