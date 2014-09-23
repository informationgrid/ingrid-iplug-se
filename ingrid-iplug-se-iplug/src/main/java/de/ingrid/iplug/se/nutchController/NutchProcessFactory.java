/**
 * 
 */
package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.utils.xml.XMLUtils;
import de.ingrid.utils.xpath.XPathUtils;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * @author joachim
 * 
 */
public class NutchProcessFactory {

    @SuppressWarnings("unchecked")
    public static IngridCrawlNutchProcess getIngridCrawlNutchProcess(Instance instance, int depth, int noUrls) throws JsonSyntaxException, JsonIOException, SAXException, IOException, ParserConfigurationException, TransformerException {
        IngridCrawlNutchProcess process = new IngridCrawlNutchProcess();
        process.setDepth(depth);
        process.setNoUrls(noUrls);

        Path nutchConfPath = Paths.get(instance.getWorkingDirectory(), "conf", "nutch-site.xml");
        process.setWorkingDirectory(instance.getWorkingDirectory());
        process.addClassPath(Paths.get(instance.getWorkingDirectory(), "conf").toAbsolutePath().toString());
        process.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + Paths.get(instance.getWorkingDirectory(), "logs").toAbsolutePath(), "-Dhadoop.log.file=hadoop.log" });
        process.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
        process.addClassPath(Paths.get(Paths.get(instance.getWorkingDirectory()).toAbsolutePath().getParent().getParent().toAbsolutePath().toString(), "apache-nutch-runtime/runtime/local/lib").toAbsolutePath().toString()
                .concat(File.separator).concat("*"));
        process.setStatusProvider(new StatusProvider());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(nutchConfPath.toFile());
        document.getDocumentElement().normalize();

        XPathUtils xpath = new XPathUtils();

        // add metadata to the nutch configuration
        List<String> metadataList = new ArrayList<String>();
        String indexParseMdValue = xpath.getString(document, "//value[text()='index.parse.md']/following-sibling::value");

        if (indexParseMdValue != null) {
            metadataList.addAll(Arrays.asList(indexParseMdValue.split(",")));
        }

        String confFile = Paths.get(instance.getWorkingDirectory(), "/conf/urlMaintenance.json").toAbsolutePath().toString();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        InputStream reader = new FileInputStream(confFile);
        UrlMaintenanceSettings settings = gson.fromJson(new InputStreamReader(reader, "UTF-8"), UrlMaintenanceSettings.class);
        for (Iterator<MetaElement> iter = settings.getMetadata().iterator(); iter.hasNext();) {
            MetaElement mde = iter.next();
            if (!metadataList.contains(mde.getId())) {
                metadataList.add(mde.getId());
            }
        }

        indexParseMdValue = StringUtils.join(metadataList, ",");

        addOrUpdateProperty(document.getDocumentElement(), "index.parse.md", indexParseMdValue, "Generated metadata from the ingrid instance configuration.");
        addOrUpdateProperty(document.getDocumentElement(), "hadoop.tmp.dir", Paths.get(instance.getWorkingDirectory(), "hadoop-tmp").toAbsolutePath().toString(), "Set hadoop temp directory to the instance.");

        document.getDocumentElement().normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(nutchConfPath.toFile());
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);

        return process;

    }

    private static void addOrUpdateProperty(Element el, String name, String value, String description) {
        XPathUtils xpath = new XPathUtils();

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

}
