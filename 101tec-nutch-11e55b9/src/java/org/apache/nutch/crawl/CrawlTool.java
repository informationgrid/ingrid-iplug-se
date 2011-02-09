package org.apache.nutch.crawl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.admin.ConfigurationUtil;
import org.apache.nutch.admin.searcher.SearcherFactory;
import org.apache.nutch.crawl.bw.BWInjector;
import org.apache.nutch.crawl.bw.BWUpdateDb;
import org.apache.nutch.crawl.metadata.MetadataInjector;
import org.apache.nutch.crawl.metadata.ParseDataUpdater;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.indexer.DeleteDuplicates;
import org.apache.nutch.indexer.IndexMerger;
import org.apache.nutch.indexer.Indexer;
import org.apache.nutch.mail.MailService;
import org.apache.nutch.parse.ParseSegment;
import org.apache.nutch.plugin.PluginRepository;
import org.apache.nutch.segment.SegmentMerger;
import org.apache.nutch.tools.HostStatistic;
import org.apache.nutch.tools.UrlReporter;
import org.apache.nutch.util.HadoopFSUtil;

import de.ingrid.iplug.se.SearchUpdateScanner;
import de.ingrid.iplug.se.crawl.WebGraphScoring;

public class CrawlTool {

  private final Configuration _configuration;
  private final Path _crawlDir;
  private PreCrawls _preCrawls;
  private FileSystem _fileSystem;
  private static final Log LOG = LogFactory.getLog(CrawlTool.class);

  public CrawlTool(Configuration configuration, Path crawlDir)
          throws IOException {
    _configuration = configuration;
    _crawlDir = crawlDir;
    _preCrawls = new PreCrawls(configuration);
    _fileSystem = FileSystem.get(_configuration);
  }

  public void preCrawl() throws IOException {
    _preCrawls.preCrawl(_crawlDir);
  }

  public void crawl(Integer topn, Integer depth) throws IOException {

    int threads = _configuration.getInt("fetcher.threads.fetch", 10);

    LOG.info("crawl started in: " + _crawlDir);
    LOG.info("threads = " + threads);
    LOG.info("depth = " + depth);
    LOG.info("topN = " + topn);

    // url dir's
    Path urlDir = new Path(_crawlDir, "urls/start");
    Path limitDir = new Path(_crawlDir, "urls/limit");
    Path excludeDir = new Path(_crawlDir, "urls/exclude");
    Path metadataDir = new Path(_crawlDir, "urls/metadata");

    // input path's
    Path crawlDb = new Path(_crawlDir, "crawldb");
    Path bwDb = new Path(_crawlDir, "bwdb");
    Path metadataDb = new Path(_crawlDir, "metadatadb");
    Path linkDb = new Path(_crawlDir, "linkdb");
    Path segments = new Path(_crawlDir, "segments");
    Path indexes = new Path(_crawlDir, "indexes");
    Path index = new Path(_crawlDir, "index");
    Path indexTemp = new Path(_crawlDir, "indexTemp");

    // injectors
    Injector injector = new Injector(_configuration);
    BWInjector bwInjector = new BWInjector(_configuration);
    MetadataInjector metadataInjector = new MetadataInjector(_configuration);

    // other jobs
    Generator generator = new Generator(_configuration);
    Fetcher fetcher = new Fetcher(_configuration);
    // ------ none nutch-specific code starts here
    UrlReporter reporter = new UrlReporter(_configuration);
    MailService mail = MailService.get(_configuration);
    // set configuration since it might have changed
    mail.setConf(_configuration);
    // ------ none nutch-specific code ends here
    ParseSegment parseSegment = new ParseSegment(_configuration);
    BWUpdateDb bwUpdateDb = new BWUpdateDb(_configuration);
    ParseDataUpdater parseDataUpdater = new ParseDataUpdater(_configuration);
    WebGraphScoring webGraphScoring = new WebGraphScoring(_configuration, _crawlDir);
    LinkDb linkDbTool = new LinkDb(_configuration);
    Indexer indexer = new Indexer(_configuration);
    DeleteDuplicates dedup = new DeleteDuplicates(_configuration);
    IndexMerger merger = new IndexMerger(_configuration);

    // statistics
    HostStatistic hostStatistic = new HostStatistic(_configuration);

    injector.inject(crawlDb, urlDir);

    boolean bwEnable = _configuration.getBoolean("bw.enable", false);
    if (LOG.isInfoEnabled()) {
        LOG.info("Use BW filtering: " + bwEnable);
    }
    if (bwEnable) {
      // BwInjector doesnt support update
      if (_fileSystem.exists(bwDb)) {
        LOG.info("bwdb exists, delete it: " + bwDb);
        _fileSystem.delete(bwDb, true);
      }
      if (_fileSystem.exists(limitDir)) {
          if (LOG.isInfoEnabled()) {
              LOG.info("Inject White urls...");
          }
        bwInjector.inject(bwDb, limitDir, false);
      }
      if (_fileSystem.exists(excludeDir)) {
          if (LOG.isInfoEnabled()) {
              LOG.info("Inject Black urls...");
          }
        bwInjector.inject(bwDb, excludeDir, true);
      }
    }

    boolean metadataEnable = _configuration
            .getBoolean("metadata.enable", false);
    if (metadataEnable) {
      // MetadataInjector deoesnt support update
      if (_fileSystem.exists(metadataDb)) {
        LOG.info("metadatadb exists, delete it: " + metadataDb);
        _fileSystem.delete(metadataDb, true);
      }
      if (_fileSystem.exists(metadataDir)) {
        metadataInjector.inject(metadataDb, metadataDir);
      }
    }

    int i;
    ArrayList<Path> segs = new ArrayList<Path>();
    for (i = 0; i < depth; i++) { // generate new segment
        if (LOG.isInfoEnabled()) {
            LOG.info("Generate new segment...");
        }
      Path segment = generator.generate(crawlDb, segments, -1, topn, System
              .currentTimeMillis());
      if (segment == null) {
        LOG.info("Stopping at depth=" + i + " - no more URLs to fetch.");
        break;
      }
      if (LOG.isInfoEnabled()) {
          LOG.info("... new segment generated:" + segment);
      }
      segs.add(segment);
      if (LOG.isInfoEnabled()) {
          LOG.info("Fetch segment:" + segment);
      }
      fetcher.fetch(segment, threads, org.apache.nutch.fetcher.Fetcher
              .isParsing(_configuration)); // fetch it
      // ------ none nutch-specific code starts here
      if (LOG.isInfoEnabled()) {
          LOG.info("Analyse segment:" + segment);
      }
      reporter.analyze(segment);
      if (LOG.isInfoEnabled()) {
          LOG.info("Send report mail for segment:" + segment);
      }
      mail.sendSegmentReport(_fileSystem, segment, i);
      // ------ none nutch-specific code ends here
      if (!Fetcher.isParsing(_configuration)) {
          if (LOG.isInfoEnabled()) {
              LOG.info("Parse segment:" + segment);
          }
        parseSegment.parse(segment); // parse it, if needed
      }
      if (bwEnable) {
          if (LOG.isInfoEnabled()) {
              LOG.info("Start updating crawldb with bwdb from segment:" + segment);
          }
        bwUpdateDb.update(crawlDb, bwDb, new Path[] { segment }, true, true); // update
      } else {
          if (LOG.isInfoEnabled()) {
              LOG.info("Do not update crawldb with bwdb from segment:" + segment);
          }
      }
      if (LOG.isInfoEnabled()) {
          LOG.info("Generate host statistics for segment:" + segment);
      }
      hostStatistic.statistic(crawlDb, segment);
      if (metadataEnable) {
        if (_fileSystem.exists(metadataDb)) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Update metadata db from segment:" + segment);
            }
          parseDataUpdater.update(metadataDb, segment);
        }
      }
    }
    
    if (segs.size() > 0) {
        LOG.info("Using WebGraph to calculate score");
        webGraphScoring.updateScore(crawlDb, segs);
    } else {
        LOG.info("No new segments, skip calculating WebGraph.");
    }

    // list of all segments that will be used
    FileStatus[] listStatus = _fileSystem.listStatus(segments, HadoopFSUtil.getPassDirectoriesFilter(_fileSystem));
    Path[] mergeSegments = HadoopFSUtil.getPaths(listStatus);
    // list of all segments that will be deleted after indexing
    Path[] segmentsToDelete = null;

    // ------ none nutch-specific code starts here
    boolean mergeEnable = _configuration.getBoolean("merge.after.fetch", false);
    LOG.info("Merge of segments enabled ? merge.after.fetch = " + mergeEnable);
    if (mergeEnable) {
    // ------ none nutch-specific code ends here

        if (mergeSegments.length > 1) {
            try {
              // merge segments
              SegmentMerger segmentMerger = new SegmentMerger(_configuration);
              Path mergeDir = new Path(segments, "merge-segments");
              if (_fileSystem.exists(mergeDir)) {
                  _fileSystem.delete(mergeDir, true);
              }
              segmentMerger.merge(mergeDir, mergeSegments, false, false, 0);
              // get merged segment
              Path mergeSegTemp = _fileSystem.listStatus(mergeDir)[0].getPath();
              // move merged segment to others
              Path mergeSegment = new Path(segments, mergeSegTemp.getName());
              _fileSystem.rename(mergeSegTemp, mergeSegment);
              _fileSystem.delete(mergeDir, true);
              // create statistic
              hostStatistic.statistic(crawlDb, mergeSegment);
              // use only merged segment
              segmentsToDelete = mergeSegments;
              mergeSegments = new Path[] { mergeSegment };
            } catch (Exception e) {
              LOG.warn("error while merging" ,e);
            }
        }

    // ------ none nutch-specific code starts here
    }
    // ------ none nutch-specific code ends here

    if (mergeSegments.length > 0) {
      if (LOG.isInfoEnabled()) {
          LOG.info("Inverting links.");
      }
      linkDbTool.invert(linkDb, mergeSegments, true, true, false); // invert links

      if (indexes != null) {
        // Delete old indexes
        if (_fileSystem.exists(indexes)) {
          LOG.info("Deleting old indexes: " + indexes);
          _fileSystem.delete(indexes, true);
        }

        // Delete old temporary index
        if (_fileSystem.exists(indexTemp)) {
          LOG.info("Deleting old merged temp index: " + indexTemp);
          _fileSystem.delete(indexTemp, true);
        }
      }

      // index, dedup & merge
      if (LOG.isInfoEnabled()) {
          LOG.info("Create index...");
      }
      indexer.index(indexes, crawlDb, linkDb, Arrays.asList(mergeSegments));
      if (indexes != null) {
        if (LOG.isInfoEnabled()) {
          LOG.info("Dedup index...");
        }
        dedup.dedup(new Path[] { indexes });
        FileStatus[] fstats = _fileSystem.listStatus(indexes, HadoopFSUtil
                .getPassDirectoriesFilter(_fileSystem));
        Path tmpDir = new Path(_configuration.get("mapred.temp.dir", ".")
                + CrawlTool.class.getName() + "_mergeIndex");
        if (LOG.isInfoEnabled()) {
            LOG.info("Merge index...");
        }
        merger.merge(HadoopFSUtil.getPaths(fstats), indexTemp, tmpDir);
        
        // Delete old index and use the new index instead
        if (_fileSystem.exists(index)) {
          LOG.info("Deleting old merged index: " + index);
          _fileSystem.delete(index, true);
        }
        LOG.info("Rename temp folder '" + indexTemp + "' to new index '" + index + "'.");
        _fileSystem.rename(indexTemp, index);
      }
    } else {
      LOG.warn("No URLs to fetch - check your seed list and URL filters.");
    }
    
    // delete old segments (after indexing so searching is meanwhile still possible)
    if (segmentsToDelete != null) {
      for (Path p : segmentsToDelete) {
        if (LOG.isInfoEnabled()) {
          LOG.info("Delete old segment: " + p);
        }
        _fileSystem.delete(p, true);
      }
    }
    
    SearchUpdateScanner.updateCrawl(_fileSystem, _crawlDir);
    SearcherFactory.getInstance(_configuration).reload();
    
    if (LOG.isInfoEnabled()) {
      LOG.info("crawl finished: " + _crawlDir);
    }
  }

  public FileSystem getFileSystem() {
    return _fileSystem;
  }

  public Configuration getConfiguration() {
    return _configuration;
  }

  public Path getCrawlDir() {
    return _crawlDir;
  }
  
  public static void main(String[] args) throws Exception {
      
      
      LOG.info("Init crawl ...");
      File workDir = new File(args[3]);
      Configuration configuration = (new ConfigurationUtil(workDir)).loadConfiguration(args[4]);
      
      Integer topN  = Integer.valueOf(args[0]);
      Integer depth = Integer.valueOf(args[1]);
      Path crawlDir = new Path(args[2]);
      
      // prepare plugins
      PluginRepository pluginRepository = new PluginRepository(configuration);
      
      CrawlTool crawlTool = new CrawlTool(configuration, crawlDir);
      
      //=======================

      FileSystem fileSystem = crawlTool.getFileSystem();
      Path lockPath = new Path(crawlDir, "crawl.running");
      Path urlPath = new Path(crawlDir, "urls/start/urls.txt");
      boolean alreadyRunning = false;
      try {
        alreadyRunning = fileSystem.exists(lockPath);
        if (!alreadyRunning) {
          fileSystem.createNewFile(lockPath);
          crawlTool.preCrawl();
          // if there's any url to fetch then start crawling
          if (fileSystem.exists(urlPath))
              crawlTool.crawl(topN, depth);
          else
              LOG.warn("It seems that there are no URLs to crawl!");
        } else {
            LOG.warn("crawl is already running");
        }
      } catch (IOException e) {
        LOG.warn("can not start crawl.", e);
      } finally {
        if (!alreadyRunning) {
          try {
            fileSystem.delete(lockPath, false);
          } catch (Throwable e) {
            LOG.warn("Can not delete lock file.", e);
          }
        }
      }
      
      //=======================
      LOG.info("Crawl finished!");
  }

}
