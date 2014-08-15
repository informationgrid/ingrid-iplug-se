package de.ingrid.iplug.se.webapp.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
import de.ingrid.iplug.se.webapp.container.Instance;


/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class ListInstancesController extends AbstractController {

    //@ModelAttribute("instances")
    public List<Instance> getInstances() throws Exception {
        ArrayList<Instance> list = new ArrayList<Instance>();
        
        String dir = SEIPlug.conf.getInstancesDir();
        if ( Files.isDirectory( Paths.get( dir ) ) ) {
            FileFilter directoryFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            };
            File instancesDirObject = new File( dir );
            File[] subDirs = instancesDirObject.listFiles( directoryFilter );
            for (File subDir : subDirs) {
                Instance instance = new Instance();
                instance.setName( subDir.getName() );
                list.add( instance );
            }
        }
        
        return list;
    }
    
    @RequestMapping(value = { "/iplug-pages/listInstances.html" }, method = RequestMethod.GET)
    public String getParameters(final ModelMap modelMap) throws Exception {

        modelMap.put( "instances", getInstances() );
        return AdminViews.SE_LIST_INSTANCES;
    }

    @RequestMapping(value = "/iplug-pages/listInstances.html", method = RequestMethod.POST)
    public String post( @ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject,
            @RequestParam(value = "action", required = false) final String action ) {

        return AdminViews.SAVE;
    }
    
    @RequestMapping(value = "/iplug-pages/listInstances.html", method = RequestMethod.POST, params = "add")
    public String addInstance(final ModelMap modelMap, @RequestParam("name") String name) {
        String dir = SEIPlug.conf.getInstancesDir();
        
        try {
            Path newDir = Files.createDirectories( Paths.get( dir + "/" + name ) );
            if (newDir == null) {
                throw new RuntimeException("Directory could not be created: " + dir + "/" + name );
            }
            modelMap.put( "instances", getInstances() );
            
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return AdminViews.SE_LIST_INSTANCES;
    }

    @RequestMapping(value = "/iplug-pages/listInstances.html", method = RequestMethod.GET, params = "delete")
    public String deleteInstance(final ModelMap modelMap, @RequestParam("id") String name) throws Exception {
        String dir = SEIPlug.conf.getInstancesDir();
        Files.delete( Paths.get( dir + "/" + name ) );
        
        modelMap.put( "instances", getInstances() );
        
        return AdminViews.SE_LIST_INSTANCES;
    }
    
}
