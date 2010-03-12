package de.ingrid.iplug.se;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import de.ingrid.iplug.se.searcher.DirectoryScanningSearcherFactory;

public class SearchUpdateScanner extends TimerTask {
    
    public static final String SEARCH_UPDATE = "search.update";
    
    protected static final Logger LOG = Logger.getLogger(SearchUpdateScanner.class);
    
    private final File _dir;
    
    private final DirectoryScanningSearcherFactory _factory;
    
    private final Timer _timer;
    
    public SearchUpdateScanner(final File workingDir, final DirectoryScanningSearcherFactory factory, final long period) {
        _dir = workingDir;
        _factory = factory;
        _timer = new Timer(true);
        _timer.schedule(this, 0, period > 0 ? period : 60000);
    }
    
    @Override
    public void run() {
        if (_dir != null && _dir.exists()) {
            // nutch instances with crawls
            LOG.debug("listing all instances");
            final File[] instances = _dir.listFiles(new InstancesFilter());
                            
            if (instances != null && instances.length > 0) {
                // updated crawls for instances
                LOG.debug("checking crawls for update");
                final Map<File, List<File>> crawls = getCrawls(instances);
                
                if (!crawls.isEmpty()) {
                    // remove update files
                    LOG.debug("removing search.update files");
                    for (final File instance : crawls.keySet()) {
                        update(crawls.get(instance));
                    }
                    // update crawls
                    LOG.info("reloading searcher factory");
                    try {
                        _factory.reload();
                    } catch (final Exception e) {
                        LOG.error("error while reloading searcher factory", e);
                    }
                }
            }
        }
    }
    
    public static void updateCrawl(final FileSystem fs, final Path crawl) throws IOException {
        fs.create(updatePath(crawl), true);
    }
    
    private static boolean isUpdated(final File crawl) {
        return updateFile(crawl).exists();
    }
    
    private static void update(final List<File> crawls) {
        for (final File crawl : crawls) {
            updateFile(crawl).delete();
        }
    }
    
    private static File updateFile(final File crawl) {
        return new File(crawl, SEARCH_UPDATE);
    }
    
    private static Path updatePath(final Path crawl) {
        return new Path(crawl, SEARCH_UPDATE);
    }
    
    private static Map<File, List<File>> getCrawls(final File... instances) {
        final Map<File, List<File>> map = new HashMap<File, List<File>>();
        for (final File instance : instances) {
            // list of crawls
            final List<File> list = new ArrayList<File>();
            final File crawls = new File(instance, "crawls");
            if (crawls.exists()) {
                for (final File crawl : crawls.listFiles()) {
                    if (crawl.isDirectory() && isUpdated(crawl)) {
                        list.add(crawl);
                    }
                }
            }
            // put crawls mapped by instance folder
            if (!list.isEmpty()) {
                map.put(instance, list);
            }
        }
        return map;
    }
    
    private static class InstancesFilter implements FileFilter {
        
        @Override
        public boolean accept(final File file) {
            if (file.isDirectory()) {
                final File crawls = new File(file, "crawls");
                if (crawls.exists()) {
                    return true;
                }
            }
            return false;
        }
    }
}
