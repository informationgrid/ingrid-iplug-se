import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.apache.nutch.indexer.IndexingJob;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector;
import de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexerCmdLineTests {

    @Test
    public void test01InjectStartURLs() throws IOException, InterruptedException {

        delete(new File("test"));

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.crawl.Injector");
        call.add("test/crawldb");
        call.add("src/test/resources/urls/start");

        executeCall(call);
    }

    @Test
    public void test02InjectBWURLs() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<String>();
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
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector");
        call.add("test/metadatadb");
        call.add("src/test/resources/urls/metadata");

        executeCall(call);
    }

    @Test
    public void test04FilterCrawlDB() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<String>();
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
        List<String> call = new ArrayList<String>();
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
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.fetcher.Fetcher");
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
        List<String> call = new ArrayList<String>();
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
    public void test09HostStatistics() throws IOException, InterruptedException {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.tools.HostStatistic");
        call.add("test/crawldb");
        call.add("test/segments/" + directories[directories.length - 1]);

        executeCall(call);
    }

    @Test
    public void test09_1ParseDataUpdater() throws IOException, InterruptedException {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        ParseDataUpdater.main(new String[] { "test/metadatadb", "test/segments/" + directories[directories.length - 1] });

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.metadata.ParseDataUpdater");
        call.add("test/metadatadb");
        call.add("test/segments/" + directories[directories.length - 1]);

        executeCall(call);
    }

    @Test
    public void test10WebgraphFilter() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<String>();
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
    public void test11WebgraphCreate() throws Exception {

        // get all segments
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.scoring.webgraph.WebGraph");
        call.add("-webgraphdb");
        call.add("test/webgraph");
        call.add("-segment");
        call.add("test/segments/" + directories[directories.length - 1]);

        executeCall(call);
    }

    @Test
    public void test12LinkRank() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.scoring.webgraph.LinkRank");
        call.add("-webgraphdb");
        call.add("test/webgraph");

        executeCall(call);
    }

    @Test
    public void test13ScoreUpdater() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.scoring.webgraph.ScoreUpdater");
        call.add("-webgraphdb");
        call.add("test/webgraph");
        call.add("-crawldb");
        call.add("test/crawldb");

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
            List<String> call = new ArrayList<String>();
            call.addAll(setUpBaseCall());
            call.add("org.apache.nutch.segment.SegmentMerger");
            call.add("test/merged_segment");
            call.add("-dir");
            call.add("test/segments");

            executeCall(call);
        }
    }

    @Test
    public void test15SegmentFilter() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.segment.SegmentFilter");
        call.add("test/filtered_segment");
        call.add("test/crawldb");
        call.add("-dir");
        call.add("test/merged_segment");

        executeCall(call);
    }

    @Test
    public void test16InvertLinks() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.crawl.LinkDb");
        call.add("test/linkdb");
        call.add("-dir");
        call.add("test/filtered_segment");
        call.add("-noNormalize");
        call.add("-noFilter");

        executeCall(call);
    }

    @Test
    public void test17FilterLinkdb() throws Exception {

        // inject start urls
        List<String> call = new ArrayList<String>();
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

        IndexingJob.main(new String[] { "test/crawldb", "-linkdb", "test/linkdb", "-dir", "test/filtered_segment", "-deleteGone" });

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.indexer.IndexingJob");
        call.add("test/crawldb");
        call.add("-linkdb");
        call.add("test/linkdb");
        call.add("-dir");
        call.add("test/filtered_segment");
        call.add("-deleteGone");

        executeCall(call);
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

    private String[] getJarFiles(String dir) {
        File file = new File(dir);
        String[] files = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                File f = new File(current, name);
                return !f.isDirectory() && name.endsWith(".jar");
            }
        });

        for (int i = 0; i < files.length; i++) {
            files[i] = (new File(dir, files[i])).getAbsolutePath();
        }

        return files;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
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
