package org.apache.nutch.crawl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.indexer.DeleteDuplicates;
import org.apache.nutch.indexer.IndexMerger;
import org.apache.nutch.indexer.Indexer;
import org.apache.nutch.parse.ParseSegment;
import org.apache.nutch.util.HadoopFSUtil;

public class CrawlTool {

  private final Configuration _configuration;
  private final Path _crawlDir;
  private PreCrawls _preCrawls;
  private static final Log LOG = LogFactory.getLog(CrawlTool.class);

  public CrawlTool(Configuration configuration, Path crawlDir) {
    _configuration = configuration;
    _crawlDir = crawlDir;
    _preCrawls = new PreCrawls(configuration);
  }

  public void preCrawl() throws IOException {
    _preCrawls.preCrawl(_crawlDir);
  }

  public void crawl(Integer topn, Integer depth) throws IOException {

    int threads = _configuration.getInt("fetcher.threads.fetch", 10);

    FileSystem fs = FileSystem.get(_configuration);

    LOG.info("crawl started in: " + _crawlDir);
    LOG.info("threads = " + threads);
    LOG.info("depth = " + depth);
    LOG.info("topN = " + topn);

    Path crawlDb = new Path(_crawlDir, "crawldb");
    Path linkDb = new Path(_crawlDir, "linkdb");
    Path segments = new Path(_crawlDir + "segments");
    Path indexes = new Path(_crawlDir + "indexes");
    Path index = new Path(_crawlDir + "index");

    Generator generator = new Generator(_configuration);
    Fetcher fetcher = new Fetcher(_configuration);
    ParseSegment parseSegment = new ParseSegment(_configuration);
    CrawlDb crawlDbTool = new CrawlDb(_configuration);
    LinkDb linkDbTool = new LinkDb(_configuration);
    Indexer indexer = new Indexer(_configuration);
    DeleteDuplicates dedup = new DeleteDuplicates(_configuration);
    IndexMerger merger = new IndexMerger(_configuration);

    int i;
    for (i = 0; i < depth; i++) { // generate new segment
      Path segment = generator.generate(crawlDb, segments, -1, topn, System
          .currentTimeMillis());
      if (segment == null) {
        LOG.info("Stopping at depth=" + i + " - no more URLs to fetch.");
        break;
      }
      fetcher.fetch(segment, threads, org.apache.nutch.fetcher.Fetcher
          .isParsing(_configuration)); // fetch it
      if (!Fetcher.isParsing(_configuration)) {
        parseSegment.parse(segment); // parse it, if needed
      }
      crawlDbTool.update(crawlDb, new Path[] { segment }, true, true); // update
      // crawldb
    }
    if (i > 0) {
      linkDbTool.invert(linkDb, segments, true, true, false); // invert links

      if (indexes != null) {
        // Delete old indexes
        if (fs.exists(indexes)) {
          LOG.info("Deleting old indexes: " + indexes);
          fs.delete(indexes, true);
        }

        // Delete old index
        if (fs.exists(index)) {
          LOG.info("Deleting old merged index: " + index);
          fs.delete(index, true);
        }
      }

      // index, dedup & merge
      FileStatus[] fstats = fs.listStatus(segments, HadoopFSUtil
          .getPassDirectoriesFilter(fs));
      indexer.index(indexes, crawlDb, linkDb, Arrays.asList(HadoopFSUtil
          .getPaths(fstats)));
      if (indexes != null) {
        dedup.dedup(new Path[] { indexes });
        fstats = fs.listStatus(indexes, HadoopFSUtil
            .getPassDirectoriesFilter(fs));
        Path tmpDir = new Path(_configuration.get("mapred.temp.dir", ".")
            + CrawlTool.class.getName() + "_mergeIndex");
        merger.merge(HadoopFSUtil.getPaths(fstats), index, tmpDir);
      }
    } else {
      LOG.warn("No URLs to fetch - check your seed list and URL filters.");
    }
    if (LOG.isInfoEnabled()) {
      LOG.info("crawl finished: " + _crawlDir);
    }

  }

  private static String getDate() {
    return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System
        .currentTimeMillis()));
  }

}
