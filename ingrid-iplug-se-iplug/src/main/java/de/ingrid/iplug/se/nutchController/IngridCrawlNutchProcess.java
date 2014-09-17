package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Wrapper for a ingrid specific nutch process execution. This is too complex to
 * execute this with a {@link GenericNutchProcess}.
 * 
 */
public class IngridCrawlNutchProcess extends NutchProcess {

    private static Logger log = Logger.getLogger(IngridCrawlNutchProcess.class);

    Integer depth = 1;

    Integer noUrls = 1;

    @Override
    public void run() {
        status = STATUS.RUNNING;

        try {

            FileSystem fs = FileSystems.getDefault();

            // commonly used crawl parameter
            String crawlDb = fs.getPath(workingDirectory.getAbsolutePath(), "crawldb").toString();
            String bwDb = fs.getPath(workingDirectory.getAbsolutePath(), "bwdb").toString();
            String mddb = fs.getPath(workingDirectory.getAbsolutePath(), "mddb").toString();
            String webgraph = fs.getPath(workingDirectory.getAbsolutePath(), "webgraph").toString();
            String linkDb = fs.getPath(workingDirectory.getAbsolutePath(), "linkdb").toString();

            String startUrls = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "start").toString();
            String metadata = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "metadata").toString();
            String limitPatterns = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "limit").toString();
            String excludePatterns = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "exclude").toString();

            String segments = fs.getPath(workingDirectory.getAbsolutePath(), "segments").toString();
            String mergedSegments = fs.getPath(workingDirectory.getAbsolutePath(), "segments_merged").toString();
            String filteredSegments = fs.getPath(workingDirectory.getAbsolutePath(), "segments_filtered").toString();

            execute("org.apache.nutch.crawl.Injector", crawlDb, startUrls);

            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWInjector", bwDb, limitPatterns, excludePatterns);

            execute("de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector", mddb, metadata);

            // Usage: <crawldb> <bwdb> <normalize> <filter> <replace current
            // crawldb>
            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWCrawlDbFilter", crawlDb, bwDb, "false", "false", "true");

            for (int i = 0; i < depth; i++) {
                execute("org.apache.nutch.crawl.Generator", crawlDb, segments, "-topN", noUrls.toString());

                String currentSegment = fs.getPath(segments, getCurrentSegment(segments)).toString();

                execute("org.apache.nutch.fetcher.Fetcher", currentSegment);

                // Usage: <crawldb> <bwdb> <segment> <normalize> <filter>
                execute("de.ingrid.iplug.se.nutch.crawl.bw.BWUpdateDb", crawlDb, bwDb, currentSegment, "true", "true");

                execute("de.ingrid.iplug.se.nutch.tools.HostStatistic", crawlDb, currentSegment);

                execute("de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater", mddb, currentSegment);
            }

            execute("org.apache.nutch.segment.SegmentMerger", mergedSegments, "-dir", segments);

            removeRecursive(fs.getPath(segments));
            Files.move(fs.getPath(mergedSegments), fs.getPath(segments), StandardCopyOption.REPLACE_EXISTING);

            execute("de.ingrid.iplug.se.nutch.segment.SegmentFilter", filteredSegments, crawlDb, "-dir", segments);

            removeRecursive(fs.getPath(segments));
            Files.move(fs.getPath(filteredSegments), fs.getPath(segments), StandardCopyOption.REPLACE_EXISTING);

            execute("org.apache.nutch.scoring.webgraph.WebGraph", "-webgraphdb", webgraph, "-segmentDir", segments);

            execute("org.apache.nutch.scoring.webgraph.LinkRank", "-webgraphdb", webgraph);

            execute("org.apache.nutch.scoring.webgraph.ScoreUpdater", "-webgraphdb", webgraph, "-crawldb", crawlDb);

            // Usage: BWWebgraphFilter <webgraphdb> <bwdb> <normalize> <filter>
            // <replace current webgraph>
            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWWebgraphFilter", webgraph, bwDb, "false", "false", "true");
            
            
            execute("org.apache.nutch.crawl.LinkDb", linkDb, "-dir", segments, "-noNormalize", "-noFilter");

            // Usage: BWLinkDbFilter <linkdb> <bwdb> <normalize> <filter>
            // <replace current linkdb>
            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWLinkDbFilter", linkDb, bwDb, "false", "false", "true");

            execute("org.apache.nutch.indexer.IndexingJob", crawlDb, "-linkdb", linkDb, "-dir", segments, "-deleteGone");

            if (status == STATUS.RUNNING) {
                status = STATUS.FINISHED;
            }

        } catch (InterruptedException e) {
            status = STATUS.INTERRUPTED;
            if (resultHandler.getWatchdog().killedProcess()) {
                log.info("Process was killed by watchdog.");
            } else {
                log.error("Process was unexpectably killed.", e);
            }
        } catch (IOException e) {
            status = STATUS.INTERRUPTED;
            log.error("Process exited with errors.", e);
        }

    }

    private void execute(String... commandAndOptions) throws IOException, InterruptedException {
        String cp = StringUtils.join(classPath, File.pathSeparator);

        String[] nutchCall = new String[] { "-cp", cp };
        nutchCall = arrayConcat(nutchCall, javaOptions);
        nutchCall = arrayConcat(nutchCall, commandAndOptions);

        CommandLine cmdLine = new CommandLine(executable);
        cmdLine.addArguments(nutchCall);

        CommandResultHandler resultHandler;
        Executor executor = new DefaultExecutor();
        if (workingDirectory != null) {
            executor.setWorkingDirectory(workingDirectory);
        } else {
            executor.setWorkingDirectory(new File("."));
        }
        ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
        executor.setWatchdog(watchdog);
        resultHandler = new CommandResultHandler(watchdog);
        if (log.isDebugEnabled()) {
            log.debug("Call: " + StringUtils.join(cmdLine.toStrings(), " "));
        }
        executor.execute(cmdLine, resultHandler);
        executor.getStreamHandler();
        resultHandler.waitFor();
        if (resultHandler.getExitValue() != 0) {
            throw new IOException("Process exited with error code: " + resultHandler.getExitValue());
        }
    }

    private String getCurrentSegment(String path) {

        File file = new File(path);
        String[] segments = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        Arrays.sort(segments);

        return segments[segments.length - 1];

    }
    
    private static void removeRecursive(Path path) throws IOException {
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
    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public Integer getNoUrls() {
        return noUrls;
    }

    public void setNoUrls(Integer noUrls) {
        this.noUrls = noUrls;
    }

}
