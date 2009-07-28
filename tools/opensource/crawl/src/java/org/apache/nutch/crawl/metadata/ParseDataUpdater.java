package org.apache.nutch.crawl.metadata;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.NutchConfiguration;

public class ParseDataUpdater extends Configured {

  public static final Log LOG = LogFactory.getLog(ParseDataUpdater.class);

  public ParseDataUpdater(Configuration configuration) {
    super(configuration);
  }

  public void update(Path metadataDb, Path segment) throws IOException {
    LOG.info("metadata update: starting");
    LOG.info("metadata update: db: " + metadataDb);
    LOG.info("metadata update: segment: " + segment);

    // tmp dir for all jobs
    Path tempDir = new Path(getConf().get("mapred.temp.dir",
        System.getProperty("java.io.tmpdir")));
    String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
    LOG.info("write tmp files into: " + tempDir);
    LOG.info("metadata update: wrap parsedata: " + segment);

    String name = "metadata-wrap-parsedata-temp-" + id;
    Path wrappedParseData = new Path(tempDir, name);
    ParseDataWrapper parseDataWrapper = new ParseDataWrapper(getConf());
    parseDataWrapper.wrap(segment, wrappedParseData);

    LOG.info("metadata update: merge metadatadb and wrapped parse_data: "
        + segment);
    name = "metadata-merge-parsedata-temp-" + id;
    Path mergeMetadataParseData = new Path(tempDir, name);
    MetadataMerger metadataMerger = new MetadataMerger(getConf());
    metadataMerger.merge(metadataDb, wrappedParseData, mergeMetadataParseData);
    FileSystem.get(getConf()).delete(wrappedParseData, true);

    // convert formats
    name = "metadata-merge-unwrap-temp-" + id;
    Path unwrapParseData = new Path(tempDir, name);
    ParseDataUnwrapper unwrapper = new ParseDataUnwrapper(getConf());
    unwrapper.unwrap(mergeMetadataParseData, unwrapParseData);
    FileSystem.get(getConf()).delete(mergeMetadataParseData, true);

    // install new parse_data
    FileSystem fs = FileSystem.get(getConf());
    Path old = new Path(segment, "old_parse_data");
    Path current = new Path(segment, ParseData.DIR_NAME);
    if (fs.exists(current)) {
      if (fs.exists(old)) {
        fs.delete(old, true);
      }
      fs.rename(current, old);
    }
    fs.rename(unwrapParseData, current);
    if (fs.exists(old)) {
      fs.delete(old, true);
    }
  }

  public static void main(String[] args) throws IOException {
    Path metadataDb = new Path(args[0]);
    Path segment = new Path(args[1]);
    Configuration conf = NutchConfiguration.create();
    ParseDataUpdater parseDataUpdater = new ParseDataUpdater(conf);
    parseDataUpdater.update(metadataDb, segment);

  }
}
