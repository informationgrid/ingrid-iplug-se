package de.ingrid.iplug.se.nutchController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

import de.ingrid.iplug.se.nutchController.IngridCrawlNutchProcess.STATES;
import de.ingrid.iplug.se.nutchController.StatusProvider.State;
import de.ingrid.iplug.se.utils.FileUtils;

/**
 * Cleans up an crashed SE instance.
 * 
 */
public class IngridCrawlNutchProcessCleaner {

    private static Logger LOG = Logger.getLogger(IngridCrawlNutchProcessCleaner.class);

    private StatusProvider statusProvider = null;

    public IngridCrawlNutchProcessCleaner(StatusProvider statusProvider) {
        this.statusProvider = statusProvider;
    }

    /**
     * Cleans up the ingrid nutch crawl instance directory in case the crawl has crashed or was interrupted by the user.
     * 
     * @param workingDirectory
     * @return False if nothing had to cleanup. True if the crawl was cleaned up.
     * @throws Exception
     */
    public boolean cleanup(Path workingDirectory) throws Exception {

        try {

            // if no state exist, return
            if (statusProvider.getStates().size() == 0) {
                LOG.info("First time crawl detected, no state file found.");
                return false;
            }
            State[] states = statusProvider.getStates().toArray(new State[0]);
            int i = states.length - 1;
            State lastState = states[i];
            while (lastState.key.equals(NutchProcess.STATES.ERROR.name()) || lastState.key.equals(NutchProcess.STATES.ABORT.name()) || i == 0) {
                lastState = states[--i];
            }
            LOG.info("Last state '" + lastState.getKey() + "' detected.");

            if (lastState.key.equals(IngridCrawlNutchProcess.STATES.FINISHED.name())) {
                // nothing todo
                LOG.info("Last Crawl was finished normally, nothing to do.");
                return false;
            }

            Path hadoopTmp = Paths.get(workingDirectory.toAbsolutePath().toString(), "hadoop-tmp");

            if (lastState.key.equals(IngridCrawlNutchProcess.STATES.INJECT_START.name())) {
                // nothing to do, HADOOP_TMP/inject-temp-* will be removed by
                // deleting HADOOM_TMP
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.INJECT_BW.name())) {
                // nothing to do, HADOOP_TMP/bw-inject-temp-* will be removed by
                // deleting HADOOM_TMP
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.INJECT_META.name())) {
                // nothing to do, HADOOP_TMP/metadata-inject-temp-* will be
                // removed by deleting HADOOM_TMP
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.FILTER_CRAWLDB.name())) {
                // nothing todo, since all temp data will be removed by the
                // crawldb cleansing
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.GENERATE.name())) {
                // remove last segment, HADOOP_TMP/generate-temp-* will be
                // removed by deleting HADOOM_TMP
                try {
                    int idx = Integer.parseInt(statusProvider.getStateProperty(STATES.GENERATE.name(), "i"));
                    String[] segments = FileUtils.getSortedSubDirectories(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments"));
                    if (segments.length == idx + 1) {
                        LOG.info("Remove orphaned segment: " + segments[idx]);
                        FileUtils.removeRecursive(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segments[idx]));
                    }

                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.GENERATE.name() + "'.", e);
                }
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.FETCH.name())) {
                // remove last segment
                try {
                    String[] segments = FileUtils.getSortedSubDirectories(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments"));
                    if (segments.length > 0) {
                        LOG.info("Remove orphaned segment: " + segments[segments.length - 1]);
                        FileUtils.removeRecursive(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segments[segments.length - 1]));
                    }
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.FETCH.name() + "'.", e);
                }

            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.UPDATE_CRAWLDB.name())) {
                // remove last segment, other temp directories will be removed
                // by the crawldb cleansing
                try {
                    String[] segments = FileUtils.getSortedSubDirectories(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments"));
                    if (segments.length > 0) {
                        LOG.info("Remove orphaned segment: " + segments[segments.length - 1]);
                        FileUtils.removeRecursive(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segments[segments.length - 1]));
                    }
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.UPDATE_CRAWLDB.name() + "'.", e);
                }
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.UPDATE_MD.name())) {
                // remove last segment, HADOOP_TMP/metadata-* will be removed by
                // deleting HADOOM_TMP
                try {
                    String[] segments = FileUtils.getSortedSubDirectories(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments"));
                    if (segments.length > 0) {
                        LOG.info("Remove orphaned segment: " + segments[segments.length - 1]);
                        FileUtils.removeRecursive(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segments[segments.length - 1]));
                    }
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.UPDATE_MD.name() + "'.", e);
                }
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.CREATE_HOST_STATISTICS.name())) {
                // nothing todo, since all temp data will be removed by the
                // crawldb cleansing
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.MERGE_SEGMENT.name())) {
                // remove segments_merged, move it, if no segments dir exists
                // because then the merge process has already completed
                try {
                    Path mergedSegments = Paths.get(workingDirectory.toAbsolutePath().toString(), "segments_merged");
                    Path segments = Paths.get(workingDirectory.toAbsolutePath().toString(), "segments");
                    if (Files.exists(mergedSegments)) {
                        if (Files.exists(segments)) {
                            LOG.info("Remove temp segments_merge directory.");
                            FileUtils.removeRecursive(mergedSegments);
                        } else {
                            LOG.info("Rename temp segments_merge directory to segments.");
                            Files.move(mergedSegments, segments, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.MERGE_SEGMENT.name() + "'.", e);
                }
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.FILTER_SEGMENT.name())) {
                // remove segments_filtered, move it, if no segments dir exists
                // because then the filter process has already completed
                try {
                    Path filteredSegments = Paths.get(workingDirectory.toAbsolutePath().toString(), "segments_filtered");
                    Path segments = Paths.get(workingDirectory.toAbsolutePath().toString(), "segments");
                    if (Files.exists(filteredSegments)) {
                        if (Files.exists(segments)) {
                            LOG.info("Remove temp segments_filtered directory.");
                            FileUtils.removeRecursive(filteredSegments);
                        } else {
                            LOG.info("Rename temp segments_filtered directory to segments.");
                            Files.move(filteredSegments, segments, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.FILTER_SEGMENT.name() + "'.", e);
                }
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.UPDATE_WEBGRAPH.name())) {
                // nothing to do, because the webgraph will be newly created
                // every crawl
            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.UPDATE_LINKDB.name())) {
                // remove WORKING_DIR/linkdb-merge-.*
                try {
                    LOG.info("Remove linkdb-merge-.* from instance directories.");
                    FileUtils.removeRecursive(workingDirectory, "linkdb-merge-.*");
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.UPDATE_LINKDB.name() + "'.", e);
                }

            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.UPDATE_LINKDB.name())) {
                // remove WORKING_DIR/linkdb-merge-.*
                try {
                    LOG.info("Remove linkdb-merge-.* from instance directories.");
                    FileUtils.removeRecursive(workingDirectory, "linkdb-merge-.*");
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.UPDATE_LINKDB.name() + "'.", e);
                }

            } else if (lastState.key.equals(IngridCrawlNutchProcess.STATES.INDEX.name())) {
                // remove WORKING_DIR/tmp_.*
                try {
                    LOG.info("Remove tmp_.* from instance directories.");
                    FileUtils.removeRecursive(workingDirectory, "tmp_.*");
                } catch (IOException e) {
                    LOG.error("Error recovering from state '" + STATES.INDEX.name() + "'.", e);
                }
            }

            // remove invalid segments
            String[] segments = FileUtils.getSortedSubDirectories(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments"));
            for (String segment : segments) {
                if (!Files.exists(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segment, "crawl_generate")) || !Files.exists(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segment, "crawl_fetch"))
                        || !Files.exists(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segment, "crawl_parse"))
                        || !Files.exists(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segment, "parse_data"))
                        || !Files.exists(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segment, "parse_text"))) {
                    LOG.info("Remove invalid segment: " + segment);
                    FileUtils.removeRecursive(Paths.get(workingDirectory.toAbsolutePath().toString(), "segments", segment));
                }
            }

            // crawldb cleansing
            try {
                LOG.info("Remove temp crawldb directories detected by regex '^[0-9]+$'.");
                FileUtils.removeRecursive(Paths.get(workingDirectory.toAbsolutePath().toString(), "crawldb"), "^[0-9]+$");
            } catch (IOException e) {
                LOG.error("Error removing directories " + Paths.get(workingDirectory.toAbsolutePath().toString(), "crawldb") + " with regex '" + "^[0-9]+$" + "'.");
            }

            // remove hadoop-temp directory
            try {
                LOG.info("Remove hadoop temp directory.");
                FileUtils.removeRecursive(hadoopTmp);
            } catch (IOException e) {
                LOG.error("Error removing directory " + hadoopTmp);
            }
            
            return true;

        } catch (Throwable t) {
            LOG.error("Error cleanup instance.", t);
            throw new Exception(t);

        }

    }

}
