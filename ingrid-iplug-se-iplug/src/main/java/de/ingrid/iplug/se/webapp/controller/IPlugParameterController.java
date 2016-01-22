/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2016 wemove digital solutions GmbH
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
public class IPlugParameterController extends AbstractController {

    @RequestMapping(value = { "/iplug-pages/welcome.html",
            "/iplug-pages/dbParams.html" }, method = RequestMethod.GET)
    public String getParameters(
            final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) {

        // write object into session
        modelMap.put("dataBasePath", SEIPlug.conf.databaseDir);
        modelMap.put("instancePath", SEIPlug.conf.getInstancesDir());
        //modelMap.put("elasticSearchPort", SEIPlug.conf.esHttpPort);
        return AdminViews.DB_PARAMS;
    }

    @RequestMapping(value = "/iplug-pages/dbParams.html", method = RequestMethod.POST)
    public String post(@RequestParam("dataBasePath") String dbPath, @RequestParam("instancePath") String instancePath,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject) {

        SEIPlug.conf.databaseDir = dbPath;
        SEIPlug.conf.setInstancesDir( instancePath );
        //SEIPlug.conf.esHttpPort = elasticSearchPort;
        
        pdCommandObject.setRankinTypes(true, false, false);
        
        return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
    }

}
