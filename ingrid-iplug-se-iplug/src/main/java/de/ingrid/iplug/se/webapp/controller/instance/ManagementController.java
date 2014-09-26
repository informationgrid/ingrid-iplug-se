package de.ingrid.iplug.se.webapp.controller.instance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.nutchController.IngridCrawlNutchProcess;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.AdminViews;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class ManagementController extends AbstractController {

    private NutchController nutchController;

    @Autowired
    public ManagementController(NutchController nutchController) {
        this.nutchController = nutchController;
    }

    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.GET)
    public String showManagement(final ModelMap modelMap, @RequestParam("instance") String name) {
        modelMap.put("instance", InstanceController.getInstanceData(name));

        return AdminViews.SE_INSTANCE_MANAGEMENT;
    }

    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.POST, params = "start")
    public String startCrawl(@RequestParam("instance") String name, @RequestParam("depth") int depth, @RequestParam("num") int numUrls) throws Exception {
        FileUtils.prepareCrawl( name );
        
        // configure crawl process        
        Instance instance = InstanceController.getInstanceData( name );

        IngridCrawlNutchProcess process = NutchProcessFactory.getIngridCrawlNutchProcess(instance, depth, numUrls);

        // run crawl process
        nutchController.start(instance, process);
        return redirect(AdminViews.SE_INSTANCE_MANAGEMENT + ".html?instance=" + name);
    }
    
    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.POST, params = "stop")
    public String stopCrawl(@RequestParam("instance") String name) throws Exception {
        
        Instance instance = InstanceController.getInstanceData( name );
        nutchController.stop( instance );
        return redirect(AdminViews.SE_INSTANCE_MANAGEMENT + ".html?instance=" + name);
    }

}
