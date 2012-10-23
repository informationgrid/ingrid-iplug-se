package de.ingrid.iplug.se.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.admin.searcher.DeduplicatingMultipleSearcher;
import org.apache.nutch.admin.searcher.MultipleSearcher;
import org.apache.nutch.admin.searcher.SearcherFactory;
import org.apache.nutch.admin.searcher.ThreadPool;
import org.apache.nutch.searcher.NutchBean;

import de.ingrid.iplug.se.NutchSearcher;

/**
 * Same as {@linkplain SearcherFactory}, but this class provides the additional
 * functionality to rescan the 'nutch.instance.folder' folder for new/deleted
 * index directories on each {@linkplain #get()} Method. To avoid extensive
 * io-operiation, the rescanning will be performened only when the last scan is
 * longer than a fixed time ago. (This is configured by the constant
 * NEXTRELOADWAITINGTIME which is set to 10 seconds.)
 * 
 */
public class DirectoryScanningSearcherFactory {

    class MultipleSearcherContainer {

        private final MultipleSearcher _searcher;

        public MultipleSearcherContainer(MultipleSearcher searcher) {
            super();
            _searcher = searcher;
        }

        public MultipleSearcher getSearcher() {
            return _searcher;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    };

    private static final Log LOG = LogFactory.getLog(DirectoryScanningSearcherFactory.class);

    private final Configuration _configuration;

    private final NutchSearcher nutchSearcher;

    private Map<String, MultipleSearcherContainer> _map = new HashMap<String, MultipleSearcherContainer>();

    public DirectoryScanningSearcherFactory(Configuration configuration, NutchSearcher nutchSearcher) {
        _configuration = configuration;
        this.nutchSearcher = nutchSearcher;
    }

    public MultipleSearcher get() throws IOException {
        String indexerBasePathsString = _configuration.get("search.instance.folder");
        if (indexerBasePathsString == null || (indexerBasePathsString.trim().length() <= 0)) {
            indexerBasePathsString = _configuration.get("nutch.instance.folder");
        }
        String[] indexerBasePathes = indexerBasePathsString.split(",");
        if (!_map.containsKey(indexerBasePathsString)) {
            Set<Path> allPaths = new HashSet<Path>();
            List<NutchBean> nuchBeans = new ArrayList<NutchBean>();
            for (String indexerBasePath : indexerBasePathes) {
                LOG.info("Create new searcher for instance: " + indexerBasePath);
                Path parent = getSearchPath(indexerBasePath);
                Set<Path> paths = findActivatedCrawlPaths(FileSystem.get(_configuration), parent, new HashSet<Path>());
                for (Path path : paths) {
                    nuchBeans.add(new NutchBean(_configuration, path));
                    allPaths.add(path);
                }
            }

            ThreadPool threadPool = new ThreadPool();

            // create the new searcher
            MultipleSearcher searcher;
            if (_configuration.getBoolean("search.deduplicate.multiple.searchers", true)) {
                LOG.info("Use deduplicating multi searcher.");
                searcher = new DeduplicatingMultipleSearcher(threadPool, nuchBeans.toArray(new NutchBean[0]), nuchBeans
                        .toArray(new NutchBean[0]));
            } else {
                searcher = new MultipleSearcher(threadPool, nuchBeans.toArray(new NutchBean[0]), nuchBeans
                        .toArray(new NutchBean[0]));
            }
            MultipleSearcherContainer multipleSearcherContainer = new MultipleSearcherContainer(searcher);
            _map.put(indexerBasePathsString, multipleSearcherContainer);
            return searcher;
        }

        // return the already created searcher
        return _map.get(indexerBasePathsString).getSearcher();
    }

    private Path getSearchPath(String indexerBasePath) {
        Path parent = new Path(indexerBasePath);
        if (indexerBasePath.endsWith("/general")) {
            parent = parent.getParent();
        }
        return parent;
    }

    public void reload() throws IOException {
        String indexerBasePathsString = _configuration.get("search.instance.folder");
        if (indexerBasePathsString == null || (indexerBasePathsString.trim().length() <= 0)) {
            indexerBasePathsString = _configuration.get("nutch.instance.folder");
        }
        LOG.info("reload searcher for instances: " + indexerBasePathsString);
        if (!_map.containsKey(indexerBasePathsString)) {
            LOG
                    .warn("could not find searcher for instances (maybe the crawl have not run at least one time when it was added)");
            LOG.info("try to get searcher again");
        } else {
            clearCache(indexerBasePathsString);
        }
        get();
        nutchSearcher.updateFacetManager();
    }

    private void clearCache(String instance) throws IOException {
        MultipleSearcher cachedSearcher = _map.remove(instance).getSearcher();
        if (cachedSearcher != null) {
            LOG.info("remove and close searcher: " + cachedSearcher);
            cachedSearcher.close();
            cachedSearcher = null;
        }
    }

    private static Set<Path> findActivatedCrawlPaths(FileSystem fileSystem, Path parent, Set<Path> list)
            throws IOException {
        FileStatus[] status = fileSystem.listStatus(parent);

        for (FileStatus fileStatus : status) {
            Path path = fileStatus.getPath();
            if (fileStatus.isDir() && fileSystem.exists(path)) {
                findActivatedCrawlPaths(fileSystem, path, list);
            } else if (path.getName().equals("search.done")) {
                Path pathToPush = path.getParent();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found activated crawl in: " + pathToPush.getName());
                }
                list.add(pathToPush);
            }
        }
        return list;
    }

}
