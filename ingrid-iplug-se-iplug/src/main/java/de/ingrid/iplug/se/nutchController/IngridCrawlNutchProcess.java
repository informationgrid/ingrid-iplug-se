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

            this.statusProvider.addState("START", "Start crawl.");

            FileSystem fs = FileSystems.getDefault();
            
            String workingPath = fs.getPath(workingDirectory.getAbsolutePath()).toString();

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

            this.statusProvider.addState("INJECT_START", "Inject start urls...");
            execute("org.apache.nutch.crawl.Injector", crawlDb, startUrls);
            this.statusProvider.appendToState("INJECT_START", " done.");

            this.statusProvider.addState("INJECT_BW", "Inject limit and exclude urls...");
            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWInjector", bwDb, limitPatterns, excludePatterns);
            this.statusProvider.appendToState("INJECT_BW", " done.");

            this.statusProvider.addState("INJECT_META", "Inject metadata...");
            execute("de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector", mddb, metadata);
            this.statusProvider.appendToState("INJECT_META", " done.");

            this.statusProvider.addState("FILTER_CRAWLDB", "Filter crawldb width limit/exclude urls...");
            // Usage: <crawldb> <bwdb> <normalize> <filter> <replace current
            // crawldb>
            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWCrawlDbFilter", crawlDb, bwDb, "false", "false", "true");
            this.statusProvider.appendToState("FILTER_CRAWLDB", " done.");

            for (int i = 0; i < depth; i++) {
                this.statusProvider.addState("GENERATE" + i, "Generate up to " + noUrls.toString() + " urls for fetching...");
                execute("org.apache.nutch.crawl.Generator", crawlDb, segments, "-topN", noUrls.toString());
                this.statusProvider.appendToState("GENERATE" + i, " done.");

                String currentSegment = fs.getPath(segments, getCurrentSegment(segments)).toString();

                this.statusProvider.addState("FETCH" + i, "Fetching...");
                execute("org.apache.nutch.fetcher.Fetcher", currentSegment);
                this.statusProvider.appendToState("FETCH" + i, " done.");

                this.statusProvider.addState("UPDATE_CRAWLDB" + i, "Update database with new urls and links...");
                // Usage: <crawldb> <bwdb> <segment> <normalize> <filter>
                execute("de.ingrid.iplug.se.nutch.crawl.bw.BWUpdateDb", crawlDb, bwDb, currentSegment, "true", "true");
                this.statusProvider.appendToState("UPDATE_CRAWLDB" + i, " done.");

                this.statusProvider.addState("UPDATE_MD" + i, "Update metadata for new urls...");
                execute("de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater", mddb, currentSegment);
                this.statusProvider.appendToState("UPDATE_MD" + i, " done.");
            }

            this.statusProvider.addState("CREATE_HOST_STATISTICS", "Create hosts statistic...");
            execute("de.ingrid.iplug.se.nutch.statistics.HostStatistic", crawlDb, workingPath);
            this.statusProvider.appendToState("CREATE_HOST_STATISTICS", " done.");

            this.statusProvider.addState("MERGE_SEGMENT", "Merge segments...");
            execute("org.apache.nutch.segment.SegmentMerger", mergedSegments, "-dir", segments);
            if (fs.getPath(mergedSegments).toFile().exists()) {
                removeRecursive(fs.getPath(segments));
                Files.move(fs.getPath(mergedSegments), fs.getPath(segments), StandardCopyOption.REPLACE_EXISTING);
            }
            this.statusProvider.appendToState("MERGE_SEGMENT", " done.");

            this.statusProvider.addState("FILTER_SEGMENT", "Filter segment width limit/exclude urls...");
            execute("de.ingrid.iplug.se.nutch.segment.SegmentFilter", filteredSegments, crawlDb, "-dir", segments);
            if (fs.getPath(filteredSegments).toFile().exists()) {
                removeRecursive(fs.getPath(segments));
                Files.move(fs.getPath(filteredSegments), fs.getPath(segments), StandardCopyOption.REPLACE_EXISTING);
            }
            this.statusProvider.appendToState("FILTER_SEGMENT", " done.");


            this.statusProvider.addState("UPDATE_WEBGRAPH", "Update web graph with new urls...");
            execute("org.apache.nutch.scoring.webgraph.WebGraph", "-webgraphdb", webgraph, "-segmentDir", segments);
            execute("org.apache.nutch.scoring.webgraph.LinkRank", "-webgraphdb", webgraph);
            execute("org.apache.nutch.scoring.webgraph.ScoreUpdater", "-webgraphdb", webgraph, "-crawldb", crawlDb);
            this.statusProvider.appendToState("UPDATE_WEBGRAPH", " done.");

            this.statusProvider.addState("FILTER_WEBGRAPH", "Filter web graph with limit/exclude urls...");
            // Usage: BWWebgraphFilter <webgraphdb> <bwdb> <normalize> <filter>
            // <replace current webgraph>
            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWWebgraphFilter", webgraph, bwDb, "false", "false", "true");
            this.statusProvider.appendToState("FILTER_WEBGRAPH", " done.");

            this.statusProvider.addState("UPDATE_LINKDB", "Update link database...");
            execute("org.apache.nutch.crawl.LinkDb", linkDb, "-dir", segments, "-noNormalize", "-noFilter");
            this.statusProvider.appendToState("UPDATE_LINKDB", " done.");

            this.statusProvider.addState("FILTER_LINKDB", "Filter link database with limit/exclude urls...");
            // Usage: BWLinkDbFilter <linkdb> <bwdb> <normalize> <filter>
            // <replace current linkdb>
            execute("de.ingrid.iplug.se.nutch.crawl.bw.BWLinkDbFilter", linkDb, bwDb, "false", "false", "true");
            this.statusProvider.appendToState("FILTER_LINKDB", " done.");

            this.statusProvider.addState("INDEX", "Create index...");
            execute("org.apache.nutch.indexer.IndexingJob", crawlDb, "-linkdb", linkDb, "-dir", segments, "-deleteGone");
            this.statusProvider.appendToState("INDEX", " done.");

            if (status == STATUS.RUNNING) {
                status = STATUS.FINISHED;
                this.statusProvider.addState("FINISHED", "Finished crawl.");
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
        // Debug specific call
        String debugOption = System.getProperty( "debugNutchCall" );
        if (debugOption != null && commandAndOptions[0].endsWith( debugOption )) {
            nutchCall = arrayConcat( nutchCall, new String[] { "-agentlib:jdwp=transport=dt_socket,address=7000,server=y,suspend=y" } );
        }
        nutchCall = arrayConcat(nutchCall, commandAndOptions);

        CommandLine cmdLine = new CommandLine(executable);
        cmdLine.addArguments(nutchCall);

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
        
        /**
         * FOR WINDOWS DEVELOPMENT TO RUN IN CYGWIN
         */
        if (System.getProperty( "runInCygwin" ) != null) {
            cmdLine = new CommandLine("C:\\cygwin\\bin\\bash.exe");
            cmdLine.addArgument( "-c" );
            
            String options = StringUtils.join(javaOptions, " ");
            String command = StringUtils.join(commandAndOptions, " ");
            // FOR DEBUGGING NUTCH
            // options += " -agentlib:jdwp=transport=dt_socket,address=7000,server=y,suspend=y";
            String call = "java -cp '" + cp + "' " + options + " " + command;
            // adding debug option
            call = call.replaceAll( "\\\\", "/" );
            cmdLine.addArgument( call );
        }
        /**
         * END
         */
        
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
