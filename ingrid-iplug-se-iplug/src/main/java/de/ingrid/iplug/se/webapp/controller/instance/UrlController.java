package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.eclipse.jdt.internal.compiler.batch.FileFinder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.webapp.controller.AdminViews;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class UrlController extends InstanceController {

    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @RequestParam("instance") String name) {

        String dir = SEIPlug.conf.getInstancesDir();
        File instanceFolder = new File( dir, name );
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

        modelMap.put( "instance", getInstanceData(name) );

        return AdminViews.SE_INSTANCE_URLS;
    }
    
    @ModelAttribute("dbUrls")
    public List<Url> getUrlsFromDB(@RequestParam("instance") String name, 
            @RequestParam(value="filter", required=false, defaultValue="") String filter) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Url> createQuery = em.getCriteriaBuilder().createQuery( Url.class );
        Root<Url> urlTable = createQuery.from( Url.class );

        List<Predicate> criteria = new ArrayList<Predicate>();
        criteria.add( criteriaBuilder.equal( urlTable.<String> get ("instance"), name ) );
        
        if (!filter.isEmpty()) {
            String[] metaOptions = filter.split( "," );
            for (String meta : metaOptions) {
                Join<Url, Metadata> j = urlTable.join("metadata", JoinType.LEFT );
                String[] metaSplit = meta.split( ":" );
                if (metaSplit.length == 2) {
                    criteria.add( criteriaBuilder.equal( j.get( "metaKey" ), metaSplit[0] ) );
                    criteria.add( criteriaBuilder.equal( j.get( "metaValue" ), metaSplit[1] ) );
                }
            }
        }
        
        
        createQuery.select( urlTable ).where( criteriaBuilder.and(criteria.toArray(new Predicate[0])) );
        TypedQuery<Url> tq = em.createQuery(createQuery);
        return tq.getResultList();
    }
    
    @ModelAttribute("filterOptions")
    public String[] getFilterOption(@RequestParam(value="filter", required=false, defaultValue="") String filter) {
        return filter.split( "," );
//        return Arrays.asList( split );
//        for (String item : split) {
//            if (item.equals( option ))
//                return true;
//        }
//        return false;
    }
    
    @ModelAttribute("metadata")
    public List<MetaElement> getMetadata(@RequestParam("instance") String name) throws FileNotFoundException, JsonSyntaxException, JsonIOException, UnsupportedEncodingException {
        String confFile = SEIPlug.conf.getInstancesDir() + "/" + name + "/conf/urlMaintenance.json";
        
        // check if json can be converted correctly
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        InputStream reader = new FileInputStream( confFile );
        UrlMaintenanceSettings settings = gson.fromJson( new InputStreamReader( reader, "UTF-8" ), UrlMaintenanceSettings.class );
        return settings.getMetadata();
    }
    
    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.POST, params = "editUrl")
    public String editUrl(@RequestParam("instance") String name, @RequestParam("id") Long id) {
        
        return redirect( AdminViews.SE_INSTANCE_URLS + ".html?instance=" + name );
    }
    
    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.POST, params = "deleteUrl")
    public String deleteUrl(@RequestParam("instance") String name, @RequestParam("id") Long id) {
        
        return redirect( AdminViews.SE_INSTANCE_URLS + ".html?instance=" + name );
    }
    
    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.POST, params = "testUrl")
    public String testUrl(@RequestParam("instance") String name, @RequestParam("id") Long id) {
        
        return redirect( AdminViews.SE_INSTANCE_URLS + ".html?instance=" + name );
    }
    
    
}
