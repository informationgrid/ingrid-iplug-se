package de.ingrid.iplug.se.nutchController;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;

/**
 * Tool for manipulating a nutch configuration file (nutch-site.xml).
 * 
 * 
 * @author joachim
 * 
 */
public class NutchConfigTool {

    private final static Log log = LogFactory.getLog(NutchConfigTool.class);

    private Path nutchConfig = null;

    private Document doc = null;

    private XPathUtils xpath = new XPathUtils();

    public NutchConfigTool(Path nutchConfig) {
        this.nutchConfig = nutchConfig;
    }

    /**
     * Adds or replaces a property <code>name</code> with value
     * <code>value</code> and description <code>description</code>. New nodes
     * are generated below the first property node. Does not write the
     * configuration (see {@link NutchConfigTool#write()}).
     * 
     * @param name
     * @param value
     * @param description
     */
    public void addOrUpdateProperty(String name, String value, String description) {

        if (doc == null) {
            openNutchConfig();
        }

        Element el = doc.getDocumentElement();

        if (!xpath.nodeExists(el, "//name[text()='" + name + "']/following-sibling::value")) {
            Node n = xpath.createElementFromXPathAsSibling(el, "/configuration/property");
            Node nameNode = xpath.createElementFromXPath(n, "name");
            XMLUtils.createOrReplaceTextNode(nameNode, name);
            Node valueNode = xpath.createElementFromXPath(n, "value");
            XMLUtils.createOrReplaceTextNode(valueNode, value);
            Node descriptionNode = xpath.createElementFromXPath(n, "description");
            XMLUtils.createOrReplaceTextNode(descriptionNode, description);
        } else {

            Node valueNode = xpath.getNode(el, "//name[text()='" + name + "']/following-sibling::value");
            XMLUtils.createOrReplaceTextNode(valueNode, value);

            Node descriptionNode = xpath.getNode(el, "//name[text()='" + name + "']/following-sibling::description");
            if (descriptionNode != null) {
                XMLUtils.createOrReplaceTextNode(descriptionNode, description);
            }
        }
    }

    /**
     * Gets the value of a property.
     * 
     * @param property
     *            Returns <code>null</code> if the property does not exist.
     * @return
     */
    public String getPropertyValue(String property) {
        if (doc == null) {
            openNutchConfig();
        }
        return xpath.getString(doc, "//value[text()='" + property + "']/following-sibling::value");
    }

    /**
     * Writes the configuration.
     */
    public void write() {
        doc.getDocumentElement().normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(nutchConfig.toFile());
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (TransformerException e) {
            log.error("Error writing nutch configuration: " + nutchConfig, e);
            throw new RuntimeException("Error writing nutch configuration: " + nutchConfig, e);
        }

    }

    private void openNutchConfig() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(nutchConfig.toFile());
            document.getDocumentElement().normalize();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("Error opening nutch configuration: " + nutchConfig, e);
            throw new RuntimeException("Error opening nutch configuration: " + nutchConfig, e);
        }
    }

}
