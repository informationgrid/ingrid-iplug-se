/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.webapp.controller.instance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.ingrid.iplug.se.StatusProviderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import de.ingrid.iplug.se.BlpImportBean;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.UVPDataImporter;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.MetaElement;
import de.ingrid.iplug.se.conf.UrlMaintenanceSettings.Metadata;
import de.ingrid.iplug.se.utils.InstanceConfigurationTool;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.AdminViews;

/**
 * Control the BLP Import page.
 *
 * @author joachim@wemove.com
 *
 */
@Controller
@SessionAttributes("plugDescription")
public class BlpImportController extends InstanceController {

    private static Logger log = LogManager.getLogger(InstanceController.class);

    @Autowired
    StatusProviderService statusProviderService;
    
    @ModelAttribute("partners")
    public Map<String,String> partners(@RequestParam("instance") String name) {

        InstanceConfigurationTool instanceConfig = null;
        try {
            instanceConfig = new InstanceConfigurationTool( Paths.get( SEIPlug.conf.getInstancesDir(), name, "conf", "urlMaintenance.json" ) );
        } catch (RuntimeException e) {
            return null;
        }
        // get partner metadata
        List<MetaElement> metadata = instanceConfig.getMetadata();
        MetaElement partnerMetadata = null;
        for (MetaElement me : metadata) {
            if (me.getLabel().equals( "Partner" )) {
                partnerMetadata = me;
                break;
            }
        }
        if (partnerMetadata == null) {
            // no Partner element found  
            return null;
        }
        
        HashMap<String, String> partners = new HashMap<>();
        for (Metadata partner : partnerMetadata.getChildren()) {
            partners.put( partner.getId(), partner.getLabel() );
        }
        return partners;
    }
    

    @RequestMapping(value = { "/iplug-pages/instanceBlpImport.html" }, method = RequestMethod.GET)
    public String showBlpImport(@ModelAttribute("blpImportBean") final BlpImportBean blpImportBean, final ModelMap modelMap, @RequestParam("instance") String name, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Instance instance = InstanceController.getInstanceData( name );

        if (instance == null) {
            return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
        } else {
            if (hasNoAccessToInstance( name, request, response )) {
                return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
            }
            modelMap.put( "instance", instance );
            return AdminViews.SE_INSTANCE_BLP_IMPORT;
        }
    }

    /**
     * Upload excel file.
     *
     * @param blpImportBean
     * @param model
     * @param name
     * @return
     * @throws IOException
     */
    @RequestMapping(method = RequestMethod.POST)
    public String upload(@ModelAttribute("blpImportBean") final BlpImportBean blpImportBean, final Model model, @RequestParam("instance") String name) throws IOException {

        Instance instance = getInstanceData( name );

        MultipartFile file = blpImportBean.getFile();

        Path blpBackupDir = Paths.get(instance.getWorkingDirectory(), "blp_backup");
        try {
            Files.createDirectories(Paths.get(instance.getWorkingDirectory(), "blp_backup"));
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, Paths.get(blpBackupDir.toString(), file.getOriginalFilename()).toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                log.error("Cannot copy file " + file.getName() + " to " + Paths.get(blpBackupDir.toString(), file.getOriginalFilename()).toAbsolutePath(), e);
            }
        } catch (Exception e) {
            log.error("Cannot create backup directory " + blpBackupDir.toAbsolutePath(), e);
        }

        UVPDataImporter importer = new UVPDataImporter();
        importer.setInstance( instance );
        importer.setPartner( blpImportBean.getPartner() );
        importer.setExcelFileInputStream( file.getInputStream() );
        importer.setExcelFileName( file.getOriginalFilename() );
        importer.setStatusProviderService(statusProviderService);
        importer.start();
        return redirect( AdminViews.SE_INSTANCE_BLP_IMPORT + ".html?instance=" + name );
    }

}
