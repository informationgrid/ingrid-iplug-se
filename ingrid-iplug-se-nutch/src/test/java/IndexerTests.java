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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
    public void test1InjectStartURLs() throws IOException, InterruptedException {

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
    public void test2InjectBWURLs() throws IOException, InterruptedException {

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
    public void test3InjectMetadata() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("de.ingrid.iplug.se.nutch.crawl.metadata.MetadataInjector");
        call.add("test/metadatadb");
        call.add("src/test/resources/urls/metadata");

        executeCall(call);
    }

    @Test
    public void test4FilterCrawlDB() throws IOException, InterruptedException {

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
    public void test5Generate() throws IOException, InterruptedException {

        // inject start urls
        List<String> call = new ArrayList<String>();
        call.addAll(setUpBaseCall());
        call.add("org.apache.nutch.crawl.Generator");
        call.add("test/crawldb");
        call.add("test/segments");

        executeCall(call);
    }

    @Test
    public void test6Fetch() throws IOException, InterruptedException {

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
        call.add("test/segments/" + directories[0]);

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

        String[] classPath = new String[] { "src/test/resources/conf", "build/apache-nutch-1.9/runtime/local" };

        String[] libs = getJarFiles("build/apache-nutch-1.9/runtime/local/lib");
        classPath = concat(classPath, libs);

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
