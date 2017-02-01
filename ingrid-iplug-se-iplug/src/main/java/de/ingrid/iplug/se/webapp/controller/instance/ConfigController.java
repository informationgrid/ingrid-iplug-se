/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.utils.InstanceConfigurationTool;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import de.ingrid.iplug.se.webapp.controller.ConfigurationCommand;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class ConfigController extends InstanceController {

    @RequestMapping(value = { "/iplug-pages/instanceConfig.html" }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, @RequestParam("instance") String name) {

        String dir = SEIPlug.conf.getInstancesDir();
        File instanceFolder = new File(dir, name);
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

        modelMap.put("instance", getInstanceData(name));
        return AdminViews.SE_INSTANCE_CONFIG;
    }

    @ModelAttribute("configurationCommands")
    public List<ConfigurationCommand> referenceDataConfiguration(@RequestParam("instance") String name) throws Exception {
        String dir = SEIPlug.conf.getInstancesDir();
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
        InstanceConfigurationTool instanceConfig = new InstanceConfigurationTool(Paths.get(SEIPlug.conf.getInstancesDir(), name, "conf", "urlMaintenance.json"));

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        UrlMaintenanceSettings settings = instanceConfig.getSettings();
        json = gson.toJson(settings);

        return json;
    }

    private List<ConfigurationCommand> loadConfigurationCommands(File file) throws ParserConfigurationException {
        List<ConfigurationCommand> list = null;
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
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
            e.printStackTrace();
            list = null;
        } catch (SAXException e) {
            e.printStackTrace();
            list = null;
        }
        return list;
    }

    @RequestMapping(value = "/iplug-pages/instanceConfig.html", method = RequestMethod.POST)
    public ResponseEntity<String> postConfiguration(@RequestParam("name") String name, @RequestParam("value") String value, @RequestParam("instance") String instance, HttpSession session) throws ParserConfigurationException, SAXException,
            IOException, TransformerException {

        String dir = SEIPlug.conf.getInstancesDir();
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

    @RequestMapping(value = "/rest/updateMetadata", method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> updateMetadataConfig(@RequestParam("instance") String name, @RequestBody String json) throws IOException {
        String confFile = SEIPlug.conf.getInstancesDir() + "/" + name + "/conf/urlMaintenance.json";

        // check if json can be converted correctly
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        UrlMaintenanceSettings settings = gson.fromJson(json, UrlMaintenanceSettings.class);

        Map<String, String> result = new HashMap<String, String>();
        // only write then json content to file
        if (settings != null) {
            // File fos = new File( SEIPlug.conf.getInstancesDir() + "/" + name
            // + "/conf/urlMaintenance.json" );
            // BufferedWriter writer = new BufferedWriter( new FileWriter( fos )
            // );
            // writer.write( json );
            // writer.close();
            Writer out = new FileWriter(confFile);
            gson.toJson(settings, out);
            out.close();
            result.put("result", "OK");
            return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
        }
        result.put("result", "Error");
        return new ResponseEntity<Map<String, String>>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
