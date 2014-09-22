package de.ingrid.iplug.se.webapp.controller;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.ingrid.admin.command.PlugdescriptionCommandObject;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import de.ingrid.iplug.se.webapp.controller.instance.InstanceController;

/**
 * Control the database parameter page.
 * 
 * @author joachim@wemove.com
 * 
 */
@Controller
@SessionAttributes("plugDescription")
public class ListInstancesController extends InstanceController {
    
    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

    // @ModelAttribute("instances")
    public List<Instance> getInstances() throws Exception {
        ArrayList<Instance> list = new ArrayList<Instance>();

        String dir = SEIPlug.conf.getInstancesDir();
        if (Files.isDirectory( Paths.get( dir ) )) {
            FileFilter directoryFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            };
            File instancesDirObject = new File( dir );
            File[] subDirs = instancesDirObject.listFiles( directoryFilter );
            for (File subDir : subDirs) {
                Instance instance = getInstanceData( subDir.getName() );
                Client client = elasticSearch.getObject().client();
                boolean typeExists = typeExists( SEIPlug.conf.index, subDir.getName(), client );
                instance.setIndexTypeExists( typeExists );
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
    public String post(@ModelAttribute("plugDescription") final PlugdescriptionCommandObject pdCommandObject,
            @RequestParam(value = "action", required = false) final String action) {

        return AdminViews.SAVE;
    }

    @RequestMapping(value = "/iplug-pages/listInstances.html", method = RequestMethod.POST, params = "recreateIndex")
    public String addTypeToIndex(@RequestParam("instance") String name) throws Exception {
        createIndexType( name );
        return redirect( AdminViews.SE_LIST_INSTANCES + ".html" );
    }
    
    private void createIndexType(String type) throws Exception {
        Client client = elasticSearch.getObject().client();
        String indexName = SEIPlug.conf.index;
        client.admin().indices().preparePutMapping().setIndices( indexName )
            .setType( type )
            .setSource( getMappingSource() )
            .execute()
            .actionGet();
    }
    
    @RequestMapping(value = "/iplug-pages/listInstances.html", method = RequestMethod.POST, params = "add")
    public String addInstance(final ModelMap modelMap, @RequestParam("instance") String name) throws Exception {

        // convert illegal chars to "_"
        name = name.replaceAll( "[:\\\\/*?|<>\\W]", "_" );
        String dir = SEIPlug.conf.getInstancesDir();
        String indexName = SEIPlug.conf.index;

        // convert illegal chars to "_"
        name = name.replaceAll( "[:\\\\/*?|<>\\W]", "_" );
        // create directory and copy necessary configuration files
        boolean success = initializeInstanceDir( dir + "/" + name );
        if (success) {
            
            Client client = elasticSearch.getObject().client();
            // if a type within an index already exists, then return error and ask user what to do
            boolean typeExists = typeExists( indexName, name, client );
            
            if (typeExists) {
                modelMap.put( "instances", getInstances() );
                modelMap.put( "error", "Type already exists in index" );
                
                return AdminViews.SE_LIST_INSTANCES;
                
            } else {
                createIndexType( name );
            }
            
        } else {
            modelMap.put( "error", "Default configuration could not be copied to: " + dir + "/" + name );
        }
        
        modelMap.put( "instances", getInstances() );
        
        return AdminViews.SE_LIST_INSTANCES;
    }

    private boolean typeExists(String indexName, String type, Client client) {
        TypesExistsRequest typeRequest = new TypesExistsRequest( new String[]{ indexName }, type );
        boolean typeExists = client.admin().indices().typesExists( typeRequest ).actionGet().isExists();
        return typeExists;
    }
    
    private String getMappingSource() throws IOException {
        ClassPathResource resource = new ClassPathResource( "data/mappingProperties.json" );
        List<String> urlsData = Files.readAllLines( Paths.get( resource.getURI() ), Charset.defaultCharset() );
        String mappingSource = "";
        for (String line : urlsData) {
            mappingSource  += line;
        }
        return mappingSource;
    }

    private boolean initializeInstanceDir(String path) {
        boolean result = false;
        
        try {
            final Path newInstanceDir = Files.createDirectories( Paths.get( path ) );
            if (newInstanceDir == null) {
                throw new RuntimeException( "Directory could not be created: " + path );
            }
            

            // copy default configuration
            Path destDir = Paths.get( newInstanceDir.toString(), "conf" );
            Path sourceDir = Paths.get( "conf", "default", "conf" );
            try {
                FileUtils.copyDirectories(sourceDir, destDir);
            } catch (IOException e) {
                e.printStackTrace();
                //modelMap.put( "error", "Default configuration could not be copied to: " + destDir );
            }

            // copy nutch configurations
            destDir = Paths.get( newInstanceDir.toString(), "conf", "nutch" );
            sourceDir = Paths.get( "apache-nutch-runtime", "runtime", "local", "conf" );
            try {
                FileUtils.copyDirectories(sourceDir, destDir);
            } catch (IOException e) {
                e.printStackTrace();
                //modelMap.put( "error", "Default configuration could not be copied to: " + destDir );
            }

            
            //modelMap.put( "instances", getInstances() );
                
                result = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }

    @RequestMapping(value = "/iplug-pages/listInstances", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteInstance(@RequestBody String name) throws Exception {
        String dir = SEIPlug.conf.getInstancesDir();
        Path directoryToDelete = Paths.get( dir, name );
        try {
            Files.walkFileTree( directoryToDelete, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete( file );
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete( dir );
                    return FileVisitResult.CONTINUE;
                }

            } );
        } catch (IOException e) {
            e.printStackTrace();
            //modelMap.put( "error", "Directory '" + directoryToDelete.toString() + "' could not be deleted!" );
            return new ResponseEntity<String>( HttpStatus.INTERNAL_SERVER_ERROR );
        }

        //modelMap.put( "instances", getInstances() );

        return new ResponseEntity<String>( HttpStatus.OK );
    }

}
