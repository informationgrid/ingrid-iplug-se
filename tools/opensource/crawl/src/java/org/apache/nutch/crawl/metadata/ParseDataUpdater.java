package org.apache.nutch.crawl.metadata;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.util.NutchConfiguration;

public class ParseDataUpdater extends Configured {

  public static final Log LOG = LogFactory.getLog(ParseDataUpdater.class);

  public void update(Path metadataDb, Path segment) throws IOException {
    LOG.info("metadata update: starting");
    LOG.info("metadata update: db: " + metadataDb);
    LOG.info("metadata update: segment: " + segment);

    // tmp dir for all jobs
    Path tempDir = new Path(getConf().get("mapred.temp.dir", System.getProperty("java.io.tmpdir")));
    String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
    LOG.info("write tmp files into: " + tempDir);
    LOG.info("metadata update: wrap parsedata: " + segment);
    String name = "metadata-wrap-parsedata-temp-" + id;
    Path wrappedParseData = new Path(tempDir, name);
    ParseDataWrapper parseDataWrapper = new ParseDataWrapper();
    parseDataWrapper.setConf(getConf());
    parseDataWrapper.wrap(segment, wrappedParseData);

    LOG.info("metadata update: merge metadatadb and wrapped parse_data: " + segment);
    name = "metadata-merge-parsedata-temp-" + id;
    Path mergeMetadataParseData = new Path(tempDir, name);
    MetadataMerger metadataMerger = new MetadataMerger();
    metadataMerger.setConf(getConf());
    metadataMerger.merge(metadataDb, wrappedParseData, mergeMetadataParseData);

  }

  public static void main(String[] args) throws IOException {
    Path metadataDb = new Path(args[0]);
    Path segment = new Path(args[1]);
    ParseDataUpdater parseDataUpdater = new ParseDataUpdater();
    Configuration conf = NutchConfiguration.create();
    parseDataUpdater.setConf(conf);
    parseDataUpdater.update(metadataDb, segment);

  }
}
