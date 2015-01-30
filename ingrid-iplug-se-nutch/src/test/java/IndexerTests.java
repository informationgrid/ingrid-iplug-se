/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2015 wemove digital solutions GmbH
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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.crawl.Injector;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.indexer.IndexingJob;
import org.apache.nutch.scoring.webgraph.LinkRank;
import org.apache.nutch.scoring.webgraph.ScoreUpdater;
import org.apache.nutch.scoring.webgraph.WebGraph;
import org.apache.nutch.util.NutchConfiguration;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
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
import de.ingrid.iplug.se.nutch.fetcher.Fetcher;
import de.ingrid.iplug.se.nutch.segment.SegmentFilter;
import de.ingrid.iplug.se.nutch.segment.SegmentMerger;
import de.ingrid.iplug.se.nutch.statistics.HostStatistic;
import de.ingrid.iplug.se.nutch.statistics.StartUrlStatusReport;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexerTests {

    @Test
    public void test00DetelteTestData() throws Exception {
        delete(new File("test"));
    }

    @Test
    public void test01InjectStartURLs() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new Injector(), new String[] { "test/crawldb", "src/test/resources/urls/start" });
    }

    @Test
    public void test02InjectBWURLs() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new BWInjector(), new String[] { "test/bwdb", "src/test/resources/urls/limit", "src/test/resources/urls/exclude" });
    }

    @Test
    public void test03InjectMetadata() throws Exception {
        ToolRunner.run(NutchConfiguration.create(), new MetadataInjector(), new String[] { "test/metadatadb", "src/test/resources/urls/metadata" });
    }

    @Test
    public void test04FilterCrawlDB() throws Exception {

        Settings settings = ImmutableSettings.settingsBuilder().put("path.data", "test").put("transport.tcp.port", 54346).put("http.port", 54347).build();
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().clusterName("elasticsearch").data(true).settings(settings);
        nodeBuilder = nodeBuilder.local(false);
        Node node = nodeBuilder.node();

        ToolRunner.run(NutchConfiguration.create(), new BWCrawlDbFilter(), new String[] { "test/crawldb", "test/bwdb", "false", "false", "true" });

        node.close();
    }

    @Test
    public void test05_1Generate() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new Generator(), new String[] { "test/crawldb", "test/segments", "-topN", "10" });
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

        ToolRunner.run(NutchConfiguration.create(), new Fetcher(), new String[] { "test/segments/" + directories[directories.length - 1] });
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

        ToolRunner.run(NutchConfiguration.create(), new BWUpdateDb(), new String[] { "test/crawldb", "test/bwdb", "test/segments/" + directories[directories.length - 1], "true", "true" });
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

        ToolRunner.run(NutchConfiguration.create(), new ParseDataUpdater(), new String[] { "test/metadatadb", "test/segments/" + directories[directories.length - 1] });
    }

    @Test
    public void test06_01Generate() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new Generator(), new String[] { "test/crawldb", "test/segments", "-topN", "10" });
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

        ToolRunner.run(NutchConfiguration.create(), new Fetcher(), new String[] { "test/segments/" + directories[directories.length - 1] });
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

        ToolRunner.run(NutchConfiguration.create(), new BWUpdateDb(), new String[] { "test/crawldb", "test/bwdb", "test/segments/" + directories[directories.length - 1], "true", "true" });
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

        ToolRunner.run(NutchConfiguration.create(), new ParseDataUpdater(), new String[] { "test/metadatadb", "test/segments/" + directories[directories.length - 1] });
    }    
    
    
    @Test
    public void test09_1HostStatistics() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new HostStatistic(), new String[] { "test/crawldb", "test" });
    }

    @Test
    public void test09_2StartUrlReport() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new StartUrlStatusReport(), new String[] { "test/crawldb", "src/test/resources/urls/start", "test" });
    }
    
    @Test
    public void test10WebgraphCreate() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new WebGraph(), new String[] { "-webgraphdb", "test/webgraph", "-segmentDir", "test/segments" });
    }

    @Test
    public void test11LinkRank() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new LinkRank(), new String[] { "-webgraphdb", "test/webgraph" });
    }

    @Test
    public void test12ScoreUpdater() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new ScoreUpdater(), new String[] { "-webgraphdb", "test/webgraph", "-crawldb", "test/crawldb" });
    }

    @Test
    public void test13WebgraphFilter() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new BWWebgraphFilter(), new String[] { "test/webgraph", "test/bwdb", "false", "false", "true" });
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
            SegmentMerger.main(new String[] { "test/merged_segment", "-dir", "test/segments" });
            FileSystem fs = FileSystems.getDefault();
            delete(fs.getPath("test/segments").toFile());
            Files.move(fs.getPath("test/merged_segment"), fs.getPath("test/segments"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    public void test15SegmentFilter() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new SegmentFilter(), new String[] { "test/filtered_segment", "test/crawldb", "-dir", "test/segments" });
        FileSystem fs = FileSystems.getDefault();
        if (fs.getPath("test/filtered_segment").toFile().exists()) {
            delete(fs.getPath("test/segments").toFile());
            Files.move(fs.getPath("test/filtered_segment"), fs.getPath("test/segments"), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    public void test16InvertLinks() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new LinkDb(), new String[] { "test/linkdb", "-dir", "test/segments", "-noNormalize", "-noFilter" });
    }

    @Test
    public void test17FilterLinkdb() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new BWLinkDbFilter(), new String[] { "test/linkdb", "test/bwdb", "false", "false", "true" });
    }

    @Test
    public void test17_5DeleteDuplicates() throws Exception {

        ToolRunner.run(NutchConfiguration.create(), new BWLinkDbFilter(), new String[] { "test/linkdb", "test/bwdb", "false", "false", "true" });
    }

    
    @Test
    public void test18Index() throws Exception {

        Settings settings = ImmutableSettings.settingsBuilder().put("path.data", "test").put("transport.tcp.port", 54346).put("http.port", 54347).build();
        NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().clusterName("elasticsearch").data(true).settings(settings);
        nodeBuilder = nodeBuilder.local(false);
        Node node = nodeBuilder.node();

        ToolRunner.run(NutchConfiguration.create(), new IndexingJob(), new String[] { "test/crawldb", "-linkdb", "test/linkdb", "-dir", "test/segments", "-deleteGone" });

        node.close();
    }

    /*
     * 
     * 
     * 
     * 
     * 
     * 
     * // fetch File file = new File("test/segments"); String[] directories =
     * file.list(new FilenameFilter() {
     * 
     * @Override public boolean accept(File current, String name) { return new
     * File(current, name).isDirectory(); } });
     * 
     * List<String> fetchCall = new ArrayList<String>();
     * fetchCall.addAll(baseCall);
     * fetchCall.add("org.apache.nutch.fetcher.Fetcher");
     * fetchCall.add("test/segments/" + directories[0]);
     * 
     * System.out.println(StringUtils.join(fetchCall, " ")); pb = new
     * ProcessBuilder(fetchCall); pb.directory(new File("."));
     * pb.redirectErrorStream(true); process = pb.start();
     * 
     * s = new Scanner( process.getInputStream() ).useDelimiter( "\\Z" ); while
     * (s.hasNextLine()) { System.out.println( s.nextLine() ); } s.close();
     * System.out.println("Exited with code " + process.waitFor());
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * 
     * }
     */

    private void executeCall(List<String> injectCall) throws IOException, InterruptedException {
        System.out.println(StringUtils.join(injectCall, " "));
        ProcessBuilder pb = new ProcessBuilder(injectCall);
        Map<String, String> localEnvMap = System.getenv();
        Map<String, String> env = pb.environment();
        for (String key : localEnvMap.keySet()) {
            env.put(key, localEnvMap.get(key));
        }
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        Process process = pb.start();

        Scanner s = new Scanner(process.getInputStream());
        while (s.hasNextLine()) {
            System.out.println(s.nextLine());
        }
        s.close();
        System.out.println("Exited with code " + process.waitFor());
    }

    private List<String> setUpBaseCall() {

        String[] classPath = new String[] { "src/test/resources/conf", "build/apache-nutch-1.9/runtime/local", "build/apache-nutch-1.9/runtime/local/lib/*" };

        String cp = StringUtils.join(classPath, File.pathSeparator);

        List<String> baseCall = new ArrayList<String>();
        baseCall.add("java");
        baseCall.add("-cp");
        baseCall.add(cp);
        baseCall.add("-Xmx512m");
        baseCall.add("-Dhadoop.log.dir=logs");
        baseCall.add("-Dhadoop.log.file=hadoop.log");

        return baseCall;

    }

    void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles())
                delete(c);
        }
        if (!f.delete())
            throw new FileNotFoundException("Failed to delete file: " + f);
    }

}
