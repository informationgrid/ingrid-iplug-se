/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
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


import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.ingrid.admin.Config;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.webapp.controller.instance.InstanceController;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IngridDocument;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ModelMap;

import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.ElasticSearchUtils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.controller.instance.scheduler.SchedulerManager;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListInstancesControllerTest extends Mockito {

    @Mock
    SchedulerManager manager;
    
    @Mock
    ElasticsearchNodeFactoryBean esBean;

    @Mock
    CommunicationService commInterfaceMock;

    @Mock
    IBus iBusMock;
    
    @BeforeEach
    public void initTest() throws Exception {
        SEIPlug.baseConfig = new Config();
        // JettyStarter.baseConfig.indexSearchInTypes = new ArrayList<>();
        MockitoAnnotations.initMocks( this );
        try (MockedStatic<ElasticSearchUtils> mocked = mockStatic(ElasticSearchUtils.class)) {
            mocked.when(() -> ElasticSearchUtils.typeExists(anyString(), any())).thenReturn(false);
        }
        // InternalNode node = new InternalNode();
        Settings.Builder builder = Settings.builder();
        TransportClient transportClient = new PreBuiltTransportClient(builder.build());
        Mockito.when( esBean.getClient() ).thenReturn( transportClient );
//        Mockito.when( ElasticSearchUtils.typeExists( Mockito.anyString(), (Client) Mockito.any() ) ).thenReturn( false );

        InstanceController.setCommunicationInterface(commInterfaceMock);
        when(commInterfaceMock.getIBus()).thenReturn(iBusMock);
        IngridDocument activeIndices = new IngridDocument();
        activeIndices.put("result", new HashSet<>());
        when(iBusMock.call(any())).thenReturn(activeIndices);
    }

    @Test
    public void testAddInstance() throws Exception {

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );

        Configuration configuration = new Configuration();
        configuration.setInstancesDir( "test-instances" );
        //configuration.activeInstances = Arrays.asList( "web" );
        SEIPlug.conf = configuration;

        ListInstancesController lic = new ListInstancesController(configuration);
        lic.setSchedulerManager( manager );
        // lic.setElasticSearch( esBean );
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        Principal principal = mock(Principal.class);
        Mockito.when( principal.getName() ).thenReturn( "admin" );
        Mockito.when( httpRequest.getUserPrincipal() ).thenReturn( principal );
        Mockito.when( httpRequest.isUserInRole( Mockito.anyString() ) ).thenReturn( false );
        HttpServletResponse httpResponse = mock(HttpServletResponse.class);
        lic.addInstance( new ModelMap(), "test", null, httpRequest, httpResponse );

        assertTrue( Files.exists( Paths.get( "test-instances", "test" ) ), "Instance path was not created" );
        assertTrue( Files.exists( Paths.get( "test-instances", "test", "conf" ) ), "Instance configuration path was not created" );
        assertTrue( Files.exists( Paths.get( "test-instances", "test", "conf", "nutch-default.xml" ) ), "Instance nutch configuration path was not created" );

        FileUtils.removeRecursive( Paths.get( "test-instances" ) );

    }

}
