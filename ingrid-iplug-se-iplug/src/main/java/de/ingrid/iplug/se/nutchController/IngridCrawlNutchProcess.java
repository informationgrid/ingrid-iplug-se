/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.nutchController;

import de.ingrid.admin.Config;
import de.ingrid.admin.JettyStarter;
import de.ingrid.elasticsearch.ElasticsearchNodeFactoryBean;
import de.ingrid.elasticsearch.IndexInfo;
import de.ingrid.elasticsearch.IndexManager;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.iplug.IPostCrawlProcessor;
import de.ingrid.iplug.se.nutchController.StatusProvider.Classification;
import de.ingrid.iplug.se.utils.DBUtils;
import de.ingrid.iplug.se.utils.FileUtils;
import de.ingrid.iplug.se.webapp.container.Instance;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

/**
 * Wrapper for a ingrid specific nutch process execution. This is too complex to
 * execute this with a {@link GenericNutchProcess}.
 * 
 */
public class IngridCrawlNutchProcess extends NutchProcess {

    private static Logger log = Logger.getLogger(IngridCrawlNutchProcess.class);

    public static enum STATES {
        START, DELETE_BEFORE_CRAWL, INJECT_START, INJECT_BW, CLEANUP_HADOOP, FINISHED, DEDUPLICATE, INDEX, FILTER_LINKDB, UPDATE_LINKDB, FILTER_WEBGRAPH, UPDATE_WEBGRAPH, FILTER_SEGMENT, MERGE_SEGMENT, INJECT_META, FILTER_CRAWLDB, GENERATE, FETCH, UPDATE_CRAWLDB, UPDATE_MD, CREATE_HOST_STATISTICS, GENERATE_ZERO_URLS, CRAWL_CLEANUP, CLEAN_DUPLICATES, CREATE_STARTURL_REPORT, CREATE_URL_ERROR_REPORT;
    };

    public Integer depth = 1;

    Integer noUrls = 1;

    IPostCrawlProcessor[] postCrawlProcessors;

    Instance instance;

    LogFileWatcher logFileWatcher = null;

    private ElasticsearchNodeFactoryBean elasticSearch;

    final IndexManager indexManager;


    @Autowired
    public IngridCrawlNutchProcess(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Override
    public void run() {
        status = STATUS.RUNNING;

        try {
            // cleanup old crawl, in case the crawl has crashed
            IngridCrawlNutchProcessCleaner ingridCrawlNutchProcessCleaner = new IngridCrawlNutchProcessCleaner(this.statusProvider);
            if (ingridCrawlNutchProcessCleaner.cleanup(workingDirectory.toPath())) {
                // clear previously set states
                this.statusProvider.clear();
                this.statusProvider.appendToState(STATES.CRAWL_CLEANUP.name(), " done.");
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
            String statistic = fs.getPath(workingDirectory.getAbsolutePath(), "statistic").toString();

            String startUrls = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "start").toString();
            String metadata = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "metadata").toString();
            String limitPatterns = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "limit").toString();
            String excludePatterns = fs.getPath(workingDirectory.getAbsolutePath(), "urls", "exclude").toString();

            String segments = fs.getPath(workingDirectory.getAbsolutePath(), "segments").toString();
            String mergedSegments = fs.getPath(workingDirectory.getAbsolutePath(), "segments_merged").toString();
            String filteredSegments = fs.getPath(workingDirectory.getAbsolutePath(), "segments_filtered").toString();
            
            NutchConfigTool nutchConfigTool = new NutchConfigTool(Paths.get(instance.getWorkingDirectory(), "conf", "nutch-site.xml"));
            if ("true".equals( nutchConfigTool.getPropertyValue( "ingrid.delete.before.crawl" ) )) {
                this.statusProvider.addState(STATES.DELETE_BEFORE_CRAWL.name(), "Delete instance crawl data...");
                FileUtils.removeRecursive(fs.getPath( crawlDb ));
                FileUtils.removeRecursive(fs.getPath( bwDb ));
                FileUtils.removeRecursive(fs.getPath( mddb ));
                FileUtils.removeRecursive(fs.getPath( webgraph ));
                FileUtils.removeRecursive(fs.getPath( linkDb ));
                FileUtils.removeRecursive(fs.getPath( segments ));
                FileUtils.removeRecursive(fs.getPath( mergedSegments ));
                FileUtils.removeRecursive(fs.getPath( filteredSegments ));
                FileUtils.removeRecursive(fs.getPath( statistic ));
                
                this.statusProvider.appendToState(STATES.DELETE_BEFORE_CRAWL.name(), " done.");
            }

            // prepare (central) index for iPlug information
            if (JettyStarter.getInstance().config.esRemoteNode) {
                this.indexManager.checkAndCreateInformationIndex();
            }

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

            this.statusProvider.addState(STATES.CREATE_HOST_STATISTICS.name(), "Create hosts statistic...");
            ret = execute("de.ingrid.iplug.se.nutch.statistics.HostStatistic", crawlDb, workingPath);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.statistics.HostStatistic");
            }
            this.statusProvider.appendToState(STATES.CREATE_HOST_STATISTICS.name(), " done.");

            this.statusProvider.addState(STATES.CREATE_URL_ERROR_REPORT.name(), "Create url error statistic...");
            ret = execute("de.ingrid.iplug.se.nutch.statistics.UrlErrorReport", crawlDb, workingPath);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.statistics.UrlErrorReport");
            }
            this.statusProvider.appendToState(STATES.CREATE_URL_ERROR_REPORT.name(), " done.");
            
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
                        writeIndex(crawlDb, linkDb, segments);
                        cleanupHadoop();
                        status = STATUS.FINISHED;
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
                logFileWatcher = LogFileWatcherFactory.getFetchLogfileWatcher(Paths.get(instance.getWorkingDirectory(), "logs", "hadoop.log").toFile(), statusProvider, STATES.FETCH.name() + i);
                ret = execute("de.ingrid.iplug.se.nutch.fetcher.Fetcher", currentSegment);
                if (ret != 0) {
                    throwCrawlError("Error during Execution of: org.apache.nutch.fetcher.Fetcher");
                }
                logFileWatcher.close();
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

            this.statusProvider.addState(STATES.CREATE_HOST_STATISTICS.name() + 1, "Create hosts statistic...");
            ret = execute("de.ingrid.iplug.se.nutch.statistics.HostStatistic", crawlDb, workingPath);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.statistics.HostStatistic");
            }
            this.statusProvider.appendToState(STATES.CREATE_HOST_STATISTICS.name() + 1, " done.");

            this.statusProvider.addState(STATES.CREATE_STARTURL_REPORT.name(), "Create start url report...");
            ret = execute("de.ingrid.iplug.se.nutch.statistics.StartUrlStatusReport", crawlDb, startUrls, workingPath);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.statistics.StartUrlStatusReport");
            } else {
                // update start url status of this instance
                Path path = Paths.get(SEIPlug.conf.getInstancesDir(), instance.getName(), "statistic", "starturlreport", "data.json");

                String content = FileUtils.readFile(path);
                JSONParser parser = new JSONParser();
                JSONArray a = (JSONArray) parser.parse(content);
                for (Object o : a) {
                    JSONObject entry = (JSONObject) o;

                    String lastFetchTime = (String) entry.get("lastFetchTime");
                    String url = (String) entry.get("url");
                    String status = (String) entry.get("status");
                    // Float score = (Float) entry.get("score");
                    // Integer fetchIntervall = (Integer) entry.get("fetchInterval");

                    String urlStatus = lastFetchTime + " (" + status + ")";
                    DBUtils.setStatus(instance, url, urlStatus);
                }
            }
            this.statusProvider.appendToState(STATES.CREATE_STARTURL_REPORT.name(), " done.");

            this.statusProvider.addState(STATES.CREATE_URL_ERROR_REPORT.name() + 1, "Create url error statistic...");
            ret = execute("de.ingrid.iplug.se.nutch.statistics.UrlErrorReport", crawlDb, workingPath);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.statistics.UrlErrorReport");
            }
            this.statusProvider.appendToState(STATES.CREATE_URL_ERROR_REPORT.name() + 1, " done.");
            
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
            ret = execute("de.ingrid.iplug.se.nutch.segment.SegmentFilter", filteredSegments, crawlDb, "-dir", segments);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.segment.SegmentFilter");
            }
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
            logFileWatcher = LogFileWatcherFactory.getWepgraphLogfileWatcher(Paths.get(instance.getWorkingDirectory(), "logs", "hadoop.log").toFile(), statusProvider, STATES.UPDATE_WEBGRAPH.name());
            ret = execute("de.ingrid.iplug.se.nutch.scoring.webgraph.LinkRankWrapper", "-webgraphdb", webgraph);
            if (ret != 0) {
                throwCrawlError("Error during Execution of: de.ingrid.iplug.se.nutch.scoring.webgraph.LinkRankWrapper");
            }
            logFileWatcher.close();
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

            if (!("true".equals( nutchConfigTool.getPropertyValue( "ingrid.index.no.deduplication" ) ))) {
                this.statusProvider.addState(STATES.DEDUPLICATE.name(), "Deduplication...");
                logFileWatcher = LogFileWatcherFactory.getDeduplicationLogfileWatcher(Paths.get(instance.getWorkingDirectory(), "logs", "hadoop.log").toFile(), statusProvider, STATES.DEDUPLICATE.name());
                ret = execute("org.apache.nutch.crawl.DeduplicationJob", crawlDb);
                if (ret != 0) {
                    throwCrawlError("Error during Execution of: org.apache.nutch.crawl.DeduplicationJob");
                }
                logFileWatcher.close();
                this.statusProvider.appendToState(STATES.DEDUPLICATE.name(), " done.");
            }

            if ("true".equals( nutchConfigTool.getPropertyValue( "ingrid.delete.before.crawl" ) )) {
                // remove instance (type) from index
                Client client = elasticSearch.getClient();
                if (indexManager.indexExists( instance.getInstanceIndexName() )) {
                    indexManager.deleteIndex( instance.getInstanceIndexName() );
                }
            }
            writeIndex(crawlDb, linkDb, segments);

            if (!("true".equals( nutchConfigTool.getPropertyValue( "ingrid.index.no.deduplication" ) ))) {
                this.statusProvider.addState(STATES.CLEAN_DUPLICATES.name(), "Clean up duplicates in index...");
                logFileWatcher = LogFileWatcherFactory.getCleaningJobLogfileWatcher(Paths.get(instance.getWorkingDirectory(), "logs", "hadoop.log").toFile(), statusProvider, STATES.CLEAN_DUPLICATES.name());
                ret = execute("org.apache.nutch.indexer.CleaningJob", crawlDb);
                if (ret != 0) {
                    throwCrawlError("Error during Execution of: org.apache.nutch.indexer.CleaningJob");
                }
                logFileWatcher.close();
                this.statusProvider.appendToState(STATES.CLEAN_DUPLICATES.name(), " done.");
            }

            cleanupHadoop();

            if (status == STATUS.RUNNING) {
                status = STATUS.FINISHED;
                this.statusProvider.addState(STATES.FINISHED.name(), "Finished crawl.");

                if (postCrawlProcessors != null) {
                    for (IPostCrawlProcessor postCrawlProcessor : postCrawlProcessors) {
                        postCrawlProcessor.execute(instance);
                    }
                }
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
            if (logFileWatcher != null) {
                logFileWatcher.close();
            }
            try {
                this.statusProvider.write();
            } catch (IOException e) {
                log.warn("Crawl log could not be written");
            }
        }

    }
    
    private void writeIndex(String crawlDb, String linkDb, String segments) throws IOException, InterruptedException, ExecutionException {
        this.statusProvider.addState(STATES.INDEX.name(), "Create index...");

        String instanceIndexName = instance.getIndexName() + "_" + instance.getName();
        if (!this.indexManager.indexExists(instanceIndexName)) {
            this.indexManager.createIndex(instanceIndexName);
        }

        int ret = execute("org.apache.nutch.indexer.IndexingJob", crawlDb, "-linkdb", linkDb, "-dir", segments, "-deleteGone", "-noCommit");
        if (ret != 0) {
            throwCrawlError("Error during Execution of: org.apache.nutch.indexer.IndexingJob");
        }

        Config config = JettyStarter.getInstance().config;

        // update central index with iPlug information
        if (config.esRemoteNode) {
            IndexInfo info = new IndexInfo();
            info.setComponentIdentifier(config.communicationProxyUrl);
            info.setToAlias(instanceIndexName);
            info.setToType("default");
            String plugIdInfo = this.indexManager.getIndexTypeIdentifier(info);
            this.indexManager.updateIPlugInformation(
                    JettyStarter.getInstance().config.communicationProxyUrl,
                    getIPlugInfo( plugIdInfo, instanceIndexName, false, null, null ) );
        }

        this.statusProvider.appendToState(STATES.INDEX.name(), " done.");
    }

    private String getIPlugInfo(String infoId, String indexName, boolean running, Integer count, Integer totalCount) throws IOException {
        Config _config = JettyStarter.getInstance().config;

        return XContentFactory.jsonBuilder().startObject()
                .field( "plugId", _config.communicationProxyUrl )
                .field( "indexId", infoId )
                .field( "iPlugName", _config.datasourceName )
                .field( "linkedIndex", indexName )
                .field( "linkedType", "default" )
                .field( "adminUrl", _config.guiUrl )
                .field( "lastHeartbeat", new Date() )
                .field( "lastIndexed", new Date() )
                .startObject( "indexingState" )
                .field( "numProcessed", count )
                .field( "totalDocs", totalCount )
                .field( "running", running )
                .endObject()
                .endObject()
                .string();
    }
    
    private void cleanupHadoop() throws IOException {
        this.statusProvider.addState(STATES.CLEANUP_HADOOP.name(), "Clean up ...");
        FileUtils.removeRecursive(Paths.get(workingDirectory.getAbsolutePath(), "hadoop-tmp"));
        this.statusProvider.appendToState(STATES.CLEANUP_HADOOP.name(), " done.");
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
            if (debugOption != null && commandAndOptions[0].endsWith(debugOption)) {
                options += " -agentlib:jdwp=transport=dt_socket,address=7000,server=y,suspend=y";
            }
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

    public void setPostCrawlProcessors(IPostCrawlProcessor[] postCrawlProcessors) {
        this.postCrawlProcessors = postCrawlProcessors;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public void setElasticSearch(ElasticsearchNodeFactoryBean esBean) {
        this.elasticSearch = esBean;
    }
    
}
