package de.ingrid.iplug.se.webapp.controller.instance;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.nutchController.IngridCrawlNutchProcess;
import de.ingrid.iplug.se.nutchController.NutchController;
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
public class ManagementController extends InstanceController {

    @Autowired
    private NutchController nutchController;

    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.GET)
    public String showManagement(final ModelMap modelMap, @RequestParam("instance") String name) {
        modelMap.put("instance", getInstanceData(name));

        return AdminViews.SE_INSTANCE_MANAGEMENT;
    }

    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.POST, params = "start")
    public String startCrawl(@RequestParam("instance") String name) throws Exception {

        Instance instance = new Instance();
        instance.setName(name);
        instance.setWorkingDirectory(SEIPlug.conf.getInstancesDir() + "/" + name);
        IngridCrawlNutchProcess process = new IngridCrawlNutchProcess();
        process.setDepth(1);
        process.setNoUrls(100);
        
        FileSystem fs = FileSystems.getDefault();
        process.addClassPath(fs.getPath(instance.getWorkingDirectory(), "conf").toAbsolutePath().toString());
        process.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + fs.getPath(instance.getWorkingDirectory(), "logs").toAbsolutePath(), "-Dhadoop.log.file=hadoop.log" });
        process.addClassPath("../../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local");
        process.addClassPathLibraryDirectory("../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local/lib");
        

        nutchController.start(instance, process);
        return redirect(AdminViews.SE_INSTANCE_MANAGEMENT + ".html?instance=" + name);
    }

}
