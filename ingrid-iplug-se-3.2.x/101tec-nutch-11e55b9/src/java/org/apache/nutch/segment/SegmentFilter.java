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
package org.apache.nutch.segment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.hadoop.mapred.SequenceFileRecordReader;
import org.apache.hadoop.util.Progressable;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.Generator;
import org.apache.nutch.metadata.MetaWrapper;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.util.HadoopFSUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

import de.ingrid.iplug.se.crawl.sns.CompressedSnsData;

/**
 * This tool takes several segments and filters them against the crawlDB. It can
 * be used to reduce the segments data if the crawlDB has been filtered.
 * 
 * @author Joachim Müller
 */
public class SegmentFilter extends Configured implements Mapper<Text, MetaWrapper, Text, MetaWrapper>,
        Reducer<Text, MetaWrapper, Text, MetaWrapper> {
    private static final Log LOG = LogFactory.getLog(SegmentFilter.class);

    private URLFilters filters = null;

    private URLNormalizers normalizers = null;

    private long curCount = 0;

    private static final String INPUT_PART_KEY = "part";

    /**
     * Wraps inputs in an {@link MetaWrapper}, to permit merging different types
     * in reduce and use additional metadata.
     */
    public static class ObjectInputFormat extends SequenceFileInputFormat<Text, MetaWrapper> {

        @Override
        public RecordReader<Text, MetaWrapper> getRecordReader(final InputSplit split, final JobConf job,
                Reporter reporter) throws IOException {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Return RecordReader for '" + split.toString() + "'");
            }

            reporter.setStatus(split.toString());

            // find part name
            SegmentPart segmentPart;
            final String spString;
            final FileSplit fSplit = (FileSplit) split;
            try {
                segmentPart = SegmentPart.get(fSplit);
                spString = segmentPart.toString();
            } catch (IOException e) {
                throw new RuntimeException("Cannot identify segment or cawldb:", e);
            }

            final SequenceFile.Reader reader = new SequenceFile.Reader(FileSystem.get(job), fSplit.getPath(), job);

            final Writable w;
            try {
                w = (Writable) reader.getValueClass().newInstance();
            } catch (Exception e) {
                throw new IOException(e.toString());
            }

            try {
                return new SequenceFileRecordReader<Text, MetaWrapper>(job, fSplit) {

                    public synchronized boolean next(Text key, MetaWrapper wrapper) throws IOException {

                        boolean res = reader.next(key, w);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Running OIF.next(), reading key: '" + key.toString() + "'. Setting Metadata."
                                    + INPUT_PART_KEY + ": '" + spString + "'");
                        }
                        wrapper.set(w);
                        wrapper.setMeta(INPUT_PART_KEY, spString);
                        return res;
                    }

                    @Override
                    public synchronized void close() throws IOException {
                        reader.close();
                    }

                    @Override
                    public MetaWrapper createValue() {
                        return new MetaWrapper();
                    }

                };
            } catch (IOException e) {
                throw new RuntimeException("Cannot create RecordReader: ", e);
            }
        }
    }

    public static class SegmentOutputFormat extends FileOutputFormat<Text, MetaWrapper> {

        @Override
        public RecordWriter<Text, MetaWrapper> getRecordWriter(final FileSystem fs, final JobConf job,
                final String name, final Progressable progress) throws IOException {
            return new RecordWriter<Text, MetaWrapper>() {
                MapFile.Writer c_out = null;

                MapFile.Writer f_out = null;

                MapFile.Writer pd_out = null;

                MapFile.Writer pt_out = null;

                SequenceFile.Writer g_out = null;

                SequenceFile.Writer p_out = null;

                SequenceFile.Writer sns_out = null;

                HashMap sliceWriters = new HashMap();

                public void write(Text key, MetaWrapper wrapper) throws IOException {

                    // unwrap
                    SegmentPart sp = SegmentPart.parse(wrapper.getMeta(INPUT_PART_KEY));
                    Writable o = (Writable) wrapper.get();
                    if (o instanceof CrawlDatum) {
                        if (sp.partName.equals(CrawlDatum.GENERATE_DIR_NAME)) {
                            g_out = ensureSequenceFile(sp.segmentName, CrawlDatum.GENERATE_DIR_NAME);
                            g_out.append(key, o);
                        } else if (sp.partName.equals(CrawlDatum.FETCH_DIR_NAME)) {
                            f_out = ensureMapFile(sp.segmentName, CrawlDatum.FETCH_DIR_NAME, CrawlDatum.class);
                            f_out.append(key, o);
                        } else if (sp.partName.equals(CrawlDatum.PARSE_DIR_NAME)) {
                            p_out = ensureSequenceFile(sp.segmentName, CrawlDatum.PARSE_DIR_NAME);
                            p_out.append(key, o);
                        } else {
                            throw new IOException("Cannot determine segment part: " + sp.partName);
                        }
                    } else if (o instanceof Content) {
                        c_out = ensureMapFile(sp.segmentName, Content.DIR_NAME, Content.class);
                        c_out.append(key, o);
                    } else if (o instanceof ParseData) {
                        // update the segment name inside contentMeta - required
                        // by Indexer
                        ((ParseData) o).getContentMeta().set(Nutch.SEGMENT_NAME_KEY, sp.segmentName);
                        pd_out = ensureMapFile(sp.segmentName, ParseData.DIR_NAME, ParseData.class);
                        pd_out.append(key, o);
                    } else if (o instanceof ParseText) {
                        pt_out = ensureMapFile(sp.segmentName, ParseText.DIR_NAME, ParseText.class);
                        pt_out.append(key, o);
                    } else if (o instanceof CompressedSnsData) {
                        sns_out = ensureSequenceFile(sp.segmentName, CompressedSnsData.DIR_NAME,
                                CompressedSnsData.class);
                        sns_out.append(key, o);
                    }
                }

                // lazily create SequenceFile-s.
                private SequenceFile.Writer ensureSequenceFile(String segName, String dirName) throws IOException {
                    return ensureSequenceFile(segName, dirName, CrawlDatum.class);
                }

                private SequenceFile.Writer ensureSequenceFile(String segName, String dirName,
                        Class<? extends Writable> clazz) throws IOException {
                    SequenceFile.Writer res = (SequenceFile.Writer) sliceWriters.get(segName + dirName);
                    if (res != null)
                        return res;
                    Path out = FileOutputFormat.getOutputPath(job);
                    Path wname = new Path(new Path(new Path(out, segName), dirName), name);
                    res = SequenceFile.createWriter(fs, job, wname, Text.class, clazz, SequenceFileOutputFormat
                            .getOutputCompressionType(job), progress);
                    sliceWriters.put(segName + dirName, res);
                    return res;
                }

                // lazily create MapFile-s.
                private MapFile.Writer ensureMapFile(String segName, String dirName, Class<? extends Writable> clazz)
                        throws IOException {
                    MapFile.Writer res = (MapFile.Writer) sliceWriters.get(segName + dirName);
                    if (res != null)
                        return res;
                    Path out = FileOutputFormat.getOutputPath(job);
                    Path wname = new Path(new Path(new Path(out, segName), dirName), name);
                    CompressionType compType = SequenceFileOutputFormat.getOutputCompressionType(job);
                    if (clazz.isAssignableFrom(ParseText.class)) {
                        compType = CompressionType.RECORD;
                    }
                    res = new MapFile.Writer(job, fs, wname.toString(), Text.class, clazz, compType, progress);
                    sliceWriters.put(segName + dirName, res);
                    return res;
                }

                public void close(Reporter reporter) throws IOException {
                    Iterator<Object> it = sliceWriters.values().iterator();
                    while (it.hasNext()) {
                        Object o = it.next();
                        if (o instanceof SequenceFile.Writer) {
                            ((SequenceFile.Writer) o).close();
                        } else {
                            ((MapFile.Writer) o).close();
                        }
                    }
                }
            };
        }
    }

    public SegmentFilter() {
        super(null);
    }

    public SegmentFilter(Configuration conf) {
        super(conf);
    }

    HashMap<String, String> segments = null;

    public void setConf(Configuration conf) {
        super.setConf(conf);
        if (conf == null)
            return;
        if (conf.getBoolean("segment.filter.filter", false))
            filters = new URLFilters(conf);
        if (conf.getBoolean("segment.filter.normalizer", false))
            normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_DEFAULT);

        String[] inSegs = conf.getStrings("segment.filter.segments.input");
        String[] outSegs = conf.getStrings("segment.filter.segments.output");

        if (segments == null && inSegs != null && outSegs != null) {
            segments = new HashMap<String, String>();
            for (int i = 0; i < inSegs.length; i++) {
                segments.put(inSegs[i], outSegs[i]);
            }
        }

    }

    public void close() throws IOException {
    }

    public void configure(JobConf conf) {
        setConf(conf);
    }

    private Text newKey = new Text();

    public void map(Text key, MetaWrapper value, OutputCollector<Text, MetaWrapper> output, Reporter reporter)
            throws IOException {
        String url = key.toString();
        if (normalizers != null) {
            try {
                url = normalizers.normalize(url, URLNormalizers.SCOPE_DEFAULT); // normalize
            } catch (Exception e) {
                LOG.warn("Skipping " + url + ":" + e.getMessage());
                url = null;
            }
        }
        if (url != null && filters != null) {
            try {
                url = filters.filter(url);
            } catch (Exception e) {
                LOG.warn("Skipping key " + url + ": " + e.getMessage());
                url = null;
            }
        }
        if (url != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Calling map() with key: '" + url + "'");
            }
            newKey.set(url);
            output.collect(newKey, value);
        }
    }

    public void reduce(Text key, Iterator<MetaWrapper> values, OutputCollector<Text, MetaWrapper> output,
            Reporter reporter) throws IOException {

        // stores segment data for every segment/part
        HashMap<String, Writable> segData = new HashMap<String, Writable>();

        // stores link data for every segment
        HashMap<String, ArrayList<CrawlDatum>> linked = new HashMap<String, ArrayList<CrawlDatum>>();
        
        HashMap<String, ArrayList<CrawlDatum>> linkedFetchDatum = new HashMap<String, ArrayList<CrawlDatum>>();
        

        boolean hasCrawlDbEntry = false;
        while (values.hasNext()) {
            MetaWrapper wrapper = values.next();
            Writable o = wrapper.get();
            String spString = wrapper.getMeta(INPUT_PART_KEY);
            SegmentPart sp = SegmentPart.parse(spString);
            // check for crawldatum entry
            if (o instanceof CrawlDatum && CrawlDatum.hasDbStatus((CrawlDatum) o)) {
                if (!sp.partName.equals(CrawlDatum.GENERATE_DIR_NAME)) {
                    hasCrawlDbEntry = true;
                    continue;
                }
                // check for link data
            } else if (o instanceof CrawlDatum && sp.partName.equals(CrawlDatum.PARSE_DIR_NAME)) {
                CrawlDatum val = (CrawlDatum) o;
                if (val.getStatus() != CrawlDatum.STATUS_SIGNATURE) {
                    ArrayList<CrawlDatum> segLinked = linked.get(sp.segmentName);
                    if (segLinked == null) {
                        segLinked = new ArrayList<CrawlDatum>();
                        linked.put(sp.segmentName, segLinked);
                    }
                    segLinked.add(val);
                    continue;
                }
                // check for link data
            } else if (o instanceof CrawlDatum && sp.partName.equals(CrawlDatum.FETCH_DIR_NAME)) {
                CrawlDatum val = (CrawlDatum) o;
                if (val.getStatus() == CrawlDatum.STATUS_LINKED) {
                    ArrayList<CrawlDatum> segLinkedFetchDatum = linkedFetchDatum.get(sp.segmentName);
                    if (segLinkedFetchDatum == null) {
                        segLinkedFetchDatum = new ArrayList<CrawlDatum>();
                        linkedFetchDatum.put(sp.segmentName, segLinkedFetchDatum);
                    }
                    segLinkedFetchDatum.add(val);
                    continue;
                }
            }
            // store segment data for later, remove duplicates using hash map
            segData.put(spString, o);
        }

        if (hasCrawlDbEntry) {
            // ok we had a crawl db entry
            MetaWrapper wrapper = new MetaWrapper();
            SegmentPart sp = new SegmentPart();
            for (String spString : segData.keySet()) {
                // write out segment data
                sp = SegmentPart.parse(spString);
                // translate segment name to the new segment name
                sp = new SegmentPart(segments.get(sp.segmentName), sp.partName);
                wrapper.setMeta(INPUT_PART_KEY, sp.toString());
                wrapper.set(segData.get(spString));
                output.collect(key, wrapper);
            }
            if (linked.size() > 0) {
                // write out link data
                for (String name : linked.keySet()) {
                    sp.partName = CrawlDatum.PARSE_DIR_NAME;
                    // translate segment name to the new segment name
                    sp.segmentName = segments.get(name);
                    wrapper.setMeta(INPUT_PART_KEY, sp.toString());
                    // write out link data
                    ArrayList<CrawlDatum> segLinked = linked.get(name);
                    for (int i = 0; i < segLinked.size(); i++) {
                        CrawlDatum link = segLinked.get(i);
                        wrapper.set(link);
                        output.collect(key, wrapper);
                    }

                }
            }
            if (linkedFetchDatum.size() > 0) {
                // write out link data
                for (String name : linkedFetchDatum.keySet()) {
                    sp.partName = CrawlDatum.FETCH_DIR_NAME;
                    // translate segment name to the new segment name
                    sp.segmentName = segments.get(name);
                    wrapper.setMeta(INPUT_PART_KEY, sp.toString());
                    // write out link data
                    ArrayList<CrawlDatum> segLinkedFetchDatum = linkedFetchDatum.get(name);
                    for (int i = 0; i < segLinkedFetchDatum.size(); i++) {
                        CrawlDatum link = segLinkedFetchDatum.get(i);
                        wrapper.set(link);
                        output.collect(key, wrapper);
                    }

                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Remove entries in segments '" + segments.keySet() + "' for key: " + key);
            }
        }
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
        directories.add(CompressedSnsData.DIR_NAME);

        JobConf job = new NutchJob(getConf());

        job.setJobName("filtersegs " + out);
        job.setBoolean("segment.merger.filter", filter);
        job.setBoolean("segment.merger.normalizer", normalize);

        Path currentCrawlDbPath = new Path(crawlDbPath, CrawlDb.CURRENT_NAME);
        FileSystem fs = FileSystem.get(getConf());

        if (!fs.exists(currentCrawlDbPath)) {
            LOG.error("CrawlDb does not exist: " + currentCrawlDbPath);
            return;
        }

        FileInputFormat.addInputPath(job, currentCrawlDbPath);

        String[] inSegs = new String[segs.length];
        String[] outSegs = new String[segs.length];

        for (int i = 0; i < segs.length; i++) {
            inSegs[i] = segs[i].getName();
            outSegs[i] = Generator.generateSegmentName();
            if (LOG.isInfoEnabled()) {
                LOG.info("Filter segment " + segs[i] + " to " + out + "/" + outSegs[i]);
            }
            for (int j = 0; j < directories.size(); j++) {
                Path gDir = new Path(segs[i], directories.get(j));
                if (fs.exists(gDir)) {
                    FileInputFormat.addInputPath(job, gDir);
                }
            }
        }

        job.setStrings("segment.filter.segments.input", inSegs);
        job.setStrings("segment.filter.segments.output", outSegs);

        job.setInputFormat(ObjectInputFormat.class);
        job.setMapperClass(SegmentFilter.class);
        job.setReducerClass(SegmentFilter.class);
        FileOutputFormat.setOutputPath(job, out);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(MetaWrapper.class);
        job.setOutputFormat(SegmentOutputFormat.class);

        setConf(job);

        JobClient.runJob(job);
        
        // ensure empty segment parts are copied
        // as empty parts will not be created by the merge process
        for (int i = 0; i < segs.length; i++) {
            for (int j = 0; j < directories.size(); j++) {
                Path gDir = new Path(segs[i], directories.get(j));
                Path oDir = new Path(new Path(out,  outSegs[i]), directories.get(j));
                if (fs.exists(gDir) && !fs.exists(oDir)) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Possibly empty source directory? Copy " + gDir + " to " + oDir);
                    }
                    FileUtil.copy(fs, gDir, fs, oDir, false, getConf());
                } 
            }
        }

    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err
                    .println("SegmentFilter output_dir crawldb_dir (-dir segments | seg1 seg2 ...) [-filter] [-normalize]");
            System.err.println("\toutput_dir\tname of the parent dir for output segment");
            System.err.println("\tcrawl_dir\tname of the crawldb to filter the segments against");
            System.err.println("\t-dir segments\tparent dir containing several segments");
            System.err.println("\tseg1 seg2 ...\tlist of segment dirs");
            System.err.println("\t-filter\t\tfilter out URL-s prohibited by current URLFilters");
            return;
        }
        Configuration conf = NutchConfiguration.create();
        final FileSystem fs = FileSystem.get(conf);
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
            return;
        }
        SegmentFilter segmentFilter = new SegmentFilter(conf);
        segmentFilter.filter(out, crawlDbPath, segs.toArray(new Path[segs.size()]), filter, normalize);
    }

}
