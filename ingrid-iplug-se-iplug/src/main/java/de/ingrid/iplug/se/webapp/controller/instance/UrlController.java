package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.controller.AbstractController;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.utils.InstanceConfigurationTool;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class UrlController extends AbstractController {

    private CommunicationService _communicationInterface;
    
    @Autowired
    public UrlController(final CommunicationService communicationInterface) throws Exception {
        _communicationInterface = communicationInterface;
    }

    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, @RequestParam("instance") String name) {

        String dir = SEIPlug.conf.getInstancesDir();
        File instanceFolder = new File(dir, name);
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

        modelMap.put("instance", InstanceController.getInstanceData(name));

        return AdminViews.SE_INSTANCE_URLS;
    }
    
    @ModelAttribute("dbUrls")
    public List<Url> getUrlsFromDB(@RequestParam("instance") String name, @RequestParam(value = "filter", required = false, defaultValue = "") String filter) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Url> createQuery = em.getCriteriaBuilder().createQuery(Url.class);
        Root<Url> urlTable = createQuery.from(Url.class);

        List<Predicate> criteria = new ArrayList<Predicate>();
        criteria.add(criteriaBuilder.equal(urlTable.<String> get("instance"), name));

        if (!filter.isEmpty()) {
            String[] metaOptions = filter.split(",");
            for (String meta : metaOptions) {
                Join<Url, Metadata> j = urlTable.join("metadata", JoinType.LEFT);
                String[] metaSplit = meta.split(":");
                if (metaSplit.length == 2) {
                    criteria.add(criteriaBuilder.equal(j.get("metaKey"), metaSplit[0]));
                    criteria.add(criteriaBuilder.equal(j.get("metaValue"), metaSplit[1]));
                }
            }
        }

        createQuery.select(urlTable).where(criteriaBuilder.and(criteria.toArray(new Predicate[0])));
        TypedQuery<Url> tq = em.createQuery(createQuery);
        return tq.getResultList();
    }

    @ModelAttribute("filterOptions")
    public String[] getFilterOption(@RequestParam(value = "filter", required = false, defaultValue = "") String filter) {
        return filter.split(",");
        // return Arrays.asList( split );
        // for (String item : split) {
        // if (item.equals( option ))
        // return true;
        // }
        // return false;
    }

    @ModelAttribute("metadata")
    public List<MetaElement> getMetadata(@RequestParam("instance") String name) throws FileNotFoundException, JsonSyntaxException, JsonIOException, UnsupportedEncodingException {

        InstanceConfigurationTool instanceConfig = null;
        try {
            instanceConfig = new InstanceConfigurationTool(Paths.get(SEIPlug.conf.getInstancesDir(), name, "conf", "urlMaintenance.json"));
        } catch (RuntimeException e) {
            return null;
        }
        List<MetaElement> metadata = instanceConfig.getMetadata();
        
        // try to get latest partner and provider from the iBus (Management-iPlug / Portal)
        if (_communicationInterface.isConnected(0)) {
            try {
                List<Partner> partners = Utils.getPartners(_communicationInterface.getIBus());
                List<Provider> providers = Utils.getProviders(_communicationInterface.getIBus());
                UrlMaintenanceSettings settings = instanceConfig.getSettings();
                
                // find partner in metadata
                for (MetaElement metaElement : metadata) {
                    if ("partner".equals( metaElement.getId() )) {
                        List<UrlMaintenanceSettings.Metadata> partnerMeta = metaElement.getChildren();
                        // remove all entries
                        partnerMeta.clear();

                        // fill with entries from iBus
                        for (Partner partner : partners) {
                            UrlMaintenanceSettings.Metadata m = settings.new Metadata();
                            m.setId( partner.getShortName() );
                            m.setLabel( partner.getDisplayName() );
                            partnerMeta.add( m );
                        }
                        
                    } else if ("provider".equals( metaElement.getId() )) {
                        List<UrlMaintenanceSettings.Metadata> providerMeta = metaElement.getChildren();
                        // remove all entries
                        providerMeta.clear();
                        
                        // fill with entries from iBus
                        for (Partner partner : partners) {
                            UrlMaintenanceSettings.Metadata m = settings.new Metadata();
                            m.setId( partner.getShortName() );
                            m.setLabel( partner.getDisplayName() );
                            
                            String providerId = partner.getShortName().substring(0, 2);
                            final Iterator<Provider> it = providers.iterator();
                            List<UrlMaintenanceSettings.Metadata> provider = new ArrayList<UrlMaintenanceSettings.Metadata>();
                            while (it.hasNext()) {
                                final Provider pr = it.next();
                                if (pr.getShortName().startsWith(providerId)) {
                                    UrlMaintenanceSettings.Metadata p = settings.new Metadata();
                                    p.setId( pr.getShortName() );
                                    p.setLabel( pr.getDisplayName() );
                                    provider.add( p );
                                    it.remove();
                                }
                            }
                            Collections.sort( provider, new ProviderComparer() );
                            m.setChildren( provider );
                            providerMeta.add( m );
                        }
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return metadata;
    }
    
    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.DELETE, params = "deleteUrl")
    public String deleteUrl(@RequestParam("instance") String name, @RequestParam("id") Long id) {

        return redirect(AdminViews.SE_INSTANCE_URLS + ".html?instance=" + name);
    }

    @RequestMapping(value = { "/iplug-pages/instanceUrls.html" }, method = RequestMethod.POST, params = "testUrl")
    public String testUrl(@RequestParam("instance") String name, @RequestParam("id") Long id) {

        return redirect(AdminViews.SE_INSTANCE_URLS + ".html?instance=" + name);
    }
    
    private class ProviderComparer implements Comparator<UrlMaintenanceSettings.Metadata> {

        @Override
        public int compare(UrlMaintenanceSettings.Metadata o1, UrlMaintenanceSettings.Metadata o2) {
            return o1.getLabel().toLowerCase().compareTo( o2.getLabel().toLowerCase() );
        }

    }

}
