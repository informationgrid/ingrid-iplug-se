/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nutch.admin.instance;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.ConfigurationUtil;
import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/index.html")
public class InstanceController extends NavigationSelector {

  @RequestMapping(method = RequestMethod.GET)
  public String welcome() {
    return "instance";
  }

  @ModelAttribute("createInstance")
  public CreateInstanceCommandObject referenceDataInstance() {
    return new CreateInstanceCommandObject();
  }

  @ModelAttribute("instances")
  public String[] referenceDataNames(HttpSession session) {
    ServletContext servletContext = session.getServletContext();
    ConfigurationUtil configurationUtil = (ConfigurationUtil) servletContext
            .getAttribute("configurationUtil");
    String[] allNames = configurationUtil.getAllNames();
    String[] allInstancesNames = new String[allNames.length - 1];
    int counter = 0;
    for (String name : allNames) {
      if (!name.equals("general")) {
        allInstancesNames[counter] = name;
        counter++;
      }
    }
    return allInstancesNames;
  }

  @RequestMapping(method = RequestMethod.POST)
  public String createIstance(
          @ModelAttribute("createInstance") CreateInstanceCommandObject commandObject,
          HttpSession httpSession) throws IOException {
    ServletContext servletContext = httpSession.getServletContext();
    ConfigurationUtil configurationUtil = (ConfigurationUtil) servletContext
            .getAttribute("configurationUtil");
    String folderName = commandObject.getFolderName();
    if (folderName != null && !"".equals(folderName.trim())
            && !configurationUtil.existsConfiguration(folderName)) {
      configurationUtil.createNewConfiguration(folderName);
    }
    return "redirect:index.html";
  }

}
