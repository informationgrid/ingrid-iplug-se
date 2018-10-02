/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
/**
 * 
 */
package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.StatusProviderService;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.iplug.IPostCrawlProcessor;
import de.ingrid.iplug.se.webapp.container.Instance;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Factory for generating {@link NutchProcess} instances.
 * 
 * @author joachim
 * 
 */
@Service
public class NutchProcessFactory {

    // private final static Log log =
    // LogFactory.getLog(NutchProcessFactory.class);
    
    
    private StatusProviderService statusProviderService;
    
    /**
     * @param instance
     * @param depth
     * @param noUrls
     * @param postCrawlProcessors
     * @return
     */
    @SuppressWarnings("unchecked")
    public IngridCrawlNutchProcess getIngridCrawlNutchProcess(Instance instance, int depth, int noUrls, IPostCrawlProcessor[] postCrawlProcessors) {
        IngridCrawlNutchProcess process = new IngridCrawlNutchProcess();
        
        process.setInstance(instance);

        process.setPostCrawlProcessors(postCrawlProcessors);
        process.setDepth(depth);
        process.setNoUrls(noUrls);
        
        process.setWorkingDirectory(instance.getWorkingDirectory());
        process.addClassPath(Paths.get(instance.getWorkingDirectory(), "conf").toAbsolutePath().toString());
        // add default properties
        process.addJavaOptions(new String[] { "-Dhadoop.log.dir=" + Paths.get(instance.getWorkingDirectory(), "logs").toAbsolutePath() });
        process.addJavaOptions(SEIPlug.conf.nutchCallJavaOptions.toArray(new String[] {}));
        process.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
        process.addClassPath(Paths.get("apache-nutch-runtime", "runtime", "local", "lib").toAbsolutePath().toString().concat(File.separator).concat("*"));
        process.setStatusProvider( statusProviderService.getStatusProvider( instance.getWorkingDirectory() ) );

        NutchConfigTool nutchConfigTool = new NutchConfigTool(Paths.get(instance.getWorkingDirectory(), "conf", "nutch-site.xml"));

        // add metadata to the nutch configuration
        List<String> metadataList = new ArrayList<String>();
        String indexParseMdValue = nutchConfigTool.getPropertyValue("index.parse.md");
        if (indexParseMdValue != null) {
            metadataList.addAll(Arrays.asList(indexParseMdValue.split(",")));
        }

        EntityManager em = DBManager.INSTANCE.getEntityManager();
        TypedQuery<String> query = em.createQuery("select distinct md.metaKey from Metadata md", String.class);
        List<String> resultList = query.getResultList();
        for (String md : resultList) {
            if (!metadataList.contains(md)) {
                metadataList.add(md);
            }
        }
        indexParseMdValue = StringUtils.join(metadataList, ",");

        String dependingFields = StringUtils.join( SEIPlug.conf.dependingFields.toArray(), "," );
        
        nutchConfigTool.addOrUpdateProperty("index.dependent.fields", dependingFields, "Fields (with its values) that shall be added to every indexed document depending on a given key (and value).");
        nutchConfigTool.addOrUpdateProperty("index.parse.md", indexParseMdValue, "Generated metadata from the ingrid instance configuration.");
        nutchConfigTool.addOrUpdateProperty("hadoop.tmp.dir", Paths.get(instance.getWorkingDirectory(), "hadoop-tmp").toAbsolutePath().toString(), "Set hadoop temp directory to the instance.");
        nutchConfigTool.addOrUpdateProperty("mapred.temp.dir", Paths.get(instance.getWorkingDirectory(), "hadoop-tmp").toAbsolutePath().toString(), "Set mapred temp directory to the instance.");
        nutchConfigTool.addOrUpdateProperty("mapred.local.dir", Paths.get(instance.getWorkingDirectory(), "hadoop-tmp").toAbsolutePath().toString(), "Set mapred local directory to the instance.");
        nutchConfigTool.addOrUpdateProperty("ingrid.indexer.elastic.type", instance.getName(),
                "Defines the index type of the indexed documents. The instance name will be used to be able to quickly manipulate all urls of an instance. This property only applies for the ingrid.indexer.elastic plugin.");
        nutchConfigTool.addOrUpdateProperty("elastic.cluster", instance.getClusterName(), "Default index to send documents to.");
        nutchConfigTool.addOrUpdateProperty("elastic.index", instance.getIndexName(), "Default index to send documents to.");
        nutchConfigTool.addOrUpdateProperty("elastic.port", instance.getEsTransportTcpPort(), "The port to connect to using TransportClient.");
        nutchConfigTool.addOrUpdateProperty("elastic.host", instance.getEsHttpHost(), "The hostname to send documents to using TransportClient. Either host\n" + "  and port must be defined or cluster.");

        nutchConfigTool.write();

        return process;

    }
    
    public NutchProcess getUrlTesterProcess(Instance instance, String url) { 
        GenericNutchProcess process = new GenericNutchProcess();

        process.setWorkingDirectory(instance.getWorkingDirectory());
        process.addClassPath(Paths.get(instance.getWorkingDirectory(), "conf").toAbsolutePath().toString());
        // add default properties
        process.addJavaOptions(SEIPlug.conf.nutchCallJavaOptions.toArray(new String[] {}));
        process.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
        process.addClassPath(Paths.get("apache-nutch-runtime", "runtime", "local", "lib").toAbsolutePath().toString().concat(File.separator).concat("*"));

        process.addCommand("de.ingrid.iplug.se.nutch.analysis.UrlTester", url);
        
        return process;        
    }

    public StatusProviderService getStatusProviderService() {
        return statusProviderService;
    }

    @Autowired
    public void setStatusProviderService(StatusProviderService statusProviderService) {
        this.statusProviderService = statusProviderService;
    }
    
}
