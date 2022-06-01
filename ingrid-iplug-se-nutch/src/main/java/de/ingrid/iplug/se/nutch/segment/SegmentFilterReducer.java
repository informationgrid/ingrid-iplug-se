/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2021 wemove digital solutions GmbH
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Metadata;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.compress.DefaultCodec;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.metadata.MetaWrapper;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.segment.SegmentPart;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This tool takes several segments and filters them against the crawlDB. It can
 * be used to reduce the segments data if the crawlDB has been filtered.
 * 
 * @author Joachim Müller
 */
public class SegmentFilterReducer extends Reducer<Text, MetaWrapper, Text, MetaWrapper> {
    private static final Log LOG = LogFactory.getLog(SegmentFilterReducer.class);

    private static final String SEGMENT_PART_KEY = "part";
    private static final String SEGMENT_SLICE_KEY = "slice";

    private long sliceSize = -1;
    private long curCount = 0;

    String segmentName = null;


    public static class SegmentOutputFormat extends
            FileOutputFormat<Text, MetaWrapper> {
        private static final String DEFAULT_SLICE = "default";

        @Override
        public RecordWriter<Text, MetaWrapper> getRecordWriter(TaskAttemptContext context)
                throws IOException {
            Configuration conf = context.getConfiguration();
            String name = getUniqueFile(context, "part", "");
            Path dir = FileOutputFormat.getOutputPath(context);
            FileSystem fs = dir.getFileSystem(context.getConfiguration());

            return new RecordWriter<Text, MetaWrapper>() {
                MapFile.Writer cOut = null;
                MapFile.Writer fOut = null;
                MapFile.Writer pdOut = null;
                MapFile.Writer ptOut = null;
                SequenceFile.Writer gOut = null;
                SequenceFile.Writer pOut = null;
                HashMap<String, Closeable> sliceWriters = new HashMap<>();
                String segmentName = conf.get("segment.merger.segmentName");

                public void write(Text key, MetaWrapper wrapper) throws IOException {
                    // unwrap
                    SegmentPart sp = SegmentPart.parse(wrapper.getMeta(SEGMENT_PART_KEY));
                    Writable o = wrapper.get();
                    String slice = wrapper.getMeta(SEGMENT_SLICE_KEY);
                    if (o instanceof CrawlDatum) {
                        if (sp.partName.equals(CrawlDatum.GENERATE_DIR_NAME)) {
                            gOut = ensureSequenceFile(slice, CrawlDatum.GENERATE_DIR_NAME);
                            gOut.append(key, o);
                        } else if (sp.partName.equals(CrawlDatum.FETCH_DIR_NAME)) {
                            fOut = ensureMapFile(slice, CrawlDatum.FETCH_DIR_NAME,
                                    CrawlDatum.class);
                            fOut.append(key, o);
                        } else if (sp.partName.equals(CrawlDatum.PARSE_DIR_NAME)) {
                            pOut = ensureSequenceFile(slice, CrawlDatum.PARSE_DIR_NAME);
                            pOut.append(key, o);
                        } else {
                            throw new IOException("Cannot determine segment part: "
                                    + sp.partName);
                        }
                    } else if (o instanceof Content) {
                        cOut = ensureMapFile(slice, Content.DIR_NAME, Content.class);
                        cOut.append(key, o);
                    } else if (o instanceof ParseData) {
                        // update the segment name inside contentMeta - required by Indexer
                        if (slice == null) {
                            ((ParseData) o).getContentMeta().set(Nutch.SEGMENT_NAME_KEY,
                                    segmentName);
                        } else {
                            ((ParseData) o).getContentMeta().set(Nutch.SEGMENT_NAME_KEY,
                                    segmentName + "-" + slice);
                        }
                        pdOut = ensureMapFile(slice, ParseData.DIR_NAME, ParseData.class);
                        pdOut.append(key, o);
                    } else if (o instanceof ParseText) {
                        ptOut = ensureMapFile(slice, ParseText.DIR_NAME, ParseText.class);
                        ptOut.append(key, o);
                    }
                }

                // lazily create SequenceFile-s.
                private SequenceFile.Writer ensureSequenceFile(String slice,
                                                               String dirName) throws IOException {
                    if (slice == null)
                        slice = DEFAULT_SLICE;
                    SequenceFile.Writer res = (SequenceFile.Writer) sliceWriters
                            .get(slice + dirName);
                    if (res != null)
                        return res;
                    Path wname;
                    Path out = FileOutputFormat.getOutputPath(context);
                    // CHANGE START HERE
                    // See Segment.SegmentOutputFormat, where this code was copied from.
                    if (slice == DEFAULT_SLICE) {
                        // Change path to be able to work only on part of a segment.
                        // See Segment.SegmentOutputFormat, where this code was copied from.
                        wname = new Path(out, name);
                    } else {
                        throw new RuntimeException("Non default slice detected, See Segment.SegmentOutputFormat, where this code was copied from.");
                    }
                    // CHANGE STOP HERE

                    res = SequenceFile.createWriter(conf, SequenceFile.Writer.file(wname),
                            SequenceFile.Writer.keyClass(Text.class),
                            SequenceFile.Writer.valueClass(CrawlDatum.class),
                            SequenceFile.Writer.bufferSize(fs.getConf().getInt("io.file.buffer.size",4096)),
                            SequenceFile.Writer.replication(fs.getDefaultReplication(wname)),
                            SequenceFile.Writer.blockSize(1073741824),
                            SequenceFile.Writer.compression(SequenceFileOutputFormat.getOutputCompressionType(context), new DefaultCodec()),
                            SequenceFile.Writer.progressable((Progressable)context),
                            SequenceFile.Writer.metadata(new Metadata()));

                    sliceWriters.put(slice + dirName, res);
                    return res;
                }

                // lazily create MapFile-s.
                private MapFile.Writer ensureMapFile(String slice, String dirName,
                                                     Class<? extends Writable> clazz) throws IOException {
                    if (slice == null)
                        slice = DEFAULT_SLICE;
                    MapFile.Writer res = (MapFile.Writer) sliceWriters.get(slice
                            + dirName);
                    if (res != null)
                        return res;
                    Path wname;
                    Path out = FileOutputFormat.getOutputPath(context);

                    // CHANGE START HERE
                    // See Segment.SegmentOutputFormat, where this code was copied from.
                    if (slice == DEFAULT_SLICE) {
                        // Change path to be able to work only on part of a segment.
                        // See Segment.SegmentOutputFormat, where this code was copied from.
                        wname = new Path(out, name);
                    } else {
                        throw new RuntimeException("Non default slice detected, See Segment.SegmentOutputFormat, where this code was copied from.");
                    }
                    // CHANGE STOP HERE

                    SequenceFile.CompressionType compType = SequenceFileOutputFormat
                            .getOutputCompressionType(context);
                    if (clazz.isAssignableFrom(ParseText.class)) {
                        compType = SequenceFile.CompressionType.RECORD;
                    }

                    MapFile.Writer.Option rKeyClassOpt = MapFile.Writer.keyClass(Text.class);
                    org.apache.hadoop.io.SequenceFile.Writer.Option rValClassOpt = SequenceFile.Writer.valueClass(clazz);
                    org.apache.hadoop.io.SequenceFile.Writer.Option rProgressOpt = SequenceFile.Writer.progressable((Progressable)context);
                    org.apache.hadoop.io.SequenceFile.Writer.Option rCompOpt = SequenceFile.Writer.compression(compType);

                    res = new MapFile.Writer(conf, wname, rKeyClassOpt,
                            rValClassOpt, rCompOpt, rProgressOpt);
                    sliceWriters.put(slice + dirName, res);
                    return res;
                }

                @Override
                public void close(TaskAttemptContext context) throws IOException {
                    Iterator<Closeable> it = sliceWriters.values().iterator();
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


    @Override
    protected void setup(Reducer<Text, MetaWrapper, Text, MetaWrapper>.Context context) throws IOException, InterruptedException {
        super.setup(context);

        Configuration conf = context.getConfiguration();
        segmentName = conf.get("segment.filter.segmentName");

        sliceSize = conf.getLong("segment.filter.slice", -1);
        if ((sliceSize > 0) && (LOG.isInfoEnabled())) {
            LOG.info("Slice size: " + sliceSize + " URLs.");
        }

        if (sliceSize > 0) {
            sliceSize = sliceSize / context.getNumReduceTasks();
        }
    }

    @Override
    public void reduce(Text key, Iterable<MetaWrapper> values, Reducer<Text, MetaWrapper, Text, MetaWrapper>.Context context) throws IOException, InterruptedException {

        // stores segment data for every segment/part
        HashMap<String, Writable> segData = new HashMap<String, Writable>();

        // stores link data for every segment
        HashMap<String, ArrayList<CrawlDatum>> linked = new HashMap<String, ArrayList<CrawlDatum>>();

        HashMap<String, ArrayList<CrawlDatum>> linkedFetchDatum = new HashMap<String, ArrayList<CrawlDatum>>();

        boolean hasCrawlDbEntry = false;
        while (values.iterator().hasNext()) {
            MetaWrapper wrapper = values.iterator().next();
            Writable o = wrapper.get();
            String spString = wrapper.getMeta(SEGMENT_PART_KEY);
            if (spString == null) {
                throw new IOException("Null segment part, key=" + key);
            }
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
                    boolean cdExists = false;
                    for (CrawlDatum cd : segLinked) {
                        if (cd.equals(val)) {
                            cdExists = true;
                            break;
                        }
                    }
                    if (!cdExists) {
                        segLinked.add(val);
                    }
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
            curCount++;
            String sliceName = null;
            MetaWrapper wrapper = new MetaWrapper();
            if (sliceSize > 0) {
                sliceName = String.valueOf(curCount / sliceSize);
                wrapper.setMeta(SEGMENT_SLICE_KEY, sliceName);
            }
            SegmentPart sp = new SegmentPart();
            for (String spString : segData.keySet()) {
                // write out segment data
                sp = SegmentPart.parse(spString);
                // translate segment name to the new segment name
                sp = new SegmentPart(segmentName, sp.partName);
                wrapper.setMeta(SEGMENT_PART_KEY, sp.toString());
                wrapper.set(segData.get(spString));
                context.write(key, wrapper);
            }
            if (linked.size() > 0) {
                // write out link data
                for (String name : linked.keySet()) {
                    sp.partName = CrawlDatum.PARSE_DIR_NAME;
                    // translate segment name to the new segment name
                    sp.segmentName = segmentName;
                    wrapper.setMeta(SEGMENT_PART_KEY, sp.toString());
                    // write out link data
                    ArrayList<CrawlDatum> segLinked = linked.get(name);
                    for (int i = 0; i < segLinked.size(); i++) {
                        CrawlDatum link = segLinked.get(i);
                        wrapper.set(link);
                        context.write(key, wrapper);
                    }

                }
            }
            if (linkedFetchDatum.size() > 0) {
                // write out link data
                for (String name : linkedFetchDatum.keySet()) {
                    sp.partName = CrawlDatum.FETCH_DIR_NAME;
                    // translate segment name to the new segment name
                    sp.segmentName = segmentName;
                    wrapper.setMeta(SEGMENT_PART_KEY, sp.toString());
                    // write out link data
                    ArrayList<CrawlDatum> segLinkedFetchDatum = linkedFetchDatum.get(name);
                    for (int i = 0; i < segLinkedFetchDatum.size(); i++) {
                        CrawlDatum link = segLinkedFetchDatum.get(i);
                        wrapper.set(link);
                        context.write(key, wrapper);
                    }

                }
            }
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Remove entries in segments for key: " + key);
            }
        }
    }


}
