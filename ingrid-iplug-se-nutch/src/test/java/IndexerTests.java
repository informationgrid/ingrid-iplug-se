/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */

import de.ingrid.iplug.se.nutch.crawl.bw.*;
import de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector;
import de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater;
import de.ingrid.iplug.se.nutch.statistics.HostStatistic;
import de.ingrid.iplug.se.nutch.statistics.StartUrlStatusReport;
import de.ingrid.iplug.se.nutch.statistics.UrlErrorReport;
import org.apache.hadoop.conf.Configuration;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class IndexerTests {

    @Test
    public void test00DetelteTestData() throws Exception {
        delete(new File("test"));
    }

    @Test
    public void test01InjectStartURLs() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new Injector(), new String[] { "test/crawldb", "src/test/resources/urls/start" });
    }

    @Test
    public void test02InjectBWURLs() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new BWInjector(), new String[] { "test/bwdb", "src/test/resources/urls/limit", "src/test/resources/urls/exclude" });
    }

    @Test
    public void test03InjectMetadata() throws Exception {
        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new MetadataInjector(), new String[] { "test/metadatadb", "src/test/resources/urls/metadata" });
    }

    @Test
    public void test04FilterCrawlDB() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new BWCrawlDbFilter(), new String[] { "test/crawldb", "test/bwdb", "false", "false", "true" });
    }

    @Test
    public void test05_1Generate() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new Generator(), new String[] { "test/crawldb", "test/segments", "-topN", "10" });
    }

    @Test
    public void test05_2Fetch() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        
        Arrays.sort(directories);

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new Fetcher(), new String[] { "test/segments/" + directories[directories.length - 1] });
    }

    @Test
    public void test05_3UpdateCrawlDb() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        
        Arrays.sort(directories);

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new BWUpdateDb(), new String[] { "test/crawldb", "test/bwdb", "test/segments/" + directories[directories.length - 1], "true", "true" });
    }

    @Test
    public void test05_4ParseDataUpdater() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        
        Arrays.sort(directories);

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new ParseDataUpdater(), new String[] { "test/metadatadb", "test/segments/" + directories[directories.length - 1] });
    }

    @Test
    public void test06_01Generate() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new Generator(), new String[] { "test/crawldb", "test/segments", "-topN", "10" });
    }

    @Test
    public void test06_02Fetch() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        
        Arrays.sort(directories);

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new Fetcher(), new String[] { "test/segments/" + directories[directories.length - 1] });
    }

    @Test
    public void test06_03UpdateCrawlDb() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new BWUpdateDb(), new String[] { "test/crawldb", "test/bwdb", "test/segments/" + directories[directories.length - 1], "true", "true" });
    }

    @Test
    public void test06_04ParseDataUpdater() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new ParseDataUpdater(), new String[] { "test/metadatadb", "test/segments/" + directories[directories.length - 1] });
    }    
    
    
    @Test
    public void test09_1HostStatistics() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new HostStatistic(), new String[] { "test/crawldb", "test" });
    }

    @Test
    public void test09_2StartUrlReport() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new StartUrlStatusReport(), new String[] { "test/crawldb", "src/test/resources/urls/start", "test" });
    }

    @Test
    public void test09_3UrlErrorReport() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new UrlErrorReport(), new String[] { "test/crawldb", "test" });
    }
    
    @Test
    public void test10WebgraphCreate() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new WebGraph(), new String[] { "-webgraphdb", "test/webgraph", "-segmentDir", "test/segments" });
    }

    @Test
    public void test11LinkRank() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new LinkRank(), new String[] { "-webgraphdb", "test/webgraph" });
    }

    @Test
    public void test12ScoreUpdater() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new ScoreUpdater(), new String[] { "-webgraphdb", "test/webgraph", "-crawldb", "test/crawldb" });
    }

    @Test
    public void test13WebgraphFilter() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new BWWebgraphFilter(), new String[] { "test/webgraph", "test/bwdb", "false", "false", "true" });
    }

    @Test
    public void test14MergeSegments() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        if (directories.length > 1) {

            Configuration c = NutchConfiguration.create();
            c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
            ToolRunner.run(c, new SegmentMerger(), new String[] { "test/merged_segment", "-dir", "test/segments" });
//            SegmentMerger.main(new String[] { "test/merged_segment", "-dir", "test/segments" });
            FileSystem fs = FileSystems.getDefault();
            delete(fs.getPath("test/segments").toFile());
            Files.move(fs.getPath("test/merged_segment"), fs.getPath("test/segments"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    public void test16InvertLinks() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new LinkDb(), new String[] { "test/linkdb", "-dir", "test/segments", "-noNormalize", "-noFilter" });
    }

    @Test
    public void test17FilterLinkdb() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new BWLinkDbFilter(), new String[] { "test/linkdb", "test/bwdb", "false", "false", "true" });
    }

    @Test
    public void test17_5DeleteDuplicates() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new BWLinkDbFilter(), new String[] { "test/linkdb", "test/bwdb", "false", "false", "true" });
    }

    
    @Test
    public void test18Index() throws Exception {

        Configuration c = NutchConfiguration.create();
        c.set("plugin.folders", "build/apache-nutch-1.19/runtime/local/plugins");
        ToolRunner.run(c, new IndexingJob(), new String[] { "test/crawldb", "-linkdb", "test/linkdb", "-dir", "test/segments", "-deleteGone" });
    }

    void delete(File f) throws IOException {
        if (Files.exists(f.toPath())) {
            Files.walk(f.toPath())
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        if (Files.exists(f.toPath())) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

}
