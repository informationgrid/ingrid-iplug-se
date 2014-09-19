/**
 * 
 */
package de.ingrid.iplug.se.nutchController;

import java.nio.file.Paths;

import de.ingrid.iplug.se.webapp.container.Instance;

/**
 * @author joachim
 *
 */
public class NutchProcessFactory {
    
    public static IngridCrawlNutchProcess getIngridCrawlNutchProcess(Instance instance, int depth, int noUrls) {
        IngridCrawlNutchProcess process = new IngridCrawlNutchProcess();
        process.setDepth(depth);
        process.setNoUrls(noUrls);
        
        process.setWorkingDirectory(instance.getWorkingDirectory());
        process.addClassPath(Paths.get(instance.getWorkingDirectory(), "conf").toAbsolutePath().toString());
        process.addJavaOptions(new String[] { "-Xmx512m", "-Dhadoop.log.dir=" + Paths.get(instance.getWorkingDirectory(), "logs").toAbsolutePath(), "-Dhadoop.log.file=hadoop.log" });
        process.addClassPath(Paths.get("apache-nutch-runtime/runtime/local").toAbsolutePath().toString());
        process.addClassPathLibraryDirectory("apache-nutch-runtime/runtime/local/lib");
        
        return process;
        
    }

}
