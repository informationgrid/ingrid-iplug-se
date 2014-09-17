package de.ingrid.iplug.se.nutchController;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.Settings.Builder;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.ingrid.admin.JettyStarter;
import de.ingrid.iplug.se.elasticsearch.Utils;

import static org.elasticsearch.node.NodeBuilder.*;

@RunWith(PowerMockRunner.class)
//@PrepareForTest(JettyStarter.class)
public class NutchProcessTest {

//    @Mock JettyStarter jettyStarter;

    @Test
    public void testGenericNutchProcessor() throws Exception {
        FileSystem fs = FileSystems.getDefault();

        Path workingDir = fs.getPath("test").toAbsolutePath();

        if (Files.exists(workingDir)) {
            removeRecursive(workingDir);
        }
        Files.createDirectories(workingDir);

        Path conf = fs.getPath("test", "conf").toAbsolutePath();
        Path urls = fs.getPath("test", "urls").toAbsolutePath();
        Path logs = fs.getPath("test", "logs").toAbsolutePath();
        Files.createDirectories(logs);

        copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/conf").toAbsolutePath(), conf);
        copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);

        GenericNutchProcess p = new GenericNutchProcess();
        p.setWorkingDirectory(workingDir.toString());
        p.addClassPath(conf.toString());
        p.addClassPath("../../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local");
        p.addClassPathLibraryDirectory("../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local/lib");
        p.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + logs, "-Dhadoop.log.file=hadoop.log" });
        p.addCommand("org.apache.nutch.crawl.Injector", "crawldb", "../../ingrid-iplug-se-nutch/src/test/resources/urls/start");
        p.start();
        Thread.sleep(500);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, p.getStatus());
        Thread.sleep(5000);
        assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, p.getStatus());
    }

    @Test
    public void testIngridCrawlNutchProcessor() throws Exception {
        FileSystem fs = FileSystems.getDefault();

        Path workingDir = fs.getPath("test").toAbsolutePath();

        if (Files.exists(workingDir)) {
            removeRecursive(workingDir);
        }
        Files.createDirectories(workingDir);

        Path conf = fs.getPath("test", "conf").toAbsolutePath();
        Path urls = fs.getPath("test", "urls").toAbsolutePath();
        Path logs = fs.getPath("test", "logs").toAbsolutePath();
        Files.createDirectories(logs);

        copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/conf").toAbsolutePath(), conf);
        copyDirectories(fs.getPath("../ingrid-iplug-se-nutch/src/test/resources/urls").toAbsolutePath(), urls);

        IngridCrawlNutchProcess p = new IngridCrawlNutchProcess();
        p.setWorkingDirectory(workingDir.toString());
        p.addClassPath(conf.toString());
        p.addClassPath("../../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local");
        p.addClassPathLibraryDirectory("../ingrid-iplug-se-nutch/build/apache-nutch-1.9/runtime/local/lib");
        p.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + logs, "-Dhadoop.log.file=hadoop.log" });
        p.setDepth(1);
        p.setNoUrls(10);
        p.start();
        
        Settings settings = ImmutableSettings.settingsBuilder().put("path.data", "./").build();

        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().clusterName("elasticsearch").data(true).settings(settings);

        nodeBuilder = nodeBuilder.local(false);

        Node node = nodeBuilder.node();
        
        long start = System.currentTimeMillis();
        Thread.sleep(500);
        assertEquals("Status is RUNNING", NutchProcess.STATUS.RUNNING, p.getStatus());
        while ((System.currentTimeMillis()- start) < 300000) {
            Thread.sleep(1000);
            if (p.getStatus() != NutchProcess.STATUS.RUNNING) {
                break;
            }
        }
        if (p.getStatus() == NutchProcess.STATUS.RUNNING) {
            node.close();
            p.stopExecution();
            fail("Crawl took more than 5 min.");
        }
        assertEquals("Status is FINISHED", NutchProcess.STATUS.FINISHED, p.getStatus());
        node.close();
    }

    private void removeRecursive(Path path) throws IOException {
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

    public void copyDirectories(Path source, Path destination) throws IOException {

        Files.walkFileTree(source, this.new CopyVisitor(source, destination));

    }
    
    public class CopyVisitor extends SimpleFileVisitor<Path> {
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
}
