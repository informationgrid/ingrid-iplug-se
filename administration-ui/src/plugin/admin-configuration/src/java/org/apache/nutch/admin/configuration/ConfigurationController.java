package org.apache.nutch.admin.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Controller
@RequestMapping("/index.html")
public class ConfigurationController extends NavigationSelector {

  @ModelAttribute("configurationCommands")
  public List<ConfigurationCommand> referenceDataConfiguration(
      HttpSession session) throws Exception {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
        .getAttribute("nutchInstance");
    File instanceFolder = nutchInstance.getInstanceFolder();
    File file = new File(instanceFolder, "conf/nutch-default.xml");
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
        .newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory
        .newDocumentBuilder();
    Document document = documentBuilder.parse(file);
    Element documentElement = document.getDocumentElement();
    NodeList nodeList = documentElement.getElementsByTagName("property");

    List<ConfigurationCommand> list = new ArrayList<ConfigurationCommand>();
    for (int i = 0; i < nodeList.getLength(); i++) {
      ConfigurationCommand configurationCommand = new ConfigurationCommand();
      Node property = nodeList.item(i);

      NodeList nameList = ((Element) property).getElementsByTagName("name");
      configurationCommand.setName(nameList.item(0).getFirstChild()
          .getNodeValue());

      NodeList valueList = ((Element) property).getElementsByTagName("value");
      if (valueList.item(0).getFirstChild() != null) {
        configurationCommand.setValue(valueList.item(0).getFirstChild()
            .getNodeValue());
      }

      NodeList descriptionList = ((Element) property)
          .getElementsByTagName("description");
      if (descriptionList.item(0).getFirstChild() != null) {
        configurationCommand.setDescription(descriptionList.item(0)
            .getFirstChild().getNodeValue());
      }
      
      list.add(configurationCommand);

    }
    return list;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String configuration(Model model, HttpSession session)
      throws ParserConfigurationException, SAXException, IOException {
    return "configuration";
  }

}
