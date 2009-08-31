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
package org.apache.nutch.admin.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    File defaultXml = new File(instanceFolder, "conf/nutch-default.xml");
    File siteXml = new File(instanceFolder, "conf/nutch-site.xml");
    List<ConfigurationCommand> defaultList = loadConfigurationCommands(defaultXml);
    List<ConfigurationCommand> siteList = loadConfigurationCommands(siteXml);

    // overwrite default values
    for (ConfigurationCommand defaultCommand : defaultList) {
      for (ConfigurationCommand siteCommand : siteList) {
        if (defaultCommand.getName().equals(siteCommand.getName())) {
          defaultCommand.setFinalValue(siteCommand.getValue());
        }
      }
    }

    // add properties from site.xml which is not in default.xml
    for (ConfigurationCommand configurationCommand : siteList) {
      if (!defaultList.contains(configurationCommand)) {
        ConfigurationCommand last = defaultList.get(defaultList.size() - 1);
        configurationCommand.setPosition(last.getPosition() + 1);
        configurationCommand.setFinalValue(configurationCommand.getValue());
        defaultList.add(configurationCommand);
      }
    }

    return defaultList;
  }

  @RequestMapping(method = RequestMethod.GET)
  public String configuration(Model model, HttpSession session)
      throws ParserConfigurationException, SAXException, IOException {
    return "configuration";
  }

  @RequestMapping(method = RequestMethod.POST)
  public String postConfiguration(@RequestParam("name") String name,
      @RequestParam("value") String value, HttpSession session)
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
        .getAttribute("nutchInstance");
    
    File instanceFolder = nutchInstance.getInstanceFolder();
    File file = new File(instanceFolder, "conf/nutch-site.xml");
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
        .newInstance();
    DocumentBuilder documentBuilder = documentBuilderFactory
        .newDocumentBuilder();
    Document document = documentBuilder.parse(file);
    Element documentElement = document.getDocumentElement();

    // update site.xml
    boolean update = false;
    NodeList nodeList = documentElement.getElementsByTagName("name");
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node firstChild = nodeList.item(i).getFirstChild();
      String textContent = firstChild.getTextContent();
      if (textContent.equals(name)) {
        update = true;
        NodeList valueList = documentElement.getElementsByTagName("value");
        Node valueNode = valueList.item(i);
        valueNode.getFirstChild().setTextContent(value);
        break;
      }
    }

    // create new property in site.xml
    if (!update) {
      Element propertyElement = document.createElement("property");
      Element nameElement = document.createElement("name");
      Element valueElement = document.createElement("value");
      nameElement.appendChild(document.createTextNode(name));
      valueElement.appendChild(document.createTextNode(value));
      propertyElement.appendChild(nameElement);
      propertyElement.appendChild(valueElement);
      documentElement.appendChild(propertyElement);
    }

    // write site.xml
    Source xmlSource = new DOMSource(document);
    Result result = new StreamResult(new FileOutputStream(file));
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty("indent", "yes");
    transformer.transform(xmlSource, result);
    
    // update configuration
    Configuration configuration = nutchInstance.getConfiguration();
    configuration.set(name, value);
    
    return "redirect:/index.hmtl";
  }

  private List<ConfigurationCommand> loadConfigurationCommands(File file)
      throws ParserConfigurationException, SAXException, IOException {
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
      configurationCommand.setPosition(i);
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
      if (descriptionList != null) {
        if (descriptionList.getLength() > 0) {
          if (descriptionList.item(0).getFirstChild() != null) {
            configurationCommand.setDescription(descriptionList.item(0)
                .getFirstChild().getNodeValue());
          }

        }
      }

      list.add(configurationCommand);
    }
    return list;
  }

}
