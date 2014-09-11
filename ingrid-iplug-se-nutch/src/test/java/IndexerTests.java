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
import org.junit.Test;

/**
 * 
 */

/**
 * @author joachim
 * 
 */
public class IndexerTests {

    @Test
    public void test() throws IOException, InterruptedException {
        
        
        delete(new File("test"));
        
        String[] classPath = new String[] {
                "src/test/resources/conf",
                "build/apache-nutch-1.9/runtime/local"
        };

        String [] libs = getJarFiles("build/apache-nutch-1.9/runtime/local/lib");
        classPath = concat(classPath, libs);
        
        String cp = StringUtils.join(classPath, File.pathSeparator);
        
        List<String> baseCall = new ArrayList<String>();
        baseCall.add("java");
        baseCall.add("-cp");
        baseCall.add(cp);
        baseCall.add("-Xmx512m");
        baseCall.add("-Dhadoop.log.dir=logs");
        baseCall.add("-Dhadoop.log.file=hadoop.log");
        
        // inject
        List<String> injectCall = new ArrayList<String>();
        injectCall.addAll(baseCall);
        injectCall.add("org.apache.nutch.crawl.Injector");
        injectCall.add("test/crawldb");
        injectCall.add("src/test/resources/urls");
        
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
        
        Scanner s = new Scanner( process.getInputStream() );
        while (s.hasNextLine()) {
            System.out.println( s.nextLine() );
        }
        s.close();
        System.out.println("Exited with code " + process.waitFor());
        
        // generate
        List<String> generateCall = new ArrayList<String>();
        generateCall.addAll(baseCall);
        generateCall.add("org.apache.nutch.crawl.Generator");
        generateCall.add("test/crawldb");
        generateCall.add("test/segments");
        
        System.out.println(StringUtils.join(generateCall, " "));
        pb = new ProcessBuilder(generateCall);
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        process = pb.start();
        
        s = new Scanner( process.getInputStream() ).useDelimiter( "\\Z" );
        while (s.hasNextLine()) {
            System.out.println( s.nextLine() );
        }
        s.close();
        System.out.println("Exited with code " + process.waitFor()); 
        
        // fetch
        File file = new File("test/segments");
        String[] directories = file.list(new FilenameFilter() {
          @Override
          public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
          }
        });
        
        List<String> fetchCall = new ArrayList<String>();
        fetchCall.addAll(baseCall);
        fetchCall.add("org.apache.nutch.fetcher.Fetcher");
        fetchCall.add("test/segments/" + directories[0]);
        
        System.out.println(StringUtils.join(fetchCall, " "));
        pb = new ProcessBuilder(fetchCall);
        pb.directory(new File("."));
        pb.redirectErrorStream(true);
        process = pb.start();
        
        s = new Scanner( process.getInputStream() ).useDelimiter( "\\Z" );
        while (s.hasNextLine()) {
            System.out.println( s.nextLine() );
        }
        s.close();
        System.out.println("Exited with code " + process.waitFor()); 
        
        
        
        
        
        
        
        
       
    
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
        
        for (int i=0; i< files.length; i++) {
            files[i] = (new File (dir, files[i])).getAbsolutePath();
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
