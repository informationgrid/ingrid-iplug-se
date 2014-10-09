/**
 * 
 */
package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;
import de.ingrid.iplug.se.utils.InstanceConfigurationTool;
import de.ingrid.iplug.se.webapp.container.Instance;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Factory for generating {@link NutchProcess} instances.
 * 
 * @author joachim
 * 
 */
public class NutchProcessFactory {

    //private final static Log log = LogFactory.getLog(NutchProcessFactory.class);
    
    /**
     * 
     * 
     * @param instance
     * @param depth
     * @param noUrls
     * @return
     * @throws JsonSyntaxException
     * @throws JsonIOException
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    @SuppressWarnings("unchecked")
    public static IngridCrawlNutchProcess getIngridCrawlNutchProcess(Instance instance, int depth, int noUrls) {
        IngridCrawlNutchProcess process = new IngridCrawlNutchProcess();
        process.setDepth(depth);
        process.setNoUrls(noUrls);

        process.setWorkingDirectory(instance.getWorkingDirectory());
        process.addClassPath(Paths.get(instance.getWorkingDirectory(), "conf").toAbsolutePath().toString());
        process.addJavaOptions(new String[] { "-Dhadoop.log.dir=" + Paths.get(instance.getWorkingDirectory(), "logs").toAbsolutePath(), "-Dhadoop.log.file=hadoop.log", "-Dfile.encoding=UTF-8" });
        process.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
        process.addClassPath(Paths.get("apache-nutch-runtime","runtime","local","lib").toAbsolutePath().toString()
                .concat(File.separator).concat("*"));
        process.setStatusProvider(new StatusProvider( instance.getWorkingDirectory() ));

        NutchConfigTool nutchConfigTool = new NutchConfigTool(Paths.get(instance.getWorkingDirectory(), "conf", "nutch-site.xml"));

        // add metadata to the nutch configuration
        List<String> metadataList = new ArrayList<String>();
        String indexParseMdValue = nutchConfigTool.getPropertyValue("index.parse.md");
        if (indexParseMdValue != null) {
            metadataList.addAll(Arrays.asList(indexParseMdValue.split(",")));
        }

        InstanceConfigurationTool instanceConfig = new InstanceConfigurationTool(Paths.get(instance.getWorkingDirectory(), "/conf/urlMaintenance.json")); 
        for (Iterator<MetaElement> iter = instanceConfig.getMetadata().iterator(); iter.hasNext();) {
            MetaElement mde = iter.next();
            if (!metadataList.contains(mde.getId())) {
                metadataList.add(mde.getId());
            }
        }
        indexParseMdValue = StringUtils.join(metadataList, ",");

        nutchConfigTool.addOrUpdateProperty("index.parse.md", indexParseMdValue, "Generated metadata from the ingrid instance configuration.");
        nutchConfigTool.addOrUpdateProperty("hadoop.tmp.dir", Paths.get(instance.getWorkingDirectory(), "hadoop-tmp").toAbsolutePath().toString(), "Set hadoop temp directory to the instance.");
        nutchConfigTool.addOrUpdateProperty("mapred.temp.dir", Paths.get(instance.getWorkingDirectory(), "hadoop-tmp").toAbsolutePath().toString(), "Set mapred temp directory to the instance.");
        nutchConfigTool.addOrUpdateProperty("mapred.local.dir", Paths.get(instance.getWorkingDirectory(), "hadoop-tmp").toAbsolutePath().toString(), "Set mapred local directory to the instance.");
        nutchConfigTool.addOrUpdateProperty("ingrid.indexer.elastic.type", instance.getName(), "Defines the index type of the indexed documents. The instance name will be used to be able to quickly manipulate all urls of an instance. This property only applies for the ingrid.indexer.elastic plugin.");
        nutchConfigTool.addOrUpdateProperty("elastic.index", instance.getIndexName(), "Default index to send documents to.");
        nutchConfigTool.addOrUpdateProperty("elastic.port", instance.getEsTransportTcpPort(), "The port to connect to using TransportClient.");
        nutchConfigTool.addOrUpdateProperty("elastic.host", instance.getEsHttpHost(), "The hostname to send documents to using TransportClient. Either host\n" + 
        		"  and port must be defined or cluster.");

        nutchConfigTool.write();

        return process;

    }

}
