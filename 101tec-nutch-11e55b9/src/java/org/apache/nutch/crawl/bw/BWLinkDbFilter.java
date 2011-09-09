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
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapFileOutputFormat;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.crawl.Inlink;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.SyncUtil;

/**
 * LinkDb filter tool that filters urls that do not pass a white-black list. The
 * tool not only filters the key urls but also the inlinks for a valid url.
 * 
 * @see BWInjector
 */
public class BWLinkDbFilter extends Configured {

    public static final Log LOG = LogFactory.getLog(BWLinkDbFilter.class);

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
     * Wraps an Inlink object and stores also the url the Inlink refers to.
     * 
     * @see Inlink
     * 
     */
    public static class InlinkEntry implements Writable {

        private Text _url;

        private Inlink _inlink;

        public InlinkEntry() {
        }

        public InlinkEntry(Text url, Inlink inlink) {
            _url = url;
            _inlink = inlink;
        }

        public void write(DataOutput out) throws IOException {
            _url.write(out);
            _inlink.write(out);
        }

        public void readFields(DataInput in) throws IOException {
            _url = new Text();
            _url.readFields(in);
            _inlink = new Inlink();
            _inlink.readFields(in);
        }

        public String toString() {
            return _url.toString() + "\n " + _inlink.toString();
        }

    }

    /**
     * Wraps an Inlinks object and stores also the url the Inlinks refers to.
     * 
     * @see Inlinks
     */
    public static class InlinksEntry implements Writable {

        private Text _url;

        private Inlinks _inlinks;

        public InlinksEntry() {
        }

        public InlinksEntry(Text url, Inlinks inlinks) {
            _url = url;
            _inlinks = inlinks;
        }

        public void write(DataOutput out) throws IOException {
            _url.write(out);
            _inlinks.write(out);
        }

        public void readFields(DataInput in) throws IOException {
            _url = new Text();
            _url.readFields(in);
            _inlinks = new Inlinks();
            _inlinks.readFields(in);
        }

        public String toString() {
            return _url.toString() + "\n " + _inlinks.toString();
        }

    }

    /**
     * Wraps an url and an {@link Inlink} into an {@link InlinkEntry} wrapper
     * and associates it with a {@link HostTypeKey}. Standard Url-Filtering and
     * normalization can be applied optional.
     */
    public static class InlinkMapper implements Mapper<Text, Inlinks, HostTypeKey, InlinkEntry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, Inlinks value, OutputCollector<HostTypeKey, InlinkEntry> out, Reporter rep)
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
                Iterator<Inlink> it = value.iterator();
                String fromUrl = null;
                while (it.hasNext()) {
                    Inlink inlink = it.next();
                    fromUrl = inlink.getFromUrl();
                    String host = new URL(fromUrl).getHost();
                    InlinkEntry linkEntry = new InlinkEntry(key, inlink);
                    out.collect(new HostTypeKey(host, HostTypeKey.INLINK_TYPE), linkEntry);
                }
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
     * Wraps an url and an {@link Inlinks} into an {@link InlinksEntry} wrapper
     * and associates it with a {@link HostTypeKey}. Standard Url-Filtering and
     * normalization can be applied optional.
     */
    public static class InlinksMapper implements Mapper<Text, Inlinks, HostTypeKey, InlinksEntry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, Inlinks value, OutputCollector<HostTypeKey, InlinksEntry> out, Reporter rep)
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
                InlinksEntry linkEntry = new InlinksEntry(key, value);
                out.collect(new HostTypeKey(host, HostTypeKey.INLINKS_TYPE), linkEntry);
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
     * entry. Here the filtering of the {@link InlinksEntry} and the
     * {@link InlinkEntry} entries takes place. Base of the filtering is the BW
     * DB.
     */
    public static class BwReducer implements Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable> {

        private BWPatterns _patterns;

        public void reduce(HostTypeKey key, Iterator<ObjectWritable> values,
                OutputCollector<HostTypeKey, ObjectWritable> out, Reporter report) throws IOException {

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

                if (value instanceof InlinksEntry) {
                    String url = (((InlinksEntry) value)._url).toString();
                    if (_patterns.willPassBWLists(url)) {
                        // url is outside the black list and matches the white
                        // list
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns passed for url: " + (((InlinksEntry) value)._url).toString()
                                    + " for HostTypeKey: " + key.toString());
                        }
                        out.collect(key, objectWritable);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns NOT passed for url: " + (((InlinksEntry) value)._url).toString()
                                    + " for HostTypeKey: " + key.toString());
                        }
                    }
                } else if (value instanceof InlinkEntry) {
                    String url = (((InlinkEntry) value)._inlink.getFromUrl()).toString();
                    if (_patterns.willPassBWLists(url)) {
                        // url is outside the black list and matches the white
                        // list
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns passed for url: " + (((InlinkEntry) value)._url).toString()
                                    + " for HostTypeKey: " + key.toString());
                        }
                        out.collect(key, objectWritable);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns NOT passed for url: " + (((InlinkEntry) value)._url).toString()
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
     * This converter transforms the {@link InlinkEntry} and
     * {@link InlinksEntry} objects that are sorted by the host names back to a
     * collection of {@link Inlinks} objects that are associated with a url.
     * 
     * This process transforms the {@link HostTypeKey} based data back into a
     * link db structure.
     */
    public static class LinkDbFormatConverter implements Reducer<Text, ObjectWritable, Text, Inlinks>,
            Mapper<HostTypeKey, ObjectWritable, Text, ObjectWritable> {

        public void configure(JobConf job) {
        }

        public void close() throws IOException {
        }

        @Override
        public void map(HostTypeKey key, ObjectWritable value, OutputCollector<Text, ObjectWritable> out, Reporter rep)
                throws IOException {

            Object entry = value.get();

            if (entry instanceof InlinksEntry) {
                InlinksEntry inlinksEntry = (InlinksEntry) entry;
                out.collect(inlinksEntry._url, value);
            } else if (entry instanceof InlinkEntry) {
                InlinkEntry inlinkEntry = (InlinkEntry) entry;
                out.collect(inlinkEntry._url, value);
            }
        }

        @Override
        public void reduce(Text key, Iterator<ObjectWritable> values, OutputCollector<Text, Inlinks> out, Reporter rep)
                throws IOException {
            Inlinks _inlinks = null;
            Object entry = null;
            while (values.hasNext()) {
                entry = values.next().get(); // unwrap

                if (entry instanceof InlinksEntry) {
                    _inlinks = ((InlinksEntry) entry)._inlinks;
                    _inlinks.clear();
                    continue;
                }

                if (_inlinks == null) {
                    // return, because no inlinks object exists for this url
                    return;
                }
                Inlink inlink = ((InlinkEntry) entry)._inlink;
                _inlinks.add(inlink);
            }
            if (_inlinks != null && _inlinks.size() > 0 && entry != null) {
                out.collect(((InlinkEntry) entry)._url, _inlinks);
            }
        }

    }

    public BWLinkDbFilter(Configuration conf) {
        super(conf);
    }

    public void update(Path linkDb, Path bwdb, boolean normalize, boolean filter,
            boolean replaceLinkDb) throws IOException {

        String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path outputLinkDb = new Path(linkDb, name);
        
        LOG.info("filter linkdb against bwdb: starting");
        LOG.info("filter linkdb against bwdb: output linkdb " + outputLinkDb);
        LOG.info("filter linkdb against bwdb: input linkdb " + linkDb);
        LOG.info("filter linkdb against bwdb: bwdb: " + bwdb);
        LOG.info("filter linkdb against bwdb: normalize urls: " + normalize);
        LOG.info("filter linkdb against bwdb: filter urls: " + filter);
        LOG.info("filter linkdb against bwdb: replace linkdb: " + replaceLinkDb);

        Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);
        
        // return if crawldb does not exist
        if (!fs.exists(linkDb)) {
            LOG.info("filter linkDb against bwdb: linkDb does not exist, nothing todo.");
            return;
        }

        // wrapping inlinks objects
        LOG.info("filter linkdb against bwdb: wrapping INLINKS objects started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedInlinksDbOutput = new Path(linkDb, name);

        JobConf job = new NutchJob(getConf());
        job.setJobName("filter linkdb against bwdb: wrap INLINKS objects from: " + linkDb);

        job.setInputFormat(SequenceFileInputFormat.class);

        Path current = new Path(linkDb, CURRENT_NAME);
        if (FileSystem.get(job).exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }

        job.setMapperClass(InlinksMapper.class);

        FileOutputFormat.setOutputPath(job, wrappedInlinksDbOutput);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(InlinksEntry.class);
        job.setBoolean(InlinksMapper.URL_FILTERING, filter);
        job.setBoolean(InlinksMapper.URL_NORMALIZING, normalize);
        SyncUtil.syncJobRun(job);// JobClient.runJob(job);

        // wrapping inlink objects
        LOG.info("filter linkdb against bwdb: wrapping INLINK(!) objects started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedInlinkDbOutput = new Path(linkDb, name);

        job = new NutchJob(getConf());
        job.setJobName("filter linkdb against bwdb: wrap INLINK(!) objects from: " + linkDb);

        job.setInputFormat(SequenceFileInputFormat.class);

        current = new Path(linkDb, CURRENT_NAME);
        if (FileSystem.get(job).exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }

        job.setMapperClass(InlinkMapper.class);

        FileOutputFormat.setOutputPath(job, wrappedInlinkDbOutput);
        job.setOutputFormat(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(InlinkEntry.class);
        job.setBoolean(InlinkMapper.URL_FILTERING, filter);
        job.setBoolean(InlinkMapper.URL_NORMALIZING, normalize);
        SyncUtil.syncJobRun(job);// JobClient.runJob(job);

        // filtering
        LOG.info("filter linkdb against bwdb: filtering started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path tmpMergedDb = new Path(linkDb, name);
        JobConf filterJob = new NutchJob(getConf());
        filterJob.setJobName("filtering: " + wrappedInlinksDbOutput + wrappedInlinkDbOutput + bwdb);
        filterJob.setInputFormat(SequenceFileInputFormat.class);

        FileInputFormat.addInputPath(filterJob, wrappedInlinksDbOutput);
        FileInputFormat.addInputPath(filterJob, wrappedInlinkDbOutput);
        FileInputFormat.addInputPath(filterJob, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(filterJob, tmpMergedDb);
        filterJob.setMapperClass(ObjectWritableMapper.class);
        filterJob.setReducerClass(BwReducer.class);
        filterJob.setOutputFormat(MapFileOutputFormat.class);
        filterJob.setOutputKeyClass(HostTypeKey.class);
        filterJob.setOutputValueClass(ObjectWritable.class);
        SyncUtil.syncJobRun(filterJob);// JobClient.runJob(filterJob);

        // remove wrappedSegOutput
        FileSystem.get(job).delete(wrappedInlinksDbOutput, true);
        FileSystem.get(job).delete(wrappedInlinkDbOutput, true);

        // convert formats
        LOG.info("filter linkdb against bwdb: converting started.");
        JobConf convertJob = new NutchJob(getConf());
        convertJob.setJobName("format converting: " + tmpMergedDb);
        FileInputFormat.addInputPath(convertJob, tmpMergedDb);
        convertJob.setInputFormat(SequenceFileInputFormat.class);
        convertJob.setMapOutputKeyClass(Text.class);
        convertJob.setMapOutputValueClass(ObjectWritable.class);
        convertJob.setMapperClass(LinkDbFormatConverter.class);
        convertJob.setReducerClass(LinkDbFormatConverter.class);
        FileOutputFormat.setOutputPath(convertJob, outputLinkDb);
        convertJob.setOutputFormat(MapFileOutputFormat.class);
        convertJob.setOutputKeyClass(Text.class);
        convertJob.setOutputValueClass(Inlinks.class);
        SyncUtil.syncJobRun(convertJob);// JobClient.runJob(convertJob);

        // 
        FileSystem.get(job).delete(tmpMergedDb, true);

        if (replaceLinkDb) {
            LOG.info("filter linkdb against bwdb: replace current linkdb");
            LinkDb.install(convertJob, linkDb);
        }
        LOG.info("filter linkdb against bwdb: finished.");

    }

    public static void main(String[] args) throws Exception {
        Configuration conf = NutchConfiguration.create();
        BWLinkDbFilter bwDb = new BWLinkDbFilter(conf);
        if (args.length != 5) {
            System.err
                    .println("Usage: BWLinkDbFilter <linkdb> <bwdb> <normalize> <filter> <replace current linkdb>");
            return;
        }
        bwDb.update(new Path(args[1]), new Path(args[2]), Boolean.valueOf(args[3]), Boolean
                .valueOf(args[4]), Boolean.valueOf(args[5]));

    }

}
