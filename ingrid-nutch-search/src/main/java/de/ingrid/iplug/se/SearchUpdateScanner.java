package de.ingrid.iplug.se;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Logger;

import de.ingrid.iplug.se.searcher.DirectoryScanningSearcherFactory;

public class SearchUpdateScanner extends TimerTask {

    public static final String SEARCH_UPDATE = "search.update";

    protected static final Logger LOG = Logger.getLogger(SearchUpdateScanner.class);

    private final Configuration _configuration;

    private final DirectoryScanningSearcherFactory _factory;

    private final Timer _timer;

    private long lastUpdateTimestamp = 0;
    
    
    public SearchUpdateScanner(final Configuration configuration, final DirectoryScanningSearcherFactory factory,
            final long period) {
        _configuration = configuration;
        _factory = factory;
        _timer = new Timer(true);
        _timer.schedule(this, 0, period > 0 ? period : 60000);
        LOG.info("Start search update scanner, check interval [sec]: " + (period > 0 ? period : 60000)/1000);
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Check for updated search instances.");
        }
        String indexerBasePathsString = _configuration.get("search.instance.folder");
        if (indexerBasePathsString == null || (indexerBasePathsString.trim().length() <= 0)) {
            indexerBasePathsString = _configuration.get("nutch.instance.folder");
        }
        String[] indexerBasePathes = indexerBasePathsString.split(",");

        List<File> instances = new ArrayList<File>();

        for (String dirStr : indexerBasePathes) {
            File dir = new File(dirStr);
            if (dir != null && dir.exists()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Try to add instance:" + dir);
                }
                // nutch instances with crawls
                instances.addAll(Arrays.asList(dir.listFiles(new InstancesFilter())));
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found instances:" + instances.toString());
        }

        if (instances.size() > 0) {
            // updated crawls for instances
            final Map<File, List<File>> crawls = getUpdatedCrawls(instances.toArray(new File[0]));

            if (!crawls.isEmpty()) {
                // update crawls
                try {
                    lastUpdateTimestamp = (new Date()).getTime();
                    _factory.reload();
                } catch (final Exception e) {
                    LOG.error("error while reloading searcher factory", e);
                }
            }
        }
    }

    public static void updateCrawl(final FileSystem fs, final Path crawl) throws IOException {
        LOG.info("updating crawl: " + crawl);
        Path searchUpdatePath = updatePath(crawl);
        if (fs.exists(searchUpdatePath)) {
            fs.delete(searchUpdatePath, true);
        }
        fs.create(searchUpdatePath, true);
    }

    private boolean isUpdated(final File crawl) {
        File searchUpdateFile = updateFile(crawl);
        return searchUpdateFile.exists() && searchUpdateFile.lastModified() > lastUpdateTimestamp;
    }

    private static File updateFile(final File crawl) {
        return new File(crawl, SEARCH_UPDATE);
    }

    private static Path updatePath(final Path crawl) {
        return new Path(crawl, SEARCH_UPDATE);
    }

    private Map<File, List<File>> getUpdatedCrawls(final File... instances) {
        final Map<File, List<File>> map = new HashMap<File, List<File>>();
        for (final File instance : instances) {
            // list of crawls
            final List<File> list = new ArrayList<File>();
            final File crawls = new File(instance, "crawls");
            if (crawls.exists()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("checking crawl directory: " + crawls);
                }
                for (final File crawl : crawls.listFiles()) {
                    if (crawl.isDirectory() && isUpdated(crawl)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("add updated crawl directory: " + crawl);
                        }
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
