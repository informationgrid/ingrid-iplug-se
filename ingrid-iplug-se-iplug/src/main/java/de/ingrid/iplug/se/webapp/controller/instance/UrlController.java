/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.webapp.controller.instance;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import de.ingrid.admin.Utils;
import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.admin.object.Partner;
import de.ingrid.admin.object.Provider;
import de.ingrid.admin.service.CommunicationService;
import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;
import de.ingrid.iplug.se.utils.InstanceConfigurationTool;
import de.ingrid.iplug.se.webapp.controller.AdminViews;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.*;

/**
 * Control the database parameter page.
 *
 * @author joachim@wemove.com
 */
@Controller
@SessionAttributes("plugDescription")
public class UrlController extends InstanceController {

    private CommunicationService _communicationInterface;

    private static final Log LOG = LogFactory.getLog(UrlController.class.getName());

    @Autowired
    private Configuration seConfig;

    @Autowired
    public UrlController(final CommunicationService communicationInterface) throws Exception {
        _communicationInterface = communicationInterface;
    }

    @RequestMapping(value = {"/iplug-pages/instanceUrls.html"}, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap, @ModelAttribute("plugDescription") final PlugdescriptionCommandObject commandObject, @RequestParam("instance") String name, HttpServletRequest request, HttpServletResponse response) {

        if (hasNoAccessToInstance(name, request, response)) {
            return redirect(AdminViews.SE_LIST_INSTANCES + ".html");
        }
        String dir = seConfig.getInstancesDir();
        File instanceFolder = new File(dir, name);
        if (!instanceFolder.exists())
            return "redirect:" + AdminViews.SE_LIST_INSTANCES + ".html";

        modelMap.put("instance", InstanceController.getInstanceData(name));

        return AdminViews.SE_INSTANCE_URLS;
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
            instanceConfig = new InstanceConfigurationTool(Paths.get(seConfig.getInstancesDir(), name, "conf", "urlMaintenance.json"));
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
                    if ("partner".equals(metaElement.getId())) {
                        List<UrlMaintenanceSettings.Metadata> partnerMeta = metaElement.getChildren();
                        // remove all entries
                        partnerMeta.clear();

                        // fill with entries from iBus
                        for (Partner partner : partners) {
                            UrlMaintenanceSettings.Metadata m = settings.new Metadata();
                            m.setId(partner.getShortName());
                            m.setLabel(partner.getDisplayName());
                            partnerMeta.add(m);
                        }

                    } else if ("provider".equals(metaElement.getId())) {
                        List<UrlMaintenanceSettings.Metadata> providerMeta = metaElement.getChildren();
                        // remove all entries
                        providerMeta.clear();

                        // fill with entries from iBus
                        for (Partner partner : partners) {
                            UrlMaintenanceSettings.Metadata m = settings.new Metadata();
                            m.setId(partner.getShortName());
                            m.setLabel(partner.getDisplayName());

                            String providerId = partner.getShortName().substring(0, 2);
                            final Iterator<Provider> it = providers.iterator();
                            List<UrlMaintenanceSettings.Metadata> provider = new ArrayList<UrlMaintenanceSettings.Metadata>();
                            while (it.hasNext()) {
                                final Provider pr = it.next();
                                if (pr.getShortName().startsWith(providerId)) {
                                    UrlMaintenanceSettings.Metadata p = settings.new Metadata();
                                    p.setId(pr.getShortName());
                                    p.setLabel(pr.getDisplayName());
                                    provider.add(p);
                                    it.remove();
                                }
                            }
                            Collections.sort(provider, new ProviderComparer());
                            m.setChildren(provider);
                            providerMeta.add(m);
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Error getting meta data from instance.", e);
            }
        }

        return metadata;
    }

    @RequestMapping(value = {"/iplug-pages/instanceUrls.html"}, method = RequestMethod.POST, params = "testUrl")
    public String testUrl(@RequestParam("instance") String name, @RequestParam("id") Long id, HttpServletRequest request, HttpServletResponse response) {
        if (hasNoAccessToInstance(name, request, response)) {
            return redirect(AdminViews.SE_LIST_INSTANCES + ".html");
        }

        return redirect(AdminViews.SE_INSTANCE_URLS + ".html?instance=" + name);
    }

    private class ProviderComparer implements Comparator<UrlMaintenanceSettings.Metadata> {

        @Override
        public int compare(UrlMaintenanceSettings.Metadata o1, UrlMaintenanceSettings.Metadata o2) {
            return o1.getLabel().toLowerCase().compareTo(o2.getLabel().toLowerCase());
        }

    }

}
