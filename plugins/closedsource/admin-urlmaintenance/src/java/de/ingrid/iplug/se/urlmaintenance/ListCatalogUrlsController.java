package de.ingrid.iplug.se.urlmaintenance;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/listCatalogUrls.html")
public class ListCatalogUrlsController {

  @RequestMapping(method = RequestMethod.GET)
  public String listCatalogUrls() {
    return "listCatalogUrls";
  }
}
