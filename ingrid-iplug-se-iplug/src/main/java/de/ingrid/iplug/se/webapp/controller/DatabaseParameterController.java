package de.ingrid.iplug.se.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.SEIPlug;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class DatabaseParameterController extends AbstractController {

    @RequestMapping(value = { "/iplug-pages/welcome.html",
            "/iplug-pages/dbParams.html" }, method = RequestMethod.GET)
    public String getParameters(
            final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) {

        // write object into session
        modelMap.put("dataBasePath", "");
        modelMap.put("instancePath", SEIPlug.conf.getInstancesDir());
        modelMap.put("elasticSearchPort", SEIPlug.conf.esHttpPort);
        return AdminViews.DB_PARAMS;
    }

    @RequestMapping(value = "/iplug-pages/dbParams.html", method = RequestMethod.POST)
    public String post(@RequestParam("dataBasePath") String dbPath, @RequestParam("instancePath") String instancePath,
            @RequestParam("elasticSearchPort") String elasticSearchPort) {

        SEIPlug.conf.setInstancesDir( instancePath );
        SEIPlug.conf.esHttpPort = elasticSearchPort;
        return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
    }

}
