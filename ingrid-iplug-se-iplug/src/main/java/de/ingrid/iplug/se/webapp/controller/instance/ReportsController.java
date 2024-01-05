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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.iplug.IPostCrawlProcessor;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.utils.DBUtils;
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
public class ReportsController extends AbstractController {

	@Autowired
	public ReportsController(NutchController nutchController, IPostCrawlProcessor[] postCrawlProcessors) {}

	@RequestMapping(value = { "/iplug-pages/instanceReports.html" }, method = RequestMethod.GET)
	public String showReports(final ModelMap modelMap, @RequestParam("instance") String name, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String user = request.getUserPrincipal().getName();
        if (request.isUserInRole( "instanceAdmin" ) && !DBUtils.isAdminForInstance( user, name )) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return null;
        }

        Instance instance = InstanceController.getInstanceData(name);

		if (instance == null) {
			return redirect(AdminViews.SE_LIST_INSTANCES + ".html");
		} else {
			modelMap.put("instance", instance);
			return AdminViews.SE_INSTANCE_REPORTS;
		}
	}
	
    @ModelAttribute("statusCodes")
    public List<String[]> getStatusCodes() {
    	
    	List<String[]> statusCodes = new ArrayList<String[]>();
    	statusCodes.add(new String[] {"17", "ACCESS_DENIED"});
    	statusCodes.add(new String[] {"16", "EXCEPTION"});
    	statusCodes.add(new String[] {"11", "GONE"});
    	statusCodes.add(new String[] {"14", "NOTFOUND"});
    	statusCodes.add(new String[] {"18", "ROBOTS_DENIED"});
    	
    	return statusCodes;
    	
    }

	
	

}
