/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.webapp.controller;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.internal.InternalNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.ui.ModelMap;

import de.ingrid.admin.JettyStarter;
import de.ingrid.admin.service.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.ElasticSearchUtils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.controller.instance.scheduler.SchedulerManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ElasticSearchUtils.class)
public class ListInstancesControllerTest {

    @Mock
    SchedulerManager manager;
    
    @Mock 
    ElasticsearchNodeFactoryBean esBean;
    
    @Before
    public void initTest() throws Exception {
        new JettyStarter( false );
        MockitoAnnotations.initMocks( this );
        PowerMockito.mockStatic( ElasticSearchUtils.class );
        InternalNode node = new InternalNode();
        Mockito.when( esBean.getObject() ).thenReturn( node );
        Mockito.when( ElasticSearchUtils.typeExists( Mockito.anyString(), (Client) Mockito.anyObject() ) ).thenReturn( false );
    }

    @Test
    public void testAddInstance() throws Exception {

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );

        Configuration configuration = new Configuration();
        configuration.setInstancesDir( "test-instances" );
        //configuration.activeInstances = Arrays.asList( "web" );
        SEIPlug.conf = configuration;

        ListInstancesController lic = new ListInstancesController();
        lic.setSchedulerManager( manager );
        // lic.setElasticSearch( esBean );
        lic.addInstance( new ModelMap(), "test", null );

        assertTrue( "Instance path was not created", Files.exists( Paths.get( "test-instances", "test" ) ) );
        assertTrue( "Instance configuration path was not created", Files.exists( Paths.get( "test-instances", "test", "conf" ) ) );
        assertTrue( "Instance nutch configuration path was not created",
                Files.exists( Paths.get( "test-instances", "test", "conf", "nutch-default.xml" ) ) );

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );

    }

}
