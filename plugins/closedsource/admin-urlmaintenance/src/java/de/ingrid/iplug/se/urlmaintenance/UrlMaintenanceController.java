package de.ingrid.iplug.se.urlmaintenance;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UrlMaintenanceController extends NavigationSelector {

  @RequestMapping(value = "/index.html", method = RequestMethod.GET)
  public String urlMaintenance() {
    return "urlmaintenance";
  }
}
