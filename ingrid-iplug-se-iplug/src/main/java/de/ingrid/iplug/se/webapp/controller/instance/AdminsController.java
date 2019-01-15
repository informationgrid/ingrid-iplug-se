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
package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.webapp.controller.AdminViews;

/**
 * Control the instance admin page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class AdminsController extends InstanceController {

    private CommunicationService _communicationInterface;
    
    @Autowired
    public AdminsController(final CommunicationService communicationInterface) throws Exception {
        _communicationInterface = communicationInterface;
    }

    @RequestMapping(value = { AdminViews.SE_INSTANCE_ADMINS }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, @RequestParam("instance") String name, HttpServletRequest request, HttpServletResponse response) {

        if (hasNoAccessToInstance(name, request, response)) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        }

        String dir = SEIPlug.conf.getInstancesDir();
        File instanceFolder = new File(dir, name);
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

        modelMap.put("instance", InstanceController.getInstanceData(name));

        return AdminViews.SE_INSTANCE_ADMINS;
    }
    
    @ModelAttribute("filterOptions")
    public String[] getFilterOption(@RequestParam(value = "filter", required = false, defaultValue = "") String filter) {
        return filter.split(",");
        // return Arrays.asList( split );
        // for (String item : split) {
        // if (item.equals( option ))
        // return true;
        // }
        // return false;
    }
    

}
