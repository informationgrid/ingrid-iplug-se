package org.apache.nutch.admin.instance;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.nutch.admin.ConfigurationUtil;
import org.apache.nutch.admin.NavigationSelector;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ValidationUtils;
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
    System.out.println("InstanceController.referenceDataNames()");
    ServletContext servletContext = session.getServletContext();
    ConfigurationUtil configurationUtil = (ConfigurationUtil) servletContext
        .getAttribute("configurationUtil");
    return configurationUtil.getAllNames();
  }

  @SuppressWarnings("unchecked")
  @ModelAttribute("contextNames")
  public String[] referenceDataContextName(HttpSession session) {
    Set<String> set = (Set<String>) session.getServletContext().getAttribute(
        "contextNames");
    return set.toArray(new String[set.size()]);
  }

  @RequestMapping(method = RequestMethod.POST)
  public String createIstance(
      @ModelAttribute("createInstance") CreateInstanceCommandObject commandObject,
      BindingResult bindingResult, HttpSession httpSession) throws IOException {
    ServletContext servletContext = httpSession.getServletContext();
    ConfigurationUtil configurationUtil = (ConfigurationUtil) servletContext
        .getAttribute("configurationUtil");
    String folderName = commandObject.getFolderName();

    // TODO refactore in a validator
    ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "folderName",
        "empty");
    if (!bindingResult.hasErrors()) {
      if (configurationUtil.existsConfiguration(folderName)) {
        bindingResult.rejectValue("folderName", "alreadyExists");
      }
    }

    if (bindingResult.hasErrors()) {
      return "instance";
    }

    configurationUtil.createNewConfiguration(folderName);
    return "redirect:index.html";
  }

}
