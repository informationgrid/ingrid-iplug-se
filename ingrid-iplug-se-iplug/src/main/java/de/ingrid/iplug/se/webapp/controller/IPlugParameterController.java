/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class IPlugParameterController extends AbstractController {

    @Autowired
    private Configuration seConfig;

    @RequestMapping(value = { "/iplug-pages/welcome.html",
            "/iplug-pages/dbParams.html" }, method = RequestMethod.GET)
    public String getParameters(
            final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject) {

        // write object into session
        modelMap.put("dataBasePath", seConfig.databaseDir);
        modelMap.put("instancePath", seConfig.getInstancesDir());
        //modelMap.put("elasticSearchPort", seConfig.esHttpPort);
        return AdminViews.DB_PARAMS;
    }

    @RequestMapping(value = "/iplug-pages/dbParams.html", method = RequestMethod.POST)
    public String post(@RequestParam("dataBasePath") String dbPath, @RequestParam("instancePath") String instancePath,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject) {

        seConfig.databaseDir = dbPath;
        seConfig.setInstancesDir( instancePath );
        //seConfig.esHttpPort = elasticSearchPort;
        
        pdCommandObject.setRankinTypes(true, false, false);
        
        return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
    }

}
