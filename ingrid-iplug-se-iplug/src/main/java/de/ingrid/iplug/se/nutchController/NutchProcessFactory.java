/**
 * 
 */
package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
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

    private final static Log log = LogFactory.getLog(NutchProcessFactory.class);
    
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
        process.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + Paths.get(instance.getWorkingDirectory(), "logs").toAbsolutePath(), "-Dhadoop.log.file=hadoop.log" });
        process.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
        process.addClassPath(Paths.get(Paths.get(instance.getWorkingDirectory()).toAbsolutePath().getParent().getParent().toAbsolutePath().toString(), "apache-nutch-runtime/runtime/local/lib").toAbsolutePath().toString()
                .concat(File.separator).concat("*"));
        process.setStatusProvider(new StatusProvider());

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

        nutchConfigTool.write();

        return process;

    }

}
