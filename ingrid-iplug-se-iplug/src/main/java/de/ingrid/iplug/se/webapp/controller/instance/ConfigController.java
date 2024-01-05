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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.utils.InstanceConfigurationTool;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import de.ingrid.iplug.se.webapp.controller.ConfigurationCommand;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class ConfigController extends InstanceController {

    private static Log log = LogFactory.getLog(ConfigController.class);

    @Autowired
    private Configuration seConfig;

    @RequestMapping(value = { "/iplug-pages/instanceConfig.html" }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, @RequestParam("instance") String name, HttpServletRequest request, HttpServletResponse response) {
        if (hasNoAccessToInstance( name, request, response )) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        }

        String dir = seConfig.getInstancesDir();
        File instanceFolder = new File(dir, name);
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

        modelMap.put("instance", getInstanceData(name));
        return AdminViews.SE_INSTANCE_CONFIG;
    }

    @ModelAttribute("configurationCommands")
    public List<ConfigurationCommand> referenceDataConfiguration(@RequestParam("instance") String name) throws Exception {
        String dir = seConfig.getInstancesDir();
        File instanceFolder = new File(dir);
        File defaultXml = new File(instanceFolder, name + "/conf/nutch-default.xml");
        File siteXml = new File(instanceFolder, name + "/conf/nutch-site.xml");
        List<ConfigurationCommand> defaultList = loadConfigurationCommands(defaultXml);
        List<ConfigurationCommand> siteList = loadConfigurationCommands(siteXml);

        if (defaultList == null || siteList == null)
            return null;

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

    @ModelAttribute("metaConfigJson")
    public String getMetadataConfigAsJson(@RequestParam("instance") String name) throws UnsupportedEncodingException {
        String json = null;
        InstanceConfigurationTool instanceConfig = new InstanceConfigurationTool(Paths.get(seConfig.getInstancesDir(), name, "conf", "urlMaintenance.json"));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        UrlMaintenanceSettings settings = instanceConfig.getSettings();
        json = gson.toJson(settings);

        return json;
    }

    private List<ConfigurationCommand> loadConfigurationCommands(File file) throws ParserConfigurationException {
        List<ConfigurationCommand> list = null;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        try {
            Document document = documentBuilder.parse(file);
            Element documentElement = document.getDocumentElement();
            NodeList nodeList = documentElement.getElementsByTagName("property");

            list = new ArrayList<ConfigurationCommand>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                ConfigurationCommand configurationCommand = new ConfigurationCommand();
                configurationCommand.setPosition(i);
                Node property = nodeList.item(i);

                NodeList nameList = ((Element) property).getElementsByTagName("name");
                configurationCommand.setName(nameList.item(0).getFirstChild().getNodeValue());

                NodeList valueList = ((Element) property).getElementsByTagName("value");
                if (valueList.item(0).getFirstChild() != null) {
                    configurationCommand.setValue(valueList.item(0).getFirstChild().getNodeValue());
                }

                NodeList descriptionList = ((Element) property).getElementsByTagName("description");
                if (descriptionList != null) {
                    if (descriptionList.getLength() > 0) {
                        if (descriptionList.item(0).getFirstChild() != null) {
                            configurationCommand.setDescription(descriptionList.item(0).getFirstChild().getNodeValue());
                        }

                    }
                }

                list.add(configurationCommand);
            }
        } catch (IOException e) {
            log.error("Error loading configuration command", e);
            list = null;
        } catch (SAXException e) {
            log.error("Error loading configuration command", e);
            list = null;
        }
        return list;
    }

    @RequestMapping(value = "/iplug-pages/instanceConfig.html", method = RequestMethod.POST)
    public ResponseEntity<String> postConfiguration(@RequestParam("name") String name, @RequestParam("value") String value, @RequestParam("instance") String instance, HttpSession session, HttpServletRequest request, HttpServletResponse response) throws ParserConfigurationException, SAXException,
            IOException, TransformerException {
        if (hasNoAccessToInstance( instance, request, response )) {
            return new ResponseEntity<String>("Access Denied", HttpStatus.FORBIDDEN);
        }

        String dir = seConfig.getInstancesDir();
        File instanceFolder = new File(dir);
        File file = new File(instanceFolder, instance + "/conf/nutch-site.xml");
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        Element documentElement = document.getDocumentElement();

        // update site.xml
        // if 'name' was found in nutch-site.xml then we have to update this
        // value
        // which is done here!
        boolean update = false;
        NodeList nodeList = documentElement.getElementsByTagName("name");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node firstChild = nodeList.item(i).getFirstChild();
            String textContent = firstChild.getTextContent();
            if (textContent.equals(name)) {
                update = true;
                NodeList valueList = documentElement.getElementsByTagName("value");
                Node valueNode = valueList.item(i);
                Node valueText = valueNode.getFirstChild();
                if (valueText == null) {
                    valueNode.appendChild(document.createTextNode(value));
                } else {
                    valueText.setTextContent(value);
                }
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
        // Configuration configuration = nutchInstance.getConfiguration();
        // configuration.set( name, value );

        // return "redirect:/index.hmtl";
        return new ResponseEntity<String>("Config Updated", HttpStatus.OK);
    }

}
