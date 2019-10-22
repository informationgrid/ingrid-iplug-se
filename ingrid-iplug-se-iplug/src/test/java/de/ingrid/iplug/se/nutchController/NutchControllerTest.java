/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.utils.statusprovider.StatusProviderService;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.elasticsearch.Utils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import static de.ingrid.iplug.se.elasticsearch.Utils.elastic;
import static de.ingrid.iplug.se.elasticsearch.Utils.elasticConfig;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class NutchControllerTest {

    @Before
    public void beforeTest() throws Exception {
        FileUtils.removeRecursive(Paths.get("test-instances"));
        JettyStarter.baseConfig = new Config();
        JettyStarter.baseConfig.index = "se-test";
        // JettyStarter.baseConfig.indexSearchInTypes = new ArrayList<String>();
        // JettyStarter.baseConfig.indexSearchInTypes.add( "test" );
        // Attention: this config property is used in ElasticConfig and Config!
        // During runtime both classes will read the config file and be initialized correctly
        JettyStarter.baseConfig.communicationProxyUrl = "/ingrid-group:unit-tests";
        JettyStarter.baseConfig.communicationLocation = "conf/communication.xml";
        Utils.setupES();
    }

    @After
    public void afterTest() throws Exception {
        elastic.getClient().close();
    }

    @Test
    public void test() throws Exception {

        Configuration configuration = new Configuration();
        configuration.setInstancesDir( "test-instances" );
        configuration.databaseID = "iplug-se-dev";
        configuration.dependingFields = new ArrayList<String>();
        configuration.nutchCallJavaOptions = java.util.Arrays.asList( "-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8" );
        SEIPlug.conf = configuration;
        Properties elasticProperties = Utils.getElasticProperties();
        String elasticNetworkHost = (String) elasticProperties.get("network.host");

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

        FileUtils.copyDirectories(Paths.get("apache-nutch-runtime/runtime/local/conf").toAbsolutePath(), conf);
        
        NutchConfigTool nct = new NutchConfigTool(Paths.get(conf.toAbsolutePath().toString(), "nutch-site.xml"));
        nct.addOrUpdateProperty("elastic.host", elasticNetworkHost,"");
        nct.addOrUpdateProperty("elastic.port", "9300", "");
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
        indexManager.postConstruct();
        IngridCrawlNutchProcess process = npf.getIngridCrawlNutchProcess(instance, 2, 10, null, indexManager, new PlugDescriptionService(config));

        NutchController nutchController = new NutchController();
        nutchController.start( instance, process );

        long start = System.currentTimeMillis();
        Thread.sleep( 500 );
        assertEquals( "Status is RUNNING", NutchProcess.STATUS.RUNNING, nutchController.getNutchProcess( instance ).getStatus() );
        while ((System.currentTimeMillis() - start) < 360000) {
            Thread.sleep( 1000 );
            if (nutchController.getNutchProcess( instance ).getStatus() != NutchProcess.STATUS.RUNNING) {
                break;
            }
        }
        if (nutchController.getNutchProcess( instance ).getStatus() == NutchProcess.STATUS.RUNNING) {
            nutchController.stop( instance );
            fail( "Crawl took more than 6 min." );
        }
        assertEquals( "Status is FINISHED", NutchProcess.STATUS.FINISHED, nutchController.getNutchProcess( instance ).getStatus() );

        System.out.println( nutchController.getNutchProcess( instance ).getStatusProvider().toString() );

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );
    }

    @Test
    public void testForceStop() throws Exception {

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );

        Configuration configuration = new Configuration();
        configuration.setInstancesDir( "test-instances" );
        configuration.databaseID = "iplug-se-dev";
        configuration.dependingFields = new ArrayList<String>();
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
        indexManager.postConstruct();
        IngridCrawlNutchProcess process = npf.getIngridCrawlNutchProcess(instance, 1, 100, null, indexManager, new PlugDescriptionService(config));

        NutchController nutchController = new NutchController();
        nutchController.start( instance, process );

        Thread.sleep( 5000 );
        assertEquals( "Status is RUNNING", NutchProcess.STATUS.RUNNING, nutchController.getNutchProcess( instance ).getStatus() );
        nutchController.stop( instance );
        Thread.sleep( 500 );
        assertEquals( "Status is CANCELLED", NutchProcess.STATUS.INTERRUPTED, nutchController.getNutchProcess( instance ).getStatus() );

        System.out.println( nutchController.getNutchProcess( instance ).getStatusProvider().toString() );

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );
    }

}
