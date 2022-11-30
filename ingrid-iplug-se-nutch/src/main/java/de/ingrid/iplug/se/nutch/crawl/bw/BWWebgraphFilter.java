/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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

package de.ingrid.iplug.se.nutch.crawl.bw;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.scoring.webgraph.LinkDatum;
import org.apache.nutch.scoring.webgraph.Node;
import org.apache.nutch.scoring.webgraph.WebGraph;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

import de.ingrid.iplug.se.nutch.net.InGridURLNormalizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Webgraph filter tool that filters urls in outlinks that do not pass a
 * white-black list. Only outlinks are filtered because the new computation of
 * the webgraph will always generate inlinks and nodes out of the outlinks.
 * 
 * @see BWInjector
 */
public class BWWebgraphFilter extends Configured implements Tool {

    public static final Logger LOG = LoggerFactory.getLogger(BWWebgraphFilter.class);

    public static final String CURRENT_NAME = "current";

    public static class ObjectWritableMapper extends Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, Writable value, Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable>.Context context) throws IOException, InterruptedException {
            ObjectWritable objectWritable = new ObjectWritable(value);
            context.write(key, objectWritable);
        }

    }

    /**
     * Wraps an LinkDatum object and stores also the url the Inlink refers to.
     * 
     * @see LinkDatum
     * 
     */
    public static class LinkDatumEntry implements Writable {

        private Text _url;

        private LinkDatum _linkDatum;

        public LinkDatumEntry() {
        }

        public LinkDatumEntry(Text url, LinkDatum linkDatum) {
            _url = url;
            _linkDatum = linkDatum;
        }

        public void write(DataOutput out) throws IOException {
            _url.write(out);
            _linkDatum.write(out);
        }

        public void readFields(DataInput in) throws IOException {
            _url = new Text();
            _url.readFields(in);
            _linkDatum = new LinkDatum();
            _linkDatum.readFields(in);
        }

        public String toString() {
            return _url.toString() + " : " + _linkDatum.toString();
        }

    }

    /**
     * Wraps an Node object and stores also the url the Node refers to.
     * 
     * @see Node
     * 
     */
    public static class NodeEntry implements Writable {

        private Text _url;

        private Node _node;

        public NodeEntry() {
        }

        public NodeEntry(Text url, Node node) {
            _url = url;
            _node = node;
        }

        public void write(DataOutput out) throws IOException {
            _url.write(out);
            _node.write(out);
        }

        public void readFields(DataInput in) throws IOException {
            _url = new Text();
            _url.readFields(in);
            _node = new Node();
            _node.readFields(in);
        }

        public String toString() {
            return _url.toString() + "\n " + _node.toString();
        }

    }

    /**
     * Wraps an url and an {@link LinkDatum} into an {@link LinkDatumEntry}
     * wrapper and associates it with a {@link HostTypeKey}. Standard
     * Url-Filtering and normalization can be applied optional.
     */
    public static class LinkDatumMapper extends Mapper<Text, LinkDatum, HostTypeKey, LinkDatumEntry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, LinkDatum value, Mapper<Text, LinkDatum, HostTypeKey, LinkDatumEntry>.Context context) throws IOException, InterruptedException {

            String url = key.toString();
            if (urlNormalizers) {
                try {
                    url = normalizers.normalize(url, scope); // normalize the
                    // url
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e);
                    url = null;
                }
            }
            if (url != null && urlFiltering) {
                try {
                    url = filters.filter(url); // filter the url
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e);
                    url = null;
                }
            }
            if (url != null) { // if it passes
                String host = new URL(url).getHost();
                LinkDatumEntry linkDatumEntry = new LinkDatumEntry(key, value);
                context.write(new HostTypeKey(host, HostTypeKey.GENERIC_DATUM_TYPE), linkDatumEntry);
            }

            url = value.getUrl();
            if (urlNormalizers) {
                try {
                    url = normalizers.normalize(url, scope); // normalize the
                    // url
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e);
                    url = null;
                }
            }
            if (url != null && urlFiltering) {
                try {
                    url = filters.filter(url); // filter the url
                } catch (Exception e) {
                    LOG.warn("Skipping " + url + ":" + e);
                    url = null;
                }
            }
            if (url != null) { // if it passes
                String host = new URL(url).getHost();
                LinkDatumEntry linkDatumEntry = new LinkDatumEntry(key, value);
                context.write(new HostTypeKey(host, HostTypeKey.LINKED_URL_TYPE), linkDatumEntry);
            }

        }

        @Override
        protected void setup(Mapper<Text, LinkDatum, HostTypeKey, LinkDatumEntry>.Context context) throws IOException, InterruptedException {
            super.setup(context);

            Configuration conf = context.getConfiguration();
            urlFiltering = conf.getBoolean(URL_FILTERING, false);
            urlNormalizers = conf.getBoolean(URL_NORMALIZING, false);
            if (urlFiltering) {
                filters = new URLFilters(conf);
            }
            if (urlNormalizers) {
                scope = conf.get(URL_NORMALIZING_SCOPE, InGridURLNormalizers.SCOPE_BWDB);
                normalizers = new URLNormalizers(conf, scope);
            }
        }

    }

    /**
     * Collects only entries that match a white list entry and no black list
     * entry. Here the filtering of the {@link LinkDatumEntry} takes place. Base
     * of the filtering is the BW DB.
     */
    public static class BwLinkDatumReducer extends Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable> {

        private BWPatterns _patterns;

        @Override
        public void reduce(HostTypeKey key, Iterable<ObjectWritable> values, Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable>.Context context) throws IOException, InterruptedException {

            for (ObjectWritable objectWritable : values) {
                Object value = objectWritable.get(); // unwrap

                if (value instanceof BWPatterns) {
                    _patterns = (BWPatterns) value;
                    // next values should be a list of entries
                    return;
                }

                if (_patterns == null) {
                    // return, because no bw pattern has been set for this url
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Key url or linked url does not have a valid BW patterns, remove it: " + value + " for HostTypeKey: " + key.toString());
                    }
                    return;
                }

                if (value instanceof LinkDatumEntry) {
                    LinkDatumEntry linkDatumEntry = (LinkDatumEntry) value;
                    String url = linkDatumEntry._url.toString();

                    if (key.getType() == HostTypeKey.GENERIC_DATUM_TYPE && _patterns.willPassBWLists(url)) {
                        // url is outside the black list and matches the white
                        // list
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Key url passed BW patterns: " + linkDatumEntry + " for HostTypeKey: " + key);
                        }
                        context.write(key, objectWritable);
                    } else if (key.getType() == HostTypeKey.LINKED_URL_TYPE && _patterns.willPassBWLists(linkDatumEntry._linkDatum.getUrl())) {
                        // linked url is outside the black list and matches the
                        // white
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Linked url passed BW patterns: " + linkDatumEntry + " for HostTypeKey: " + key);
                        }
                        context.write(key, objectWritable);
                    } else {

                        if (LOG.isDebugEnabled()) {

                            if (key.getType() == HostTypeKey.GENERIC_DATUM_TYPE) {
                                LOG.debug("Key url does not match BW patterns, remove it: " + linkDatumEntry + " for HostTypeKey: " + key);
                            } else {
                                LOG.debug("Linked url does not match BW patterns, remove it: " + linkDatumEntry + " for HostTypeKey: " + key);
                            }
                        }
                    }
                }

            }
        }

    }

    /**
     * Collects only {@link LinkDatum} entries that have two values attached.
     * This means that both key url as well as linked url have passed the BW
     * patterns (see {@link BwLinkDatumReducer}).
     */
    public static class FilterLinkDatumMapper extends Mapper<HostTypeKey, ObjectWritable, Text, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, ObjectWritable value, Mapper<HostTypeKey, ObjectWritable, Text, ObjectWritable>.Context context) throws IOException, InterruptedException {

            if (value.get() instanceof LinkDatumEntry) {
                context.write(new Text(( value.get()).toString()), value);
            }
        }
    }

    /**
     * Collects only {@link LinkDatum} entries that have two values attached.
     * This means that both key url as well as linked url have passed the BW
     * patterns (see {@link BwLinkDatumReducer}).
     */
    public static class FilterLinkDatumReducer extends Reducer<Text, ObjectWritable, Text, LinkDatum> {

        @Override
        public void reduce(Text key, Iterable<ObjectWritable> values, Reducer<Text, ObjectWritable, Text, LinkDatum>.Context context) throws IOException, InterruptedException {

            int cnt = 0;
            ObjectWritable value = null;
            while (values.iterator().hasNext()) {
                value = values.iterator().next();
                cnt++;
            }
            if (cnt == 2) {
                context.write(((LinkDatumEntry) value.get())._url, ((LinkDatumEntry) value.get())._linkDatum);
            }
        }
    }

    public BWWebgraphFilter() {
    }

    public void update(Path webgraphPath, Path bwdb, boolean normalize, boolean filter, boolean replaceWebgraph) throws IOException, InterruptedException, ClassNotFoundException {

        String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path outputWebgraph = new Path(getConf().get("hadoop.tmp.dir", "."), "webgraph_" + name);

        LOG.info("filter webgraph against bwdb: starting");
        LOG.info("filter webgraph against bwdb: output webgraph " + outputWebgraph);
        LOG.info("filter webgraph against bwdb: input webgraph " + webgraphPath);
        LOG.info("filter webgraph against bwdb: bwdb: " + bwdb);
        LOG.info("filter webgraph against bwdb: normalize urls: " + normalize);
        LOG.info("filter webgraph against bwdb: filter urls: " + filter);
        LOG.info("filter webgraph against bwdb: replace webgraph: " + replaceWebgraph);

        Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);

        // return if webgraph does not exist
        if (!fs.exists(webgraphPath)) {
            LOG.info("filter webgraph against bwdb: webgraph does not exist, nothing todo.");
            return;
        }

        // wrap outlinks
        LOG.info("filter webgraph against bwdb: wrap OUTLINK objects...");
        Job job = NutchJob.getInstance(conf);
        Configuration jobConf = job.getConfiguration();
        job.setJobName("filter webgraph against bwdb: wrapping OUTLINK objects from: " + webgraphPath);
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedOutlinkOutput = new Path(outputWebgraph, name);
        FileInputFormat.addInputPath(job, new Path(webgraphPath, WebGraph.OUTLINK_DIR));
        FileOutputFormat.setOutputPath(job, wrappedOutlinkOutput);
        job.setMapperClass(LinkDatumMapper.class);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(LinkDatumEntry.class);
        jobConf.setBoolean(LinkDatumMapper.URL_FILTERING, filter);
        jobConf.setBoolean(LinkDatumMapper.URL_NORMALIZING, normalize);
        Path lock = CrawlDb.lock(conf, webgraphPath, false);
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
            fs.delete(wrappedOutlinkOutput, true);
            throw e;
        } finally {
            LockUtil.removeLockFile(fs, lock);
        }

        // filter outlinks
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path filteredOutlinkDb = new Path(outputWebgraph, name);
        LOG.info("filter webgraph against bwdb: filter OUTLINK objects against bwdb.");
        job = NutchJob.getInstance(conf);
        job.setJobName("filter webgraph against bwdb: filter OUTLINK objects against bwdb." + wrappedOutlinkOutput + bwdb);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(job, wrappedOutlinkOutput);
        FileInputFormat.addInputPath(job, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(job, filteredOutlinkDb);
        job.setMapperClass(ObjectWritableMapper.class);
        job.setMapOutputKeyClass(HostTypeKey.class);
        job.setMapOutputValueClass(ObjectWritable.class);
        job.setReducerClass(BwLinkDatumReducer.class);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(ObjectWritable.class);
        lock = CrawlDb.lock(conf, bwdb, false);
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
            fs.delete(filteredOutlinkDb, true);
            throw e;
        } finally {
            NutchJob.cleanupAfterFailure(wrappedOutlinkOutput, lock, fs);
        }

        // filter for valid entries, keep only entries where key and link url
        // have passed the BW patters
        Path outputOutlinkDb = new Path(outputWebgraph, WebGraph.OUTLINK_DIR);
        LOG.info("filter webgraph against bwdb: filter OUTLINK objects for valid entries.");
        job = NutchJob.getInstance(conf);
        job.setJobName("filter webgraph against bwdb: filter OUTLINK objects for valid entries." + filteredOutlinkDb + bwdb);
        job.setInputFormatClass(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(job, filteredOutlinkDb);
        FileOutputFormat.setOutputPath(job, outputOutlinkDb);
        job.setMapperClass(FilterLinkDatumMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(ObjectWritable.class);
        job.setReducerClass(FilterLinkDatumReducer.class);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LinkDatum.class);
        lock = CrawlDb.lock(conf, filteredOutlinkDb, false);
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
            fs.delete(outputOutlinkDb, true);
            throw e;
        } finally {
            NutchJob.cleanupAfterFailure(filteredOutlinkDb, lock, fs);
        }

        /*
         * LOG.info("filter webgraph against bwdb: rebuild INLINK db."); Path
         * outputInlinkDb = new Path(outputWebgraph, WebGraph.INLINK_DIR);
         * 
         * JobConf inlinkJob = new NutchJob(conf);
         * inlinkJob.setJobName("Inlinkdb " + outputInlinkDb);
         * FileInputFormat.addInputPath(inlinkJob, outputOutlinkDb);
         * inlinkJob.setInputFormat(SequenceFileInputFormat.class);
         * inlinkJob.setMapperClass(InlinkDb.class);
         * inlinkJob.setMapOutputKeyClass(Text.class);
         * inlinkJob.setMapOutputValueClass(LinkDatum.class);
         * inlinkJob.setOutputKeyClass(Text.class);
         * inlinkJob.setOutputValueClass(LinkDatum.class);
         * FileOutputFormat.setOutputPath(inlinkJob, outputInlinkDb);
         * inlinkJob.setOutputFormat(MapFileOutputFormat.class);
         * SyncUtil.syncJobRun(inlinkJob);// JobClient.runJob(filterJob);
         * 
         * // rebuild node db
         * LOG.info("filter webgraph against bwdb: rebuild NODE db."); Path
         * outputNodeDb = new Path(outputWebgraph, WebGraph.NODE_DIR); JobConf
         * nodeJob = new NutchJob(conf); nodeJob.setJobName("NodeDb " +
         * outputNodeDb); LOG.info("NodeDb: adding input: " + outputOutlinkDb);
         * LOG.info("NodeDb: adding input: " + outputInlinkDb);
         * FileInputFormat.addInputPath(nodeJob, outputOutlinkDb);
         * FileInputFormat.addInputPath(nodeJob, outputInlinkDb);
         * nodeJob.setInputFormat(SequenceFileInputFormat.class);
         * nodeJob.setReducerClass(WebGraph.NodeDb.class);
         * nodeJob.setMapOutputKeyClass(Text.class);
         * nodeJob.setMapOutputValueClass(LinkDatum.class);
         * nodeJob.setOutputKeyClass(Text.class);
         * nodeJob.setOutputValueClass(Node.class);
         * FileOutputFormat.setOutputPath(nodeJob, outputNodeDb);
         * nodeJob.setOutputFormat(MapFileOutputFormat.class);
         * SyncUtil.syncJobRun(nodeJob);// JobClient.runJob(filterJob);
         */

        if (replaceWebgraph) {
            LOG.info("filter webgraph against bwdb: replace current webgraph");
            Path old = new Path(conf.get("mapred.temp.dir", ".") + "webgraph_old");
            if (fs.exists(webgraphPath)) {
                if (fs.exists(old))
                    fs.delete(old, true);
                fs.rename(webgraphPath, old);
            }
            fs.mkdirs(webgraphPath);
            // fs.rename(outputNodeDb, new Path(webgraphPath,
            // WebGraph.NODE_DIR));
            fs.rename(outputOutlinkDb, new Path(webgraphPath, WebGraph.OUTLINK_DIR));
            // fs.rename(outputInlinkDb, new Path(webgraphPath,
            // WebGraph.INLINK_DIR));
            if (fs.exists(old))
                fs.delete(old, true);
            fs.delete(outputWebgraph, true);
        }
        LOG.info("ilter webgraph against bwdb: finished.");

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new BWWebgraphFilter(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: BWWebgraphFilter <webgraphdb> <bwdb> <normalize> <filter> <replace current webgraph>");
            return -1;
        }
        try {

            update(new Path(args[0]), new Path(args[1]), Boolean.parseBoolean(args[2]), Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]));

            return 0;
        } catch (Exception e) {
            LOG.error("BWWebgraphFilter: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
