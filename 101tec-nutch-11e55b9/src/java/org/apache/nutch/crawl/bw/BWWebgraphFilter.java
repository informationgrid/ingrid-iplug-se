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

package org.apache.nutch.crawl.bw;

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
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.CrawlTool;
import org.apache.nutch.crawl.Inlink;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.scoring.webgraph.LinkDatum;
import org.apache.nutch.scoring.webgraph.Node;
import org.apache.nutch.scoring.webgraph.WebGraph;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.SyncUtil;

/**
 * Webgraph filter tool that filters urls in inlinks, nodes, outlinks that do not pass a white-black list.
 * 
 * @see BWInjector
 */
public class BWWebgraphFilter extends Configured {

    public static final Log LOG = LogFactory.getLog(BWWebgraphFilter.class);

    public static final String CURRENT_NAME = "current";

    public static class ObjectWritableMapper implements Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, Writable value, OutputCollector<HostTypeKey, ObjectWritable> collector,
                Reporter reporter) throws IOException {
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
            return _url.toString() + "\n " + _linkDatum.toString();
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
     * Wraps an url and an {@link LinkDatum} into an {@link LinkDatumEntry} wrapper
     * and associates it with a {@link HostTypeKey}. Standard Url-Filtering and
     * normalization can be applied optional.
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
        public void map(Text key, LinkDatum value, OutputCollector<HostTypeKey, LinkDatumEntry> out, Reporter rep)
                throws IOException {

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
        }

        public void configure(JobConf job) {
            urlFiltering = job.getBoolean(URL_FILTERING, false);
            urlNormalizers = job.getBoolean(URL_NORMALIZING, false);
            if (urlFiltering) {
                filters = new URLFilters(job);
            }
            if (urlNormalizers) {
                scope = job.get(URL_NORMALIZING_SCOPE, URLNormalizers.SCOPE_BWDB);
                normalizers = new URLNormalizers(job, scope);
            }
        }

        public void close() throws IOException {
        }

    }    
    
    
    /**
     * Wraps an url and an {@link Node} into an {@link NodeEntry} wrapper
     * and associates it with a {@link HostTypeKey}. Standard Url-Filtering and
     * normalization can be applied optional.
     */
    public static class NodeMapper implements Mapper<Text, Node, HostTypeKey, NodeEntry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, Node value, OutputCollector<HostTypeKey, NodeEntry> out, Reporter rep)
                throws IOException {

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
                NodeEntry nodeEntry = new NodeEntry(key, value);
                out.collect(new HostTypeKey(host, HostTypeKey.GENERIC_DATUM_TYPE), nodeEntry);
            }
        }

        public void configure(JobConf job) {
            urlFiltering = job.getBoolean(URL_FILTERING, false);
            urlNormalizers = job.getBoolean(URL_NORMALIZING, false);
            if (urlFiltering) {
                filters = new URLFilters(job);
            }
            if (urlNormalizers) {
                scope = job.get(URL_NORMALIZING_SCOPE, URLNormalizers.SCOPE_BWDB);
                normalizers = new URLNormalizers(job, scope);
            }
        }

        public void close() throws IOException {
        }

    }      
    
    
    /**
     * Collects only entries that match a white list entry and no black list
     * entry. Here the filtering of the {@link LinkDatumEntry} takes place. 
     * Base of the filtering is the BW DB.
     */
    public static class BwLinkDatumReducer implements Reducer<HostTypeKey, ObjectWritable, Text, LinkDatum> {

        private BWPatterns _patterns;

        public void reduce(HostTypeKey key, Iterator<ObjectWritable> values,
                OutputCollector<Text, LinkDatum> out, Reporter report) throws IOException {

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
                    return;
                }

                if (value instanceof LinkDatumEntry) {
                    LinkDatumEntry linkDatumEntry = (LinkDatumEntry) value;
                    String url = linkDatumEntry._url.toString();
                    if (_patterns.willPassBWLists(url)) {
                        // url is outside the black list and matches the white
                        // list
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns passed for url: " + url
                                    + " for HostTypeKey: " + key.toString());
                        }
                        out.collect(linkDatumEntry._url, linkDatumEntry._linkDatum);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns NOT passed for url: " + url
                                    + " for HostTypeKey: " + key.toString());
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
     * Collects only entries that match a white list entry and no black list
     * entry. Here the filtering of the {@link NodeEntry} takes place. 
     * Base of the filtering is the BW DB.
     */
    public static class BwNodeReducer implements Reducer<HostTypeKey, ObjectWritable, Text, Node> {

        private BWPatterns _patterns;

        public void reduce(HostTypeKey key, Iterator<ObjectWritable> values,
                OutputCollector<Text, Node> out, Reporter report) throws IOException {

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
                    return;
                }

                if (value instanceof NodeEntry) {
                    NodeEntry nodeEntry = (NodeEntry) value;
                    String url = nodeEntry._url.toString();
                    if (_patterns.willPassBWLists(url)) {
                        // url is outside the black list and matches the white
                        // list
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns passed for url: " + url
                                    + " for HostTypeKey: " + key.toString());
                        }
                        out.collect(nodeEntry._url, nodeEntry._node);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns NOT passed for url: " + url
                                    + " for HostTypeKey: " + key.toString());
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
    
    public BWWebgraphFilter(Configuration conf) {
        super(conf);
    }

    public void update(Path webgraphPath, Path bwdb, boolean normalize, boolean filter,
            boolean replaceWebgraph) throws IOException {

        String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path outputWebgraph = new Path(webgraphPath, name);
        
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
        LOG.info("filter webgraph against bwdb: filter OUTLINK objects...");
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
        SyncUtil.syncJobRun(job);// JobClient.runJob(job);
        
        // filter outlinks
        Path outputOutlinkDb = new Path(outputWebgraph, WebGraph.OUTLINK_DIR);
        LOG.info("filter webgraph against bwdb: filter OUTLINK objects against bwdb.");
        job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: filter OUTLINK objects against bwdb." + wrappedOutlinkOutput + bwdb);
        job.setInputFormat(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(job, wrappedOutlinkOutput);
        FileInputFormat.addInputPath(job, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(job, outputOutlinkDb);
        job.setMapperClass(ObjectWritableMapper.class);
        job.setReducerClass(BwLinkDatumReducer.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LinkDatum.class);
        SyncUtil.syncJobRun(job);// JobClient.runJob(filterJob);
        FileSystem.get(job).delete(wrappedOutlinkOutput, true);
        
        
        // wrap inlinks
        LOG.info("filter webgraph against bwdb: filter INLINK objects...");
        job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: wrapping INLINK objects from: " + webgraphPath);
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedInlinkOutput = new Path(outputWebgraph, name);
        FileInputFormat.addInputPath(job, new Path(webgraphPath, WebGraph.INLINK_DIR));
        FileOutputFormat.setOutputPath(job, wrappedInlinkOutput);
        job.setMapperClass(LinkDatumMapper.class);
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(LinkDatumEntry.class);
        job.setBoolean(LinkDatumMapper.URL_FILTERING, filter);
        job.setBoolean(LinkDatumMapper.URL_NORMALIZING, normalize);
        SyncUtil.syncJobRun(job);// JobClient.runJob(job);
        
        // filter inlinks
        Path outputInlinkDb = new Path(outputWebgraph, WebGraph.INLINK_DIR);
        LOG.info("filter webgraph against bwdb: filter INLINK objects against bwdb.");
        job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: filter INLINK objects against bwdb." + wrappedInlinkOutput + bwdb);
        job.setInputFormat(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(job, wrappedInlinkOutput);
        FileInputFormat.addInputPath(job, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(job, outputInlinkDb);
        job.setMapperClass(ObjectWritableMapper.class);
        job.setReducerClass(BwLinkDatumReducer.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LinkDatum.class);
        SyncUtil.syncJobRun(job);// JobClient.runJob(filterJob);        
        FileSystem.get(job).delete(wrappedInlinkOutput, true);
        
        // wrap nodes
        LOG.info("filter webgraph against bwdb: filter NODE objects...");
        job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: wrapping NODE objects from: " + webgraphPath);
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedNodeOutput = new Path(outputWebgraph, name);
        FileInputFormat.addInputPath(job, new Path(webgraphPath, WebGraph.NODE_DIR));
        FileOutputFormat.setOutputPath(job, wrappedNodeOutput);
        job.setMapperClass(NodeMapper.class);
        job.setInputFormat(SequenceFileInputFormat.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(NodeEntry.class);
        job.setBoolean(NodeMapper.URL_FILTERING, filter);
        job.setBoolean(NodeMapper.URL_NORMALIZING, normalize);
        SyncUtil.syncJobRun(job);// JobClient.runJob(job);
        
        // filter nodes
        Path outputNodeDb = new Path(outputWebgraph, WebGraph.NODE_DIR);
        LOG.info("filter webgraph against bwdb: filter NODE objects against bwdb.");
        job = new NutchJob(getConf());
        job.setJobName("filter webgraph against bwdb: filter NODE objects against bwdb." + wrappedNodeOutput + bwdb);
        job.setInputFormat(SequenceFileInputFormat.class);
        FileInputFormat.addInputPath(job, wrappedNodeOutput);
        FileInputFormat.addInputPath(job, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(job, outputNodeDb);
        job.setMapperClass(ObjectWritableMapper.class);
        job.setReducerClass(BwNodeReducer.class);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Node.class);
        SyncUtil.syncJobRun(job);// JobClient.runJob(filterJob);         
        FileSystem.get(job).delete(wrappedNodeOutput, true);


        if (replaceWebgraph) {
            LOG.info("filter webgraph against bwdb: replace current webgraph");
            Path old = new Path(conf.get("mapred.temp.dir", ".")
                    + "webgraph_old");
            if (fs.exists(webgraphPath)) {
              if (fs.exists(old)) fs.delete(old, true);
              fs.rename(webgraphPath, old);
            }
            fs.mkdirs(webgraphPath);
            fs.rename(outputNodeDb, new Path(webgraphPath, WebGraph.NODE_DIR));
            fs.rename(outputOutlinkDb, new Path(webgraphPath, WebGraph.OUTLINK_DIR));
            fs.rename(outputInlinkDb, new Path(webgraphPath, WebGraph.INLINK_DIR));
            if (fs.exists(old)) fs.delete(old, true);
        }
        LOG.info("ilter webgraph against bwdb: finished.");

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = NutchConfiguration.create();
        BWWebgraphFilter bwDb = new BWWebgraphFilter(conf);
        if (args.length != 5) {
            System.err
                    .println("Usage: BWWebgraphFilter <webgraphdb> <bwdb> <normalize> <filter> <replace current webgraph>");
            return;
        }
        bwDb.update(new Path(args[1]), new Path(args[2]), Boolean.valueOf(args[3]), Boolean
                .valueOf(args[4]), Boolean.valueOf(args[5]));

    }

}