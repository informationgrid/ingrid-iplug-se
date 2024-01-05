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
package de.ingrid.iplug.se.webapp.controller.instance;

import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.iplug.se.iplug.IPostCrawlProcessor;
import de.ingrid.iplug.se.nutchController.IngridCrawlNutchProcess;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class ManagementController extends InstanceController {

    private NutchController nutchController;

    private IPostCrawlProcessor[] postCrawlProcessors;

    private final ElasticsearchNodeFactoryBean elasticSearch;

    private final IndexManager indexManager;

    private PlugDescriptionService plugDescriptionService;

    private final NutchProcessFactory nutchProcessFactory;

    @Autowired
    public ManagementController(NutchController nutchController, IPostCrawlProcessor[] postCrawlProcessors, ElasticsearchNodeFactoryBean elasticSearch, IndexManager indexManager, PlugDescriptionService pds, NutchProcessFactory nutchProcessFactory) {
        this.nutchController = nutchController;
        this.postCrawlProcessors = postCrawlProcessors;
        this.elasticSearch = elasticSearch;
        this.indexManager = indexManager;
        this.plugDescriptionService = pds;
        this.nutchProcessFactory = nutchProcessFactory;
    }

    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.GET)
    public String showManagement(final ModelMap modelMap, @RequestParam("instance") String name, HttpServletRequest request, HttpServletResponse response) {
        Instance instance = InstanceController.getInstanceData(name);

        if (instance == null) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        } else {
            if (hasNoAccessToInstance(name, request, response)) {
                return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
            }
            modelMap.put("instance", instance );
            return AdminViews.SE_INSTANCE_MANAGEMENT;
        }
    }

    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.POST, params = "start")
    public String startCrawl(@RequestParam("instance") String name, @RequestParam("depth") int depth, @RequestParam("num") int numUrls, HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        if (hasNoAccessToInstance(name, request, response)) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        }

        FileUtils.prepareCrawl( name );
        
        // configure crawl process        
        Instance instance = InstanceController.getInstanceData( name );

        IngridCrawlNutchProcess process = nutchProcessFactory.getIngridCrawlNutchProcess(instance, depth, numUrls, postCrawlProcessors, indexManager, plugDescriptionService);

        // run crawl process
        nutchController.start(instance, process);
        return redirect(AdminViews.SE_INSTANCE_MANAGEMENT + ".html?instance=" + name);
    }
    
    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.POST, params = "stop")
    public String stopCrawl(@RequestParam("instance") String name, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (hasNoAccessToInstance(name, request, response)) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        }
        
        Instance instance = InstanceController.getInstanceData( name );
        nutchController.stop( instance );
        return redirect(AdminViews.SE_INSTANCE_MANAGEMENT + ".html?instance=" + name);
    }

}
