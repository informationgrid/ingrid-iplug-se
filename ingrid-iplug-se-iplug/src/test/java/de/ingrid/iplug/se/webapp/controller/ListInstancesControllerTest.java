package de.ingrid.iplug.se.webapp.controller;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.ui.ModelMap;

import de.ingrid.iplug.se.Configuration;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.utils.FileUtils;

public class ListInstancesControllerTest {

    @Test
    public void testAddInstance() throws Exception {
        
        FileUtils.removeRecursive(Paths.get("test-instances"));
        
        Configuration configuration = new Configuration();
        configuration.setInstancesDir("test-instances");
        SEIPlug.conf = configuration;
        
        ListInstancesController lic = new ListInstancesController();
        lic.addInstance(new ModelMap(), "test");
        
        assertTrue("Instance path created", Files.exists(Paths.get("test-instances", "test")));
        assertTrue("Instance configuration path created", Files.exists(Paths.get("test-instances", "test", "conf")));
        assertTrue("Instance nutch configuration path created", Files.exists(Paths.get("test-instances", "test", "conf", "nutch", "nutch-default.xml")));

        FileUtils.removeRecursive(Paths.get("test-instances"));
        
    }

}
