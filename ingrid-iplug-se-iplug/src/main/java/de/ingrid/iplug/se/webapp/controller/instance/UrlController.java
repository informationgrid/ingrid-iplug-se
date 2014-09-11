package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.File;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.IngridPartner;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.UrlTypes;
import de.ingrid.iplug.se.db.DBManager;
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
    public List<Url> getUrlsFromDB(@RequestParam("instance") String name) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        CriteriaQuery<Url> createQuery = em.getCriteriaBuilder().createQuery( Url.class );
        Root<Url> from = createQuery.from( Url.class );
        createQuery.select( from );
        Query query = em.createQuery( createQuery );
        return query.getResultList();
    }
    
    @ModelAttribute("partners")
    public List<IngridPartner> getPartner() {
        return SEIPlug.conf.getUrlMaintenanceSettings().getPartner();
    }
    
    @ModelAttribute("types")
    public List<UrlTypes> getTypes() {
        return SEIPlug.conf.getUrlMaintenanceSettings().getTypes();
    }
    
    @ModelAttribute("metadata")
    public List<MetaElement> getMetadata() {
        List<MetaElement> metadata = SEIPlug.conf.getUrlMaintenanceSettings().getMetadata();
//        List<Metadata> metadata = new ArrayList<Metadata>();
//        
//        UrlMaintenanceSettings settings = SEIPlug.conf.getUrlMaintenanceSettings();
//        
//        Metadata m = settings.new Metadata();
//        m.setId( "partnerMD" );
//        m.setLabel( "Partner :D" );
//        List<Metadata> children = new ArrayList<Metadata>();
//        
//        Metadata c1 = settings.new Metadata();
//        c1.setId( "by" );
//        c1.setLabel( "Bayern :D" );
//        List<Metadata> c1List  = new ArrayList<Metadata>();
//        Metadata c11 = settings.new Metadata();
//        c11.setId( "p1" );
//        c11.setLabel( "Provider Nummer 1" );
//        c1List.add( c11 );
//        Metadata c12 = settings.new Metadata();
//        c12.setId( "p2" );
//        c12.setLabel( "Provider Nummer 2" );
//        c1List.add( c12 );
//        c1.setChildren( c1List );
//        
//        Metadata c2 = settings.new Metadata();
//        c2.setId( "by" );
//        c2.setLabel( "Sachsen :D" );
//        
//        c1List  = new ArrayList<Metadata>();
//        c11 = settings.new Metadata();
//        c11.setId( "p3" );
//        c11.setLabel( "Provider Nummer 3" );
//        c1List.add( c11 );
//        c12 = settings.new Metadata();
//        c12.setId( "p4" );
//        c12.setLabel( "Provider Nummer 4" );
//        c1List.add( c12 );
//        c2.setChildren( c1List );
//        
//        children.add( c1  );
//        m.setChildren( children );
//        metadata.add( m  );
//        
//        
//        
//        metadata.add( m2  );
        
        return metadata;
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
