/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2023 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ingrid.iplug.se.nutch.segment;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.metadata.MetaWrapper;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.segment.SegmentMerger;
import org.apache.nutch.util.HadoopFSUtil;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * This tool takes several segments and filters them against the crawlDB. It can
 * be used to reduce the segments data if the crawlDB has been filtered.
 * 
 * @author Joachim Müller
 */
public class SegmentFilter extends Configured implements Tool {
    private static final Logger LOG = LoggerFactory.getLogger(SegmentFilter.class);

    private static final String SEGMENT_PART_KEY = "part";
    private static final String SEGMENT_SLICE_KEY = "slice";

    private URLFilters filters = null;
    private URLNormalizers normalizers = null;
    private long sliceSize = -1;
    private long curCount = 0;

    public SegmentFilter() {
        super(null);
    }

    public SegmentFilter(Configuration conf) {
        super(conf);
    }

    String segmentName = null;

    public void setConf(Configuration conf) {
        super.setConf(conf);
        if (conf == null)
            return;
        if (conf.getBoolean("segment.filter.filter", false)) {
            filters = new URLFilters(conf);
        }
        if (conf.getBoolean("segment.filter.normalizer", false))
            normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_DEFAULT);

        sliceSize = conf.getLong("segment.filter.slice", -1);
        if ((sliceSize > 0) && (LOG.isInfoEnabled())) {
            LOG.info("Slice size: " + sliceSize + " URLs.");
        }
        
        segmentName = conf.get("segment.filter.segmentName");

    }

    public void filter(Path out, Path crawlDbPath, Path[] segs, boolean filter, boolean normalize) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Use filter crawl db " + crawlDbPath);
        }
        ArrayList<String> directories = new ArrayList<String>();
        directories.add(Content.DIR_NAME);
        directories.add(CrawlDatum.GENERATE_DIR_NAME);
        directories.add(CrawlDatum.FETCH_DIR_NAME);
        directories.add(CrawlDatum.PARSE_DIR_NAME);
        directories.add(ParseData.DIR_NAME);
        directories.add(ParseText.DIR_NAME);

        Path currentCrawlDbPath = new Path(crawlDbPath, CrawlDb.CURRENT_NAME);
        FileSystem fs = FileSystem.get(getConf());

        if (!fs.exists(currentCrawlDbPath)) {
            LOG.error("CrawlDb does not exist: " + currentCrawlDbPath);
            return;
        }

        for (Path srcSegmentPath : segs) {
            String srcSegment = srcSegmentPath.getName();
            String dstSegment = Generator.generateSegmentName();
            for (String dir : directories) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Filter segment " + srcSegment + " to " + out + "/" + dstSegment + "for part " + dir);
                }

                Job job = NutchJob.getInstance(getConf());
                Configuration conf = job.getConfiguration();

                job.setJobName("filtersegs " + out + "_" + dir);
                conf.setBoolean("segment.filter.filter", filter);
                conf.setBoolean("segment.filter.normalizer", normalize);

                FileInputFormat.addInputPath(job, currentCrawlDbPath);

                Path gDir = new Path(srcSegmentPath, dir);
                if (fs.exists(gDir)) {
                    FileInputFormat.addInputPath(job, gDir);
                    conf.setLong("segment.filter.slice", 0);
                    conf.set("segment.filter.segmentName", dstSegment);
                    // parameter is required
                    conf.set("segment.merger.segmentName", "");

                    job.setInputFormatClass(SegmentMerger.ObjectInputFormat.class);
                    job.setMapperClass(SegmentFilterMapper.class);
                    job.setReducerClass(SegmentFilterReducer.class);
                    FileOutputFormat.setOutputPath(job, new Path(new Path(out, dstSegment), dir));
                    job.setOutputKeyClass(Text.class);
                    job.setOutputValueClass(MetaWrapper.class);
                    job.setOutputFormatClass(SegmentFilterReducer.SegmentOutputFormat.class);

                    Path lock = CrawlDb.lock(getConf(), currentCrawlDbPath, false);
                    try {
                        boolean success = job.waitForCompletion(true);
                        if (!success) {
                            String message = "Job did not succeed, job status:"
                                    + job.getStatus().getState() + ", reason: "
                                    + job.getStatus().getFailureInfo();
                            LOG.error(message);
                            throw new RuntimeException(message);
                        }
                    } catch (IOException | InterruptedException | ClassNotFoundException e) {
                        LOG.error("Job failed: {}", e);
                        throw e;
                    } finally {
                        LockUtil.removeLockFile(fs, lock);
                    }
                }

            }
        }
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new SegmentFilter(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("SegmentFilter output_dir crawldb_dir (-dir segments | seg1 seg2 ...) [-filter] [-normalize]");
            System.err.println("\toutput_dir\tname of the parent dir for output segment");
            System.err.println("\tcrawl_dir\tname of the crawldb to filter the segments against");
            System.err.println("\t-dir segments\tparent dir containing several segments");
            System.err.println("\tseg1 seg2 ...\tlist of segment dirs");
            System.err.println("\t-filter\t\tfilter out URL-s prohibited by current URLFilters");
            return -1;
        }
        try {
            final FileSystem fs = FileSystem.get(getConf());
            Path out = new Path(args[0]);
            Path crawlDbPath = new Path(args[1]);
            ArrayList<Path> segs = new ArrayList<Path>();
            boolean filter = false;
            boolean normalize = false;
            for (int i = 2; i < args.length; i++) {
                if (args[i].equals("-dir")) {
                    FileStatus[] fstats = fs.listStatus(new Path(args[++i]), HadoopFSUtil.getPassDirectoriesFilter(fs));
                    Path[] files = HadoopFSUtil.getPaths(fstats);
                    for (int j = 0; j < files.length; j++)
                        segs.add(files[j]);
                } else if (args[i].equals("-filter")) {
                    filter = true;
                } else if (args[i].equals("-normalize")) {
                    normalize = true;
                } else {
                    segs.add(new Path(args[i]));
                }
            }
            if (segs.size() == 0) {
                System.err.println("ERROR: No input segments.");
                return -1;
            }
            filter(out, crawlDbPath, segs.toArray(new Path[segs.size()]), filter, normalize);
            return 0;
        } catch (Exception e) {
            LOG.error("ParseDataUpdater: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
