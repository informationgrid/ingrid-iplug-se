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
package org.apache.nutch.admin.urlupload;

import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginController extends NavigationSelector {

  @RequestMapping(value = "/login.html", method = RequestMethod.GET)
  public String login(Model model, HttpSession session) {
    Boolean secure = (Boolean) session.getAttribute("securityEnabled");
    model.addAttribute("securityEnabled", secure);
    return "login";
  }

  @RequestMapping(value = "/loginFailure.html", method = RequestMethod.GET)
  public String loginFailure() {
    return "loginFailure";
  }

  @RequestMapping(value = "/logout.html", method = RequestMethod.GET)
  public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/index.html";
  }

}
