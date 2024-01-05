/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2024 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.apache.nutch.crawl.Inlink;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.crawl.LinkDb;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

import de.ingrid.iplug.se.nutch.net.InGridURLNormalizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LinkDb filter tool that filters urls that do not pass a white-black list. The
 * tool not only filters the key urls but also the inlinks for a valid url.
 * 
 * @see BWInjector
 */
public class BWLinkDbFilter extends Configured implements Tool {

    public static final Logger LOG = LoggerFactory.getLogger(BWLinkDbFilter.class);

    public static final String CURRENT_NAME = "current";

    public static class ObjectWritableMapper extends Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, Writable value, Mapper<HostTypeKey, Writable, HostTypeKey, ObjectWritable>.Context context) throws IOException, InterruptedException {
            ObjectWritable objectWritable = new ObjectWritable(value);
            context.write(key, objectWritable);
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
            return _url.toString() + " : " + _inlink.toString();
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
    public static class InlinkMapper extends Mapper<Text, Inlinks, HostTypeKey, InlinkEntry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, Inlinks value, Mapper<Text, Inlinks, HostTypeKey, InlinkEntry>.Context context) throws IOException, InterruptedException {

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
                String fromUrl;
                while (it.hasNext()) {
                    Inlink inlink = it.next();
                    fromUrl = inlink.getFromUrl();
                    String host = new URL(fromUrl).getHost();
                    InlinkEntry linkEntry = new InlinkEntry(key, inlink);
                    context.write(new HostTypeKey(host, HostTypeKey.INLINK_TYPE), linkEntry);
                }
            }
        }

        @Override
        protected void setup(Mapper<Text, Inlinks, HostTypeKey, InlinkEntry>.Context context) throws IOException, InterruptedException {
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
     * Wraps an url and an {@link Inlinks} into an {@link InlinksEntry} wrapper
     * and associates it with a {@link HostTypeKey}. Standard Url-Filtering and
     * normalization can be applied optional.
     */
    public static class InlinksMapper extends Mapper<Text, Inlinks, HostTypeKey, InlinksEntry> {

        public static final String URL_FILTERING = "bwupdatedb.url.filters";

        public static final String URL_NORMALIZING = "bwupdatedb.url.normalizers";

        public static final String URL_NORMALIZING_SCOPE = "bwupdatedb.url.normalizers.scope";

        private boolean urlFiltering;

        private boolean urlNormalizers;

        private URLFilters filters;

        private URLNormalizers normalizers;

        private String scope;

        @Override
        public void map(Text key, Inlinks value, Mapper<Text, Inlinks, HostTypeKey, InlinksEntry>.Context context) throws IOException, InterruptedException {

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
                context.write(new HostTypeKey(host, HostTypeKey.INLINKS_TYPE), linkEntry);
            }
        }

        @Override
        protected void setup(Mapper<Text, Inlinks, HostTypeKey, InlinksEntry>.Context context) throws IOException, InterruptedException {
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
     * entry. Here the filtering of the {@link InlinksEntry} and the
     * {@link InlinkEntry} entries takes place. Base of the filtering is the BW
     * DB.
     */
    public static class BwReducer extends Reducer<HostTypeKey, ObjectWritable, HostTypeKey, ObjectWritable> {

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
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Key url or linked url does not have a valid BW patterns, remove it: " + value + " for HostTypeKey: " + key.toString());
                    }
                    // return, because no bw pattern has been set for this url
                    return;
                }

                if (value instanceof InlinksEntry) {
                    String url = (((InlinksEntry) value)._url).toString();
                    if (_patterns.willPassBWLists(url)) {
                        // url is outside the black list and matches the white
                        // list
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns passed for url: " + (((InlinksEntry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                        }
                        context.write(key, objectWritable);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("InlinksEntry does not pass BW patterns, remove it: " + (((InlinksEntry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                        }
                    }
                } else if (value instanceof InlinkEntry) {
                    String url = (((InlinkEntry) value)._inlink.getFromUrl());
                    if (_patterns.willPassBWLists(url)) {
                        // url is outside the black list and matches the white
                        // list
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("BW patterns passed for url: " + (((InlinkEntry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                        }
                        context.write(key, objectWritable);
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("InlinkEntry does not pass BW patterns, remove it: " + (((InlinkEntry) value)._url).toString() + " for HostTypeKey: " + key.toString());
                        }
                    }
                }

            }
        }
    }

    /**
     * This converter transforms the {@link InlinkEntry} and
     * {@link InlinksEntry} objects that are sorted by the host names back to a
     * collection of {@link Inlinks} objects that are associated with an url.
     *
     * This process transforms the {@link HostTypeKey} based data back into a
     * link db structure.
     */
    public static class LinkDbFormatConverterMapper extends Mapper<HostTypeKey, ObjectWritable, Text, ObjectWritable> {

        @Override
        public void map(HostTypeKey key, ObjectWritable value, Mapper<HostTypeKey, ObjectWritable, Text, ObjectWritable>.Context context) throws IOException, InterruptedException {

            Object entry = value.get();

            if (entry instanceof InlinksEntry) {
                InlinksEntry inlinksEntry = (InlinksEntry) entry;
                context.write(inlinksEntry._url, value);
            } else if (entry instanceof InlinkEntry) {
                InlinkEntry inlinkEntry = (InlinkEntry) entry;
                context.write(inlinkEntry._url, value);
            } else {
                LOG.error("Error mapping, unknown type for  " + entry);
            }
        }

    }

    /**
     * This converter transforms the {@link InlinkEntry} and
     * {@link InlinksEntry} objects that are sorted by the host names back to a
     * collection of {@link Inlinks} objects that are associated with an url.
     * 
     * This process transforms the {@link HostTypeKey} based data back into a
     * link db structure.
     */
    public static class LinkDbFormatConverterReducer extends Reducer<Text, ObjectWritable, Text, Inlinks> {

        @Override
        public void reduce(Text key, Iterable<ObjectWritable> values, Reducer<Text, ObjectWritable, Text, Inlinks>.Context context) throws IOException, InterruptedException {
            Inlinks _inlinks = null;
            List<Inlink> inLinkEntries = new ArrayList<>();
            Object entry = null;

            while (values.iterator().hasNext()) {
                entry = values.iterator().next().get(); // unwrap

                if (entry instanceof InlinksEntry) {
                    _inlinks = ((InlinksEntry) entry)._inlinks;
                    _inlinks.clear();
                } else if (entry instanceof InlinkEntry) {
                    inLinkEntries.add(((InlinkEntry) entry)._inlink);
                }
            }
            if (_inlinks != null && inLinkEntries.size() > 0 && entry != null) {
                for (Inlink in : inLinkEntries) {
                    _inlinks.add(in);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Collect " + key + " : " + _inlinks);
                }
                context.write(key, _inlinks);
            }
        }

    }

    public BWLinkDbFilter() {
    }

    public void update(Path linkDb, Path bwdb, boolean normalize, boolean filter, boolean replaceLinkDb) throws IOException, InterruptedException, ClassNotFoundException {

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

        Job job = NutchJob.getInstance(getConf());
        Configuration jobConf = job.getConfiguration();
        job.setJobName("filter linkdb against bwdb: wrap INLINKS objects from: " + linkDb);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        Path current = new Path(linkDb, CURRENT_NAME);
        if (fs.exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }

        job.setMapperClass(InlinksMapper.class);

        FileOutputFormat.setOutputPath(job, wrappedInlinksDbOutput);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(InlinksEntry.class);
        jobConf.setBoolean(InlinksMapper.URL_FILTERING, filter);
        jobConf.setBoolean(InlinksMapper.URL_NORMALIZING, normalize);
        Path lock = CrawlDb.lock(getConf(), current, false);
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

        // wrapping inlink objects
        LOG.info("filter linkdb against bwdb: wrapping INLINK(!) objects started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path wrappedInlinkDbOutput = new Path(linkDb, name);

        job = NutchJob.getInstance(getConf());
        jobConf = job.getConfiguration();
        job.setJobName("filter linkdb against bwdb: wrap INLINK(!) objects from: " + linkDb);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        current = new Path(linkDb, CURRENT_NAME);
        if (fs.exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }

        job.setMapperClass(InlinkMapper.class);

        FileOutputFormat.setOutputPath(job, wrappedInlinkDbOutput);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostTypeKey.class);
        job.setOutputValueClass(InlinkEntry.class);
        jobConf.setBoolean(InlinkMapper.URL_FILTERING, filter);
        jobConf.setBoolean(InlinkMapper.URL_NORMALIZING, normalize);
        lock = CrawlDb.lock(getConf(), current, false);
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

        // filtering
        LOG.info("filter linkdb against bwdb: filtering started.");
        name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path tmpMergedDb = new Path(linkDb, name);

        Job filterJob = NutchJob.getInstance(getConf());

        filterJob.setJobName("filtering: " + wrappedInlinksDbOutput + wrappedInlinkDbOutput + bwdb);
        filterJob.setInputFormatClass(SequenceFileInputFormat.class);

        FileInputFormat.addInputPath(filterJob, wrappedInlinksDbOutput);
        FileInputFormat.addInputPath(filterJob, wrappedInlinkDbOutput);
        FileInputFormat.addInputPath(filterJob, new Path(bwdb, "current"));
        FileOutputFormat.setOutputPath(filterJob, tmpMergedDb);
        filterJob.setMapperClass(ObjectWritableMapper.class);
        filterJob.setReducerClass(BwReducer.class);
        filterJob.setOutputFormatClass(MapFileOutputFormat.class);
        filterJob.setOutputKeyClass(HostTypeKey.class);
        filterJob.setOutputValueClass(ObjectWritable.class);

        Path wrappedInlinksDbOutputLock = CrawlDb.lock(getConf(), wrappedInlinksDbOutput, false);
        Path wrappedInlinkDbOutputLock = CrawlDb.lock(getConf(), wrappedInlinkDbOutput, false);
        try {
            boolean success = filterJob.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + filterJob.getStatus().getState() + ", reason: "
                        + filterJob.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            throw e;
        } finally {
            NutchJob.cleanupAfterFailure(wrappedInlinksDbOutput, wrappedInlinksDbOutputLock, fs);
            NutchJob.cleanupAfterFailure(wrappedInlinkDbOutput, wrappedInlinkDbOutputLock, fs);
        }

        // convert formats
        LOG.info("filter linkdb against bwdb: converting started.");

        Job convertJob = NutchJob.getInstance(getConf());

        convertJob.setJobName("format converting: " + tmpMergedDb);
        FileInputFormat.addInputPath(convertJob, tmpMergedDb);
        convertJob.setInputFormatClass(SequenceFileInputFormat.class);
        convertJob.setMapOutputKeyClass(Text.class);
        convertJob.setMapOutputValueClass(ObjectWritable.class);
        convertJob.setMapperClass(LinkDbFormatConverterMapper.class);
        convertJob.setReducerClass(LinkDbFormatConverterReducer.class);
        FileOutputFormat.setOutputPath(convertJob, outputLinkDb);
        convertJob.setOutputFormatClass(MapFileOutputFormat.class);
        convertJob.setOutputKeyClass(Text.class);
        convertJob.setOutputValueClass(Inlinks.class);
        lock = CrawlDb.lock(getConf(), tmpMergedDb, false);
        try {
            boolean success = convertJob.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + convertJob.getStatus().getState() + ", reason: "
                        + convertJob.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            throw e;
        } finally {
            NutchJob.cleanupAfterFailure(tmpMergedDb, lock, fs);
        }

        if (replaceLinkDb) {
            LOG.info("filter linkdb against bwdb: replace current linkdb");
            LinkDb.install(convertJob, linkDb);
        }
        LOG.info("filter linkdb against bwdb: finished.");

    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new BWLinkDbFilter(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: BWLinkDbFilter <linkdb> <bwdb> <normalize> <filter> <replace current linkdb>");
            return -1;
        }
        try {

            update(new Path(args[0]), new Path(args[1]), Boolean.parseBoolean(args[2]), Boolean.parseBoolean(args[3]), Boolean.parseBoolean(args[4]));

            return 0;
        } catch (Exception e) {
            LOG.error("BWLinkDbFilter: " + StringUtils.stringifyException(e));
            return -1;
        }
    }

}
