package de.ingrid.iplug.se.webapp.controller.instance;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.UrlHandler;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.nutchController.IngridCrawlNutchProcess;
import de.ingrid.iplug.se.nutchController.NutchController;
import de.ingrid.iplug.se.nutchController.NutchProcessFactory;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class ManagementController extends InstanceController {

    private NutchController nutchController;

    @Autowired
    public ManagementController(NutchController nutchController) {
        this.nutchController = nutchController;
    }

    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.GET)
    public String showManagement(final ModelMap modelMap, @RequestParam("instance") String name) {
        modelMap.put("instance", getInstanceData(name));

        return AdminViews.SE_INSTANCE_MANAGEMENT;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = { "/iplug-pages/instanceManagement.html" }, method = RequestMethod.POST, params = "start")
    public String startCrawl(@RequestParam("instance") String name) throws Exception {
        String workDir = SEIPlug.conf.getInstancesDir() + "/" + name;

        // get all urls belonging to the given instance
        List<Url> urls = UrlHandler.getUrlsByInstance( name );
        Map<String, Map<String, List<String>>> startUrls = new HashMap<String, Map<String, List<String>>>();
        List<String> limitUrls = new ArrayList<String>(); 
        List<String> excludeUrls = new ArrayList<String>();
        
        for (Url url : urls) {
            
            Map<String, List<String>> metadata = new HashMap<String, List<String>>();
            for (Metadata meta : url.getMetadata()) {
                List<String> metaValues = metadata.get( meta.getMetaKey() );
                if (metaValues == null) {
                    metaValues = new ArrayList<String>();
                    metadata.put( meta.getMetaKey(), metaValues );
                }
                metaValues.add( meta.getMetaValue() ) ;
            }
            
            startUrls.put( url.getUrl(), metadata );
            for (String limit : url.getLimitUrls()) {
                limitUrls.add( limit );
            }
            for (String exclude : url.getExcludeUrls()) {
                excludeUrls.add( exclude );
            }
        }
        
        // output urls and metadata
        String[] startUrlsValue = startUrls.keySet().toArray( new String[0] );
        FileUtils.writeToFile( Paths.get( workDir, "urls", "start" ).toAbsolutePath(), "seed.txt", Arrays.asList( startUrlsValue ));
        
        List<String> metadataValues = new ArrayList<String>();
        for (String start : startUrlsValue) {
            Map<String, List<String>> metas = startUrls.get( start );
            
            String metasConcat = start;
            for (String key : metas.keySet()) {
                metasConcat += "\t" + key + ":\t" + StringUtils.join( metas.get( key ), "\t" );
            }
            metadataValues.add( metasConcat );
        }
        
        FileUtils.writeToFile( Paths.get( workDir, "urls", "metadata" ).toAbsolutePath(), "seed.txt", metadataValues );
        FileUtils.writeToFile( Paths.get( workDir, "urls", "limit" ).toAbsolutePath(), "seed.txt", limitUrls );
        FileUtils.writeToFile( Paths.get( workDir, "urls", "exclude" ).toAbsolutePath(), "seed.txt", excludeUrls );
        
        // configure crawl process        
        Instance instance = new Instance();
        instance.setName(name);
        instance.setWorkingDirectory( workDir );

        IngridCrawlNutchProcess process = NutchProcessFactory.getIngridCrawlNutchProcess(instance, 1, 100);

        // run crawl process
        nutchController.start(instance, process);
        return redirect(AdminViews.SE_INSTANCE_MANAGEMENT + ".html?instance=" + name);
    }

}
