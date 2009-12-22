package de.ingrid.iplug.se.searcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.admin.searcher.MultipleSearcher;
import org.apache.nutch.admin.searcher.SearcherFactory;
import org.apache.nutch.admin.searcher.ThreadPool;
import org.apache.nutch.searcher.NutchBean;

import de.ingrid.iplug.util.TimeProvider;

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
    private final long _nextDirScanTime;
    private final Set<Path> _lastScanDirResult;

    public MultipleSearcherContainer(MultipleSearcher searcher, long nextDirScanTime, Set<Path> lastScanDirResult) {
      super();
      _searcher = searcher;
      _nextDirScanTime = nextDirScanTime;
      _lastScanDirResult = lastScanDirResult;
    }

    public MultipleSearcher getSearcher() {
      return _searcher;
    }

    public long getNextDirScanTime() {
      return _nextDirScanTime;
    }

    public Set<Path> getLastScanDirResult() {
      return _lastScanDirResult;
    }

    @Override
    public String toString() {
      return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  };

  public static final long NEXTRELOADWAITINGTIME = TimeUnit.SECONDS.toMillis(10);
  private static final Log LOG = LogFactory.getLog(DirectoryScanningSearcherFactory.class);

  private final Configuration _configuration;
  private final TimeProvider _timeProvider;

  private Map<String, MultipleSearcherContainer> _map = new HashMap<String, MultipleSearcherContainer>();

  public DirectoryScanningSearcherFactory(Configuration configuration, TimeProvider timeProvider) {
    _configuration = configuration;
    _timeProvider = timeProvider;
  }

  public MultipleSearcher get() throws IOException {
    String indexerBasePath = _configuration.get("nutch.instance.folder");
    if (!_map.containsKey(indexerBasePath)) {
      LOG.info("Create new searcher for instance: " + indexerBasePath);
      Path parent = getSearchPath(indexerBasePath);
      Set<Path> paths = findActivatedCrawlPaths(FileSystem.get(_configuration), parent, new HashSet<Path>());
      ThreadPool threadPool = new ThreadPool();
      List<NutchBean> nuchBeans = new ArrayList<NutchBean>(paths.size());
      for (Path path : paths) {
        nuchBeans.add(new NutchBean(_configuration, path));
      }

      // create the new searcher
      MultipleSearcher searcher = new MultipleSearcher(threadPool, nuchBeans.toArray(new NutchBean[0]), nuchBeans
          .toArray(new NutchBean[0]));
      MultipleSearcherContainer multipleSearcherContainer = new MultipleSearcherContainer(searcher, _timeProvider
          .getTime()
          + NEXTRELOADWAITINGTIME, paths);
      _map.put(indexerBasePath, multipleSearcherContainer);
      return searcher;
    } else {
      // test for next reload time
      MultipleSearcherContainer multipleSearcherContainer = _map.get(indexerBasePath);
      if (_timeProvider.getTime() >= multipleSearcherContainer.getNextDirScanTime()) {
        // scan directory for changes
        Path parent = getSearchPath(indexerBasePath);
        Set<Path> paths = findActivatedCrawlPaths(FileSystem.get(_configuration), parent, new HashSet<Path>());
        if (!paths.equals(multipleSearcherContainer.getLastScanDirResult())) {
          // Changes found, so reload the searcher now
          reload();
        }
        return _map.get(indexerBasePath).getSearcher();
      } else {
        return multipleSearcherContainer.getSearcher();
      }
    }
  }

  private Path getSearchPath(String indexerBasePath) {
    Path parent = new Path(indexerBasePath);
    if (indexerBasePath.endsWith("/general")) {
      parent = parent.getParent();
    }
    return parent;
  }

  public void reload() throws IOException {
    String instance = _configuration.get("nutch.instance.folder");
    LOG.info("reload searcher for instance: " + instance);
    clearCache(instance);
    get();
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
      if (fileStatus.isDir()) {
        findActivatedCrawlPaths(fileSystem, path, list);
      } else if (path.getName().equals("search.done")) {
        Path pathToPush = path.getParent();
        list.add(pathToPush);
      }
    }
    return list;
  }

}
