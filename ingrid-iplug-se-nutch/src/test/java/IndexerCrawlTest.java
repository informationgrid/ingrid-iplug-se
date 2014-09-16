import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.crawl.Injector;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.indexer.IndexingJob;
import org.apache.nutch.scoring.webgraph.LinkRank;
import org.apache.nutch.scoring.webgraph.ScoreUpdater;
import org.apache.nutch.scoring.webgraph.WebGraph;
import org.apache.nutch.segment.SegmentMerger;
import org.apache.nutch.util.NutchConfiguration;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.ingrid.iplug.se.nutch.crawl.bw.BWCrawlDbFilter;
import de.ingrid.iplug.se.nutch.crawl.bw.BWInjector;
import de.ingrid.iplug.se.nutch.crawl.bw.BWLinkDbFilter;
import de.ingrid.iplug.se.nutch.crawl.bw.BWUpdateDb;
import de.ingrid.iplug.se.nutch.crawl.bw.BWWebgraphFilter;
import de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector;
import de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater;
import de.ingrid.iplug.se.nutch.segment.SegmentFilter;
import de.ingrid.iplug.se.nutch.tools.HostStatistic;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexerCrawlTest {

    @Test
    public void testCrawl() throws Exception {

        delete(new File("test"));

        ToolRunner.run(NutchConfiguration.create(), new Injector(), new String[] { "test/crawldb", "src/test/resources/urls/start" });
 
        BWInjector.main(new String[] { "test/bwdb", "src/test/resources/urls/limit", "src/test/resources/urls/exclude" });

        MetadataInjector.main(new String[] { "test/metadatadb", "src/test/resources/urls/metadata" });

        BWCrawlDbFilter.main(new String[] { "test/crawldb", "test/bwdb", "false", "false", "true" });

        for (int i = 0; i < 2; i++) {
            ToolRunner.run(NutchConfiguration.create(), new Generator(), new String[] { "test/crawldb", "test/segments" });

            String[] segments = getSegments("test/segments");
            ToolRunner.run(NutchConfiguration.create(), new Fetcher(), new String[] { "test/segments/" + segments[segments.length - 1] });

            BWUpdateDb.main(new String[] { "test/crawldb", "test/bwdb", "test/segments/" + segments[segments.length - 1], "true", "true" });

            HostStatistic.main(new String[] { "test/crawldb", "test/segments/" + segments[segments.length - 1] });

            ParseDataUpdater.main(new String[] { "test/metadatadb", "test/segments/" + segments[segments.length - 1] });
        }

        ToolRunner.run(NutchConfiguration.create(), new WebGraph(), new String[] { "-webgraphdb", "test/webgraph", "-segmentDir", "test/segments" });
        ToolRunner.run(NutchConfiguration.create(), new LinkRank(), new String[] { "-webgraphdb", "test/webgraph" });
        ToolRunner.run(NutchConfiguration.create(), new ScoreUpdater(), new String[] { "-webgraphdb", "test/webgraph", "-crawldb", "test/crawldb" });

        BWWebgraphFilter.main(new String[] { "test/webgraph", "test/bwdb", "false", "false", "true" });

        SegmentMerger.main(new String[] { "test/merged_segment", "-dir", "test/segments" });

        SegmentFilter.main(new String[] { "test/filtered_segment", "test/crawldb", "-dir", "test/merged_segment" });

        ToolRunner.run(NutchConfiguration.create(), new LinkDb(), new String[] { "test/linkdb", "-dir", "test/filtered_segment", "-noNormalize", "-noFilter" });

        BWLinkDbFilter.main(new String[] { "test/linkdb", "test/bwdb", "false", "false", "true" });

        ToolRunner.run(NutchConfiguration.create(), new IndexingJob(), new String[] { "test/crawldb", "-linkdb", "test/linkdb", "-dir", "test/filtered_segment", "-deleteGone" });

    }

    void delete(File f) throws IOException {
        if (!f.exists()) return;
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

    private String[] getSegments(String path) {

        File file = new File(path);
        String[] segments = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        Arrays.sort(segments);

        return segments;

    }
}
