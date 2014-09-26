package de.ingrid.iplug.se.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.UrlHandler;
import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;
import edu.emory.mathcs.backport.java.util.Arrays;


public class FileUtils {
    
    private static Logger log = Logger.getLogger( FileUtils.class );
    
    public static void removeRecursive(Path path) throws IOException {
        if (!Files.exists(path)) return;
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                // try to delete the file anyway, even if its attributes
                // could not be read, since delete-only access is
                // theoretically possible
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc == null) {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else {
                    // directory iteration failed; propagate exception
                    throw exc;
                }
            }
        });
    }

    public static void copyDirectories(Path source, Path destination) throws IOException {

        Files.walkFileTree(source, new CopyVisitor(source, destination));

    }
    
    public static void writeToFile( Path path, String name, List<String> listContent ) throws IOException {
        if(path != null){
            Files.createDirectories( path );
            if (listContent != null) {
                Writer writer = new FileWriter( path.resolve( name ).toString() );
                BufferedWriter bWriter = new BufferedWriter( writer );
                for (String content : listContent) {
                    bWriter.write(content);
                    bWriter.newLine();
                }
                bWriter.close();
            } else {
                log.error("Content is null!");
            }
        }
    }
    
    
    private static class CopyVisitor extends SimpleFileVisitor<Path> {
        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;

        
        CopyVisitor(Path fromPath, Path toPath) {
            this.fromPath = fromPath;
            this.toPath = toPath;
        }
        
        
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir));
            if (!Files.exists(targetPath)) {
                Files.createDirectory(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption);
            return FileVisitResult.CONTINUE;
        }
    }
    
    public static void prepareCrawl(String name) throws IOException {
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
    }

    public static String readFile(Path path) throws IOException {
        byte[] encoded = Files.readAllBytes( path );
        return new String(encoded, "UTF-8");
    }

}
