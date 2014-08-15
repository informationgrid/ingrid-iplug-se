package de.ingrid.iplug.se.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.webapp.container.Instance;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class InstanceController extends AbstractController {

    @ModelAttribute("instance")
    public Instance getInstance(@RequestParam("id") int id) throws Exception {
        Instance instance = new Instance();
        instance.setName("instance " + id);
        
        return instance;
    }
    
    @RequestMapping(value = { "/iplug-pages/instance.html" }, method = RequestMethod.GET)
    public String getParameters( final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @RequestParam("id") int id) {

        return AdminViews.SE_INSTANCE;
    }

    @RequestMapping(value = "/iplug-pages/instance.html", method = RequestMethod.POST)
    public String post( final BindingResult errors, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject) {

        

        return AdminViews.SE_LIST_INSTANCES;
    }

}
