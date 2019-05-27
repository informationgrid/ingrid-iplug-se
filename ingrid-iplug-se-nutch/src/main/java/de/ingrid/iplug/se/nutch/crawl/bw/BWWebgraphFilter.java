/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
/**
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
import java.util.Iterator;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.scoring.webgraph.LinkDatum;
import org.apache.nutch.scoring.webgraph.Node;
import org.apache.nutch.scoring.webgraph.WebGraph;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

import de.ingrid.iplug.se.nutch.net.InGridURLNormalizers;

/**
 * Webgraph filter tool that filters urls in outlinks that do not pass a
 * white-black list. Only outlinks are filtered because the new computation of
 * the webgraph will always generate inlinks and nodes out of the outlinks.
 * 
 * @see BWInjector
 */
public class BWWebgraphFilter extends Configured implements Tool {

    public static final Log LOG = LogFactory.getLog(BWWebgraphFilter.class);

    public static final String CURRENT_NAME = "current";

    public static class ObjectWritableMapper implements Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, Writable value, OutputCollector<HostTypeKey, ObjectWritable> collector, Reporter reporter) throws IOException {
            ObjectWritable objectWritable = new ObjectWritable(value);
            collector.collect(key, objectWritable);
        }

        @Override
        public void configure(JobConf jobConf) {

        }

        @Override
        public void close() throws IOException {

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
    public static class LinkDatumMapper implements Mapper<Text, LinkDatum, HostTypeKey, LinkDatumEntry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, LinkDatum value, OutputCollector<HostTypeKey, LinkDatumEntry> out, Reporter rep) throws IOException {

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
                out.collect(new HostTypeKey(host, HostTypeKey.GENERIC_DATUM_TYPE), linkDatumEntry);
            }

            url = value.getUrl().toString();
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
                out.collect(new HostTypeKey(host, HostTypeKey.LINKED_URL_TYPE), linkDatumEntry);
            }

        }

        public void configure(JobConf job) {
            urlFiltering = job.getBoolean(URL_FILTERING, false);
            urlNormalizers = job.getBoolean(URL_NORMALIZING, false);
            if (urlFiltering) {
                filters = new URLFilters(job);
            }
            if (urlNormalizers) {
                scope = job.get(URL_NORMALIZING_SCOPE, InGridURLNormalizers.SCOPE_BWDB);
                normalizers = new URLNormalizers(job, scope);
            }
        }

        public void close() throws IOException {
        }

    }

    /**
     * Collects only entries that match a white list entry and no black list
     * entry. Here the filtering of the {@link LinkDatumEntry} takes place. Base
     * of the filtering is the BW DB.
     */
    public static class BwLinkDatumReducer implements Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable> {

        private BWPatterns _patterns;

        public void reduce(HostTypeKey key, Iterator<ObjectWritable> values, OutputCollector<HostTypeKey, ObjectWritable> out, Reporter report) throws IOException {

            while (values.hasNext()) {
                ObjectWritable objectWritable = (ObjectWritable) values.next();
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
                            LOG.debug("Key url passed BW patterns: " + linkDatumEntry + " for HostTypeKey: " + key.toString());
                        }
                        out.collect(key, objectWritable);
                    } else if (key.getType() == HostTypeKey.LINKED_URL_TYPE && _patterns.willPassBWLists(linkDatumEntry._linkDatum.getUrl())) {
                        // linked url is outside the black list and matches the
                        // white
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Linked url passed BW patterns: " + linkDatumEntry + " for HostTypeKey: " + key.toString());
                        }
                        out.collect(key, objectWritable);
                    } else {

                        if (LOG.isDebugEnabled()) {

                            if (key.getType() == HostTypeKey.GENERIC_DATUM_TYPE) {
                                LOG.debug("Key url does not match BW patterns, remove it: " + linkDatumEntry + " for HostTypeKey: " + key.toString());
                            } else {
                                LOG.debug("Linked url does not match BW patterns, remove it: " + linkDatumEntry + " for HostTypeKey: " + key.toString());
                            }
                        }
                    }
                }

            }
        }

        public void configure(JobConf arg0) {
        }

        public void close() throws IOException {
        }

    }

    /**
     * Collects only {@link Linkdatum} entries that have two values attached.
     * This means that both key url as well as linked url have passed the BW
     * patterns (see {@link BwLinkDatumReducer}).
     */
    public static class FilterLinkDatumReducer implements Mapper<HostTypeKey, ObjectWritable, Text, ObjectWritable>, Reducer<Text, ObjectWritable, Text, LinkDatum> {

        @Override
        public void map(HostTypeKey key, ObjectWritable value, OutputCollector<Text, ObjectWritable> out, Reporter rep) throws IOException {

            if (value.get() instanceof LinkDatumEntry) {
                out.collect(new Text(((LinkDatumEntry) value.get()).toString()), value);
            }
        }

        @Override
        public void reduce(Text key, Iterator<ObjectWritable> values, OutputCollector<Text, LinkDatum> out, Reporter report) throws IOException {

            int cnt = 0;
            ObjectWritable value = null;
            while (values.hasNext()) {
                value = values.next();
                cnt++;
            }
            if (cnt == 2) {
                out.collect(((LinkDatumEntry) value.get())._url, ((LinkDatumEntry) value.get())._linkDatum);
            }
        }

        public void configure(JobConf arg0) {
        }

        public void close() throws IOException {
        }

    }

    /**
     * Unwraps {@link LinkDatumValues} .
     */
    public static class UnwrapLinkDatumReducer implements Mapper<LinkDatumEntry, LinkDatumEntry, Text, LinkDatum> {

        @Override
        public void map(LinkDatumEntry key, LinkDatumEntry value, OutputCollector<Text, LinkDatum> out, Reporter rep) throws IOException {
            out.collect(key._url, key._linkDatum);
        }

        public void configure(JobConf arg0) {
        }

        public void close() throws IOException {
        }

    }

    public BWWebgraphFilter(Configuration conf, Path crawlDir) {
        super(conf);

    }

    public BWWebgraphFilter() {
    }

    public void update(Path webgraphPath, Path bwdb, boolean normalize, boolean filter, boolean replaceWebgraph) throws IOException {

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
        JobConf job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: wrapping OUTLINK objects from: " + webgraphPath);
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedOutlinkOutput = new Path(outputWebgraph, name);
        FileInputFormat.addInputPath(job, new Path(webgraphPath, WebGraph.OUTLINK_DIR));
        FileOutputFormat.setOutputPath(job, wrappedOutlinkOutput);
        job.setMapperClass(LinkDatumMapper.class);
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(LinkDatumEntry.class);
        job.setBoolean(LinkDatumMapper.URL_FILTERING, filter);
        job.setBoolean(LinkDatumMapper.URL_NORMALIZING, normalize);
        JobClient.runJob(job);

        // filter outlinks
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path filteredOutlinkDb = new Path(outputWebgraph, name);
        LOG.info("filter webgraph against bwdb: filter OUTLINK objects against bwdb.");
        job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: filter OUTLINK objects against bwdb." + wrappedOutlinkOutput + bwdb);
        job.setInputFormat(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(job, wrappedOutlinkOutput);
        FileInputFormat.addInputPath(job, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(job, filteredOutlinkDb);
        job.setMapperClass(ObjectWritableMapper.class);
        job.setMapOutputKeyClass(HostTypeKey.class);
        job.setMapOutputValueClass(ObjectWritable.class);
        job.setReducerClass(BwLinkDatumReducer.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(ObjectWritable.class);
        JobClient.runJob(job);
        FileSystem.get(job).delete(wrappedOutlinkOutput, true);

        // filter for valid entries, keep only entries where key and link url
        // have passed the BW patters
        Path outputOutlinkDb = new Path(outputWebgraph, WebGraph.OUTLINK_DIR);
        LOG.info("filter webgraph against bwdb: filter OUTLINK objects for valid entries.");
        job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: filter OUTLINK objects for valid entries." + filteredOutlinkDb + bwdb);
        job.setInputFormat(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(job, filteredOutlinkDb);
        FileOutputFormat.setOutputPath(job, outputOutlinkDb);
        job.setMapperClass(FilterLinkDatumReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(ObjectWritable.class);
        job.setReducerClass(FilterLinkDatumReducer.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LinkDatum.class);
        JobClient.runJob(job);
        FileSystem.get(job).delete(filteredOutlinkDb, true);

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

            update(new Path(args[0]), new Path(args[1]), Boolean.valueOf(args[2]), Boolean.valueOf(args[3]), Boolean.valueOf(args[4]));

            return 0;
        } catch (Exception e) {
            LOG.error("BWWebgraphFilter: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
