package de.ingrid.iplug.se.nutchController;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import de.ingrid.iplug.se.nutchController.StatusProvider.Classification;
import de.ingrid.iplug.se.utils.FileUtils;

/**
 * Wrapper for a ingrid specific nutch process execution. This is too complex to
 * execute this with a {@link GenericNutchProcess}.
 * 
 */
public class IngridCrawlNutchProcess extends NutchProcess {

    private static Logger log = Logger.getLogger(IngridCrawlNutchProcess.class);

    public static enum STATES {
        START, INJECT_START, INJECT_BW, CLEANUP_HADOOP, FINISHED, INDEX, FILTER_LINKDB, UPDATE_LINKDB, FILTER_WEBGRAPH, UPDATE_WEBGRAPH, FILTER_SEGMENT, MERGE_SEGMENT, INJECT_META, FILTER_CRAWLDB, GENERATE, FETCH, UPDATE_CRAWLDB, UPDATE_MD, CREATE_HOST_STATISTICS, GENERATE_ZERO_URLS, CRAWL_CLEANUP;
    };

    Integer depth = 1;

    Integer noUrls = 1;

    @Override
    public void run() {
        status = STATUS.RUNNING;

        try {
            // cleanup old crawl, in case the crawl has crashed
            IngridCrawlNutchProcessCleaner ingridCrawlNutchProcessCleaner = new IngridCrawlNutchProcessCleaner(this.statusProvider);
            if (ingridCrawlNutchProcessCleaner.cleanup(workingDirectory.toPath())) {
                // clear previously set states
                this.statusProvider.clear();
                this.statusProvider.addState(STATES.CRAWL_CLEANUP.name(), "Crawl was cleaned up after crash or user abort.");
            } else {
                this.statusProvider.clear();
            }

            this.statusProvider.addState(STATES.START.name(), "Start crawl. [depth:" + depth + ";urls:" + noUrls + "]");
            this.statusProvider.setStateProperty(STATES.START.name(), "depth", depth.toString());
            this.statusProvider.setStateProperty(STATES.START.name(), "urls", noUrls.toString());

            FileSystem fs = FileSystems.getDefault();

            String workingPath = fs.getPath(workingDirectory.getAbsolutePath()).toString();
            this.statusProvider.setStateProperty(STATES.START.name(), "working.direcrory", workingPath);

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

            this.statusProvider.addState(STATES.INJECT_START.name(), "Inject start urls...");
            int ret = execute("org.apache.nutch.crawl.Injector", crawlDb, startUrls);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: org.apache.nutch.crawl.Injector");
            }
            this.statusProvider.appendToState(STATES.INJECT_START.name(), " done.");

            this.statusProvider.addState(STATES.INJECT_BW.name(), "Inject limit and exclude urls...");
            ret = execute("de.ingrid.iplug.se.nutch.crawl.bw.BWInjector", bwDb, limitPatterns, excludePatterns);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.crawl.bw.BWInjector");
            }
            this.statusProvider.appendToState(STATES.INJECT_BW.name(), " done.");

            this.statusProvider.addState(STATES.INJECT_META.name(), "Inject metadata...");
            ret = execute("de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector", mddb, metadata);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector");
            }
            this.statusProvider.appendToState(STATES.INJECT_META.name(), " done.");

            this.statusProvider.addState(STATES.FILTER_CRAWLDB.name(), "Filter crawldb by limit/exclude urls...");
            // Usage: <crawldb> <bwdb> <normalize> <filter> <replace current
            // crawldb>
            ret = execute("de.ingrid.iplug.se.nutch.crawl.bw.BWCrawlDbFilter", crawlDb, bwDb, "false", "false", "true");
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.crawl.bw.BWCrawlDbFilter");
            }
            this.statusProvider.appendToState(STATES.FILTER_CRAWLDB.name(), " done.");

            for (int i = 0; i < depth; i++) {
                this.statusProvider.addState(STATES.GENERATE.name() + i, "Generate up to " + noUrls.toString() + " urls for fetching " + "[" + (i + 1) + "/" + depth + "] ...");
                this.statusProvider.setStateProperty(STATES.GENERATE.name(), "i", Integer.toString(i));
                ret = execute("org.apache.nutch.crawl.Generator", crawlDb, segments, "-topN", noUrls.toString());
                if (ret != 0) {
                    log.warn("No URLs generated for fetch process. All urls fetched?");
                    this.statusProvider.addState(STATES.GENERATE_ZERO_URLS.name(), "No URLs generated for fetch process. All urls fetched?", Classification.WARN);
                    if (i == 0) {
                        // skip whole crawl process if this is the first
                        // generation.
                        // this should not be a problem, because no new segment
                        // should be generated
                        // TODO: check this (maybe check for multiple segments),
                        // because we cannot distinguish between zero URLs and
                        // error. :-(
                        this.status = STATUS.INTERRUPTED;
                        this.statusProvider.addState(STATES.FINISHED.name(), "Finished crawl.");
                        return;
                    } else {
                        // skip fetch, update crawldb, update metadata and
                        // continue.
                        break;
                    }
                }
                this.statusProvider.appendToState(STATES.GENERATE.name() + i, " done.");

                String currentSegment = fs.getPath(segments, getCurrentSegment(segments)).toString();

                this.statusProvider.addState(STATES.FETCH.name() + i, "Fetching " + "[" + (i + 1) + "/" + depth + "] ...");
                this.statusProvider.setStateProperty(STATES.FETCH.name(), "i", Integer.toString(i));
                ret = execute("de.ingrid.iplug.se.nutch.fetcher.Fetcher", currentSegment);
                if (ret != 0) {
                    throwCrawlError("Error during Execution of: org.apache.nutch.fetcher.Fetcher");
                }
                this.statusProvider.appendToState(STATES.FETCH.name() + i, " done.");

                this.statusProvider.addState(STATES.UPDATE_CRAWLDB.name() + i, "Update database with new urls and links " + "[" + (i + 1) + "/" + depth + "] ...");
                this.statusProvider.setStateProperty(STATES.UPDATE_CRAWLDB.name(), "i", Integer.toString(i));
                // Usage: <crawldb> <bwdb> <segment> <normalize> <filter>
                ret = execute("de.ingrid.iplug.se.nutch.crawl.bw.BWUpdateDb", crawlDb, bwDb, currentSegment, "true", "true");
                if (ret != 0) {
                    throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.crawl.bw.BWUpdateDb");
                }
                this.statusProvider.appendToState(STATES.UPDATE_CRAWLDB.name() + i, " done.");

                this.statusProvider.addState(STATES.UPDATE_MD.name() + i, "Update metadata for new urls " + "[" + (i + 1) + "/" + depth + "] ...");
                this.statusProvider.setStateProperty(STATES.UPDATE_MD.name(), "i", Integer.toString(i));
                ret = execute("de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater", mddb, currentSegment);
                if (ret != 0) {
                    throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater");
                }
                this.statusProvider.appendToState(STATES.UPDATE_MD.name() + i, " done.");
            }

            this.statusProvider.addState(STATES.CREATE_HOST_STATISTICS.name(), "Create hosts statistic...");
            ret = execute("de.ingrid.iplug.se.nutch.statistics.HostStatistic", crawlDb, workingPath);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.statistics.HostStatistic");
            }
            this.statusProvider.appendToState(STATES.CREATE_HOST_STATISTICS.name(), " done.");

            this.statusProvider.addState(STATES.MERGE_SEGMENT.name(), "Merge segments...");
            ret = execute("de.ingrid.iplug.se.nutch.segment.SegmentMerger", mergedSegments, "-dir", segments);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: org.apache.nutch.segment.SegmentMerger");
            }
            if (fs.getPath(mergedSegments).toFile().exists()) {
                FileUtils.removeRecursive(fs.getPath(segments));
                Files.move(fs.getPath(mergedSegments), fs.getPath(segments), StandardCopyOption.REPLACE_EXISTING);
            }
            this.statusProvider.appendToState(STATES.MERGE_SEGMENT.name(), " done.");

            this.statusProvider.addState(STATES.FILTER_SEGMENT.name(), "Filter segment by limit/exclude urls...");
            execute("de.ingrid.iplug.se.nutch.segment.SegmentFilter", filteredSegments, crawlDb, "-dir", segments);
            if (fs.getPath(filteredSegments).toFile().exists()) {
                FileUtils.removeRecursive(fs.getPath(segments));
                Files.move(fs.getPath(filteredSegments), fs.getPath(segments), StandardCopyOption.REPLACE_EXISTING);
            }
            this.statusProvider.appendToState(STATES.FILTER_SEGMENT.name(), " done.");

            this.statusProvider.addState(STATES.UPDATE_WEBGRAPH.name(), "Update web graph with new urls...");
            if (Files.exists(Paths.get(webgraph))) {
                FileUtils.removeRecursive(Paths.get(webgraph));
            }
            ret = execute("org.apache.nutch.scoring.webgraph.WebGraph", "-webgraphdb", webgraph, "-segmentDir", segments);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: org.apache.nutch.scoring.webgraph.WebGraph");
            }
            ret = execute("org.apache.nutch.scoring.webgraph.LinkRank", "-webgraphdb", webgraph);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: org.apache.nutch.scoring.webgraph.LinkRank");
            }
            ret = execute("org.apache.nutch.scoring.webgraph.ScoreUpdater", "-webgraphdb", webgraph, "-crawldb", crawlDb);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: org.apache.nutch.scoring.webgraph.ScoreUpdater");
            }
            this.statusProvider.appendToState(STATES.UPDATE_WEBGRAPH.name(), " done.");

            this.statusProvider.addState(STATES.UPDATE_LINKDB.name(), "Update link database...");
            ret = execute("org.apache.nutch.crawl.LinkDb", linkDb, "-dir", segments, "-noNormalize", "-noFilter");
            if (ret != 0) {
                throwCrawlError("Error during Execution of: org.apache.nutch.crawl.LinkDb");
            }
            this.statusProvider.appendToState(STATES.UPDATE_LINKDB.name(), " done.");

            this.statusProvider.addState(STATES.INDEX.name(), "Create index...");
            ret = execute("org.apache.nutch.indexer.IndexingJob", crawlDb, "-linkdb", linkDb, "-dir", segments, "-deleteGone");
            if (ret != 0) {
                throwCrawlError("Error during Execution of: org.apache.nutch.indexer.IndexingJob");
            }
            this.statusProvider.appendToState(STATES.INDEX.name(), " done.");

            this.statusProvider.addState(STATES.CLEANUP_HADOOP.name(), "Clean up ...");
            FileUtils.removeRecursive(Paths.get(workingDirectory.getAbsolutePath(), "hadoop-tmp"));
            this.statusProvider.appendToState(STATES.CLEANUP_HADOOP.name(), " done.");

            if (status == STATUS.RUNNING) {
                status = STATUS.FINISHED;
                this.statusProvider.addState(STATES.FINISHED.name(), "Finished crawl.");
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
        } catch (Throwable t) {
            status = STATUS.INTERRUPTED;
            log.error("Process exited with errors.", t);
        } finally {
            try {
                this.statusProvider.write();
            } catch (IOException e) {
                log.warn("Crawl log could not be written");
            }
        }

    }

    private void throwCrawlError(String string) throws IOException {
        this.statusProvider.addState(NutchProcess.STATES.ERROR.name(), string, Classification.ERROR);
        throw new IOException(string + ". Process exited with error code: " + resultHandler.getExitValue());
    }

    private int execute(String... commandAndOptions) throws IOException, InterruptedException {
        String cp = StringUtils.join(classPath, File.pathSeparator);

        String[] nutchCall = new String[] { "-cp", cp };
        nutchCall = arrayConcat(nutchCall, javaOptions);
        // Debug specific call
        String debugOption = System.getProperty("debugNutchCall");
        if (debugOption != null && commandAndOptions[0].endsWith(debugOption)) {
            nutchCall = arrayConcat(nutchCall, new String[] { "-agentlib:jdwp=transport=dt_socket,address=7000,server=y,suspend=y" });
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
        if (System.getProperty("runInCygwin") != null) {
            cmdLine = new CommandLine("C:\\cygwin\\bin\\bash.exe");
            cmdLine.addArgument("-c");

            String options = StringUtils.join(javaOptions, " ");
            String command = StringUtils.join(commandAndOptions, " ");
            // FOR DEBUGGING NUTCH
            // options +=
            // " -agentlib:jdwp=transport=dt_socket,address=7000,server=y,suspend=y";
            String call = "java -cp '" + cp + "' " + options + " " + command;
            // adding debug option
            call = call.replaceAll("\\\\", "/");
            cmdLine.addArgument(call);
        }
        /**
         * END
         */

        executor.execute(cmdLine, resultHandler);
        executor.getStreamHandler();
        resultHandler.waitFor();
        return resultHandler.getExitValue();
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
