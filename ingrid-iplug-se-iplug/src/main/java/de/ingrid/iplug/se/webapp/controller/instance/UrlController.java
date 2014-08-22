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
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.SEIPlug;
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
public class UrlController extends AbstractController {

    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap,
            @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject,
            @RequestParam("instance") String name) {

        String dir = SEIPlug.conf.getInstancesDir();
        File instanceFolder = new File( dir, name );
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

//        Instance instance = getInstanceData( name );
        //instance.setUrls( getUrlsFromDB() );

//        modelMap.put( "instance", instance );

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
