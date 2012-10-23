package de.ingrid.iplug.se.urlmaintenance.web;

import java.util.Iterator;
import java.util.List;

import org.apache.nutch.admin.NavigationSelector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.DatabaseExport;
import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.ExcludeUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.LimitUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.commandObjects.StartUrlCommand;
import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "startUrlCommand" })
public class FinishAddWebUrlController extends NavigationSelector {

  private final DatabaseExport _databaseExport;

  @Autowired
  public FinishAddWebUrlController(DatabaseExport databaseExport) {
    super();
    _databaseExport = databaseExport;
  }

  @RequestMapping(value = "/web/finishWebUrl.html", method = RequestMethod.GET)
  public String finish(@ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand, @ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand) {
    List<LimitUrlCommand> limitUrls = startUrlCommand.getLimitUrlCommands();
    List<ExcludeUrlCommand> excludeUrls = startUrlCommand.getExcludeUrlCommands();
    cleanupEmptyUrls(limitUrls);
    cleanupEmptyUrls(excludeUrls);
    return "web/finishWebUrl";
  }

  @RequestMapping(value = "/web/finishWebUrl.html", method = RequestMethod.POST)
  public String postFinish(@ModelAttribute("startUrlCommand") StartUrlCommand startUrlCommand,
      @ModelAttribute("partnerProviderCommand") PartnerProviderCommand partnerProviderCommand) {
    startUrlCommand.write();
//    _databaseExport.exportWebUrls();

    return "redirect:/web/listWebUrls.html";
  }

  @SuppressWarnings("unchecked")
  private void cleanupEmptyUrls(List<? extends Url> list) {
    Iterator<Url> iterator = (Iterator<Url>) list.iterator();
    while (iterator.hasNext()) {
      Url url = (Url) iterator.next();
      if (url.getUrl() == null || url.getUrl().equals("")) {
        iterator.remove();
      }
    }
  }

}
