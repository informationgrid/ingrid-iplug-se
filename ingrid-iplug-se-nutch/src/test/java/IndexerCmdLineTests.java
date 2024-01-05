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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.settings.Settings;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class IndexerCmdLineTests {

    @Test
    public void test01InjectStartURLs() throws IOException, InterruptedException {

        delete(new File("test"));

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.crawl.Injector");
        call.add("test/crawldb");
        call.add("src/test/resources/urls/start");

        executeCall(call);
    }

    @Test
    public void test02InjectBWURLs() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.bw.BWInjector");
        call.add("test/bwdb");
        call.add("src/test/resources/urls/limit");
        call.add("src/test/resources/urls/exclude");

        executeCall(call);
    }

    @Test
    public void test03InjectMetadata() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector");
        call.add("test/metadatadb");
        call.add("src/test/resources/urls/metadata");

        executeCall(call);
    }

    @Test
    public void test04FilterCrawlDB() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.bw.BWCrawlDbFilter");
        call.add("test/crawldb");
        call.add("test/bwdb");
        call.add("false");
        call.add("false");
        call.add("true");

        executeCall(call);
    }

    @Test
    public void test05Generate() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.crawl.Generator");
        call.add("test/crawldb");
        call.add("test/segments");

        executeCall(call);
    }

    @Test
    public void test06Fetch() throws IOException, InterruptedException {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add(org.apache.nutch.fetcher.Fetcher.class.getName());
        call.add("test/segments/" + directories[directories.length - 1]);

        executeCall(call);
    }

    @Test
    public void test08UpdateCrawlDb() throws IOException, InterruptedException {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.bw.BWUpdateDb");
        call.add("test/crawldb");
        call.add("test/bwdb");
        call.add("test/segments/" + directories[directories.length - 1]);
        call.add("true");
        call.add("true");

        executeCall(call);
    }

    @Test
    public void test09ParseDataUpdater() throws IOException, InterruptedException {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater");
        call.add("test/metadatadb");
        call.add("test/segments/" + directories[directories.length - 1]);

        executeCall(call);
    }

    @Test
    public void test09_1HostStatistics() throws IOException, InterruptedException {

        // create statistics
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.statistics.HostStatistic");
        call.add("test/crawldb");
        call.add("test");

        executeCall(call);
    }

    @Test
    public void test09_2StartUrlStatusReport() throws IOException, InterruptedException {

        // create statistics
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.statistics.StartUrlStatusReport");
        call.add("test/crawldb");
        call.add("src/test/resources/urls/start");
        call.add("test");

        executeCall(call);
    }

    
    @Test
    public void test09_3UrlErrorReport() throws IOException, InterruptedException {

        // create statistics
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.statistics.UrlErrorReport");
        call.add("test/crawldb");
        call.add("test");

        executeCall(call);
    }
    
    
    @Test
    public void test10WebgraphCreate() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.scoring.webgraph.WebGraph");
        call.add("-webgraphdb");
        call.add("test/webgraph");
        call.add("-segment");
        call.add("test/segments/" + directories[directories.length - 1]);

        executeCall(call);
    }

    @Test
    public void test11LinkRank() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.scoring.webgraph.LinkRank");
        call.add("-webgraphdb");
        call.add("test/webgraph");

        executeCall(call);
    }

    @Test
    public void test12ScoreUpdater() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.scoring.webgraph.ScoreUpdater");
        call.add("-webgraphdb");
        call.add("test/webgraph");
        call.add("-crawldb");
        call.add("test/crawldb");

        executeCall(call);
    }

    @Test
    public void test13WebgraphFilter() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.bw.BWWebgraphFilter");
        call.add("test/webgraph");
        call.add("test/bwdb");
        call.add("false");
        call.add("false");
        call.add("true");

        executeCall(call);
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
            // inject start urls
            List<String> call = new ArrayList<>();
            call.addAll(setUpBaseCall());
            call.add("de.ingrid.iplug.se.nutch.segment.SegmentMerger");
            call.add("test/merged_segment");
            call.add("-dir");
            call.add("test/segments");
            executeCall(call);
            FileSystem fs = FileSystems.getDefault();
            if (fs.getPath("test/merged_segment").toFile().exists()) {
                delete(fs.getPath("test/segments").toFile());
                Files.move(fs.getPath("test/merged_segment"), fs.getPath("test/segments"), StandardCopyOption.REPLACE_EXISTING);
            }

        }
    }

    @Test
    public void test15SegmentFilter() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.segment.SegmentFilter");
        call.add("test/filtered_segment");
        call.add("test/crawldb");
        call.add("-dir");
        call.add("test/segments");

        executeCall(call);
        FileSystem fs = FileSystems.getDefault();
        if (fs.getPath("test/filtered_segment").toFile().exists()) {
            delete(fs.getPath("test/segments").toFile());
            Files.move(fs.getPath("test/filtered_segment"), fs.getPath("test/segments"), StandardCopyOption.REPLACE_EXISTING);
        }

    }

    @Test
    public void test16InvertLinks() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.crawl.LinkDb");
        call.add("test/linkdb");
        call.add("-dir");
        call.add("test/segments");
        call.add("-noNormalize");
        call.add("-noFilter");

        executeCall(call);
    }

    @Test
    public void test17FilterLinkdb() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.bw.BWLinkDbFilter");
        call.add("test/linkdb");
        call.add("test/bwdb");
        call.add("false");
        call.add("false");
        call.add("true");

        executeCall(call);
    }

    @Test
    public void test18Index() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.indexer.IndexingJob");
        call.add("test/crawldb");
        call.add("-linkdb");
        call.add("test/linkdb");
        call.add("-dir");
        call.add("test/segments");
        call.add("-deleteGone");

        Settings settings = Settings.builder()
                .put("path.data", "test")
                .put("transport.tcp.port", 54346)
                .put("http.port", 54347)
                .build();
        /*NodeBuilder nodeBuilder = NodeBuilder.nodeBuilder().clusterName("elasticsearch").data(true).settings(settings);
        nodeBuilder = nodeBuilder.local(false);
        Node node = nodeBuilder.node();*/

        executeCall(call);

//        node.close();

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

        // Debug specific call
        String debugOption = System.getProperty( "debugNutchCall" );
        if (debugOption != null && injectCall.contains(debugOption)) {
            injectCall.add(1, "-agentlib:jdwp=transport=dt_socket,address=7000,server=y,suspend=y");
        }
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

        String[] classPath = new String[] { "src/test/resources/conf", "build/apache-nutch-1.19/runtime/local", "build/apache-nutch-1.19/runtime/local/lib/*" };

        String cp = StringUtils.join(classPath, File.pathSeparator);

        List<String> baseCall = new ArrayList<>();
        baseCall.add("java");
        baseCall.add("-cp");
        baseCall.add(cp);
        baseCall.add("-Xmx512m");
        baseCall.add("-Dhadoop.log.dir=logs");
        baseCall.add("-Dhadoop.log.file=hadoop.log");
        baseCall.add("-Dfile.encoding=UTF-8");

        return baseCall;

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

}
