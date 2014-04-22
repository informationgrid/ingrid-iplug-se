package de.ingrid.iplug.se.urlmaintenance.importer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.urlmaintenance.PartnerProviderCommand;
import de.ingrid.iplug.se.urlmaintenance.parse.IUrlFileParser;
import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.ICatalogUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IProviderDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IStartUrlDao;
import de.ingrid.iplug.se.urlmaintenance.validation.UploadCommandValidator;
import de.ingrid.nutch.admin.NavigationSelector;

@Controller
@SessionAttributes(value = { "partnerProviderCommand", "containerCommand" })
public class ImporterController extends NavigationSelector {

  private final IProviderDao _providerDao;
  private final IStartUrlDao _startUrlDao;
  private final ICatalogUrlDao _catalogUrlDao;

    private final UploadCommandValidator _validator;

  @Autowired
    public ImporterController(final IProviderDao providerDao, final IStartUrlDao urlDao,
            final ICatalogUrlDao catalogUrlDao, final UploadCommandValidator validator) {
    _providerDao = providerDao;
    _startUrlDao = urlDao;
    _catalogUrlDao = catalogUrlDao;
    _validator = validator;
  }

  @ModelAttribute("containerCommand")
  public ContainerCommand injectContainerCommand(final HttpSession session) {
    ContainerCommand command = (ContainerCommand) session.getAttribute("containerCommand");
    if (command == null) {
      command = new ContainerCommand();
    }
    return command;
  }

  @ModelAttribute("errors")
  public Map<String, String> injectErrors() {
    return new HashMap<String, String>();
  }

  @ModelAttribute("uploadCommand")
  public UploadCommand injectUploadCommand(
      @ModelAttribute("partnerProviderCommand") final PartnerProviderCommand partnerProviderCommand) {
    final UploadCommand uploadCommand = new UploadCommand();
    uploadCommand.setProviderId(partnerProviderCommand.getProvider().getId());
    return uploadCommand;
  }

  @RequestMapping(value = "/import/importer.html", method = RequestMethod.GET)
  public String get(@RequestParam(value = "state", required = false) final String state, final ModelMap modelMap) {
    modelMap.addAttribute("state", state);
    return "import/importer";
  }

  @RequestMapping(value = "/import/importer.html", method = RequestMethod.POST)
  public String post(@ModelAttribute("uploadCommand") final UploadCommand uploadCommand, final Errors errors,
      @ModelAttribute("containerCommand") final ContainerCommand containerCommand, final HttpSession session)
      throws Exception {
    if(_validator.validate(errors).hasErrors()) {
        return "import/importer";
    }

    final ImportFactory factory = new ImportFactory(uploadCommand, _providerDao, _startUrlDao, _catalogUrlDao);
    final File file = factory.saveFile();

    final IUrlFileParser parser = factory.findFileParser();
    final IUrlValidator urlValidator = factory.findUrlValidator();
    parser.parse(file);
    containerCommand.clear();
    containerCommand.setType(uploadCommand.getType());
    while (parser.hasNext()) {
      final UrlContainer container = parser.next();
      final HashMap<String, String> errorCodes = new HashMap<String, String>();
      urlValidator.validate(container, errorCodes);
      containerCommand.addContainer(container, errorCodes);
    }

    return "redirect:/import/urls.html";
  }
}
