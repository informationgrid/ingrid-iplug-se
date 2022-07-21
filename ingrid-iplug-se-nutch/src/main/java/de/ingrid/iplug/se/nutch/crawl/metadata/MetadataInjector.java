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

package de.ingrid.iplug.se.nutch.crawl.metadata;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ingrid.utils.tool.UrlTool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.MapFileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.crawl.CrawlDb;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.util.LockUtil;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.util.NutchJob;

public class MetadataInjector extends Configured implements Tool {

    private static final Log LOG = LogFactory.getLog(MetadataInjector.class);

    public static class MetadataContainer implements Writable {

        private final Set<Metadata> _metadatas = new HashSet<>();

        public MetadataContainer() {
        }

        public MetadataContainer(Metadata... metadatas) {
            Collections.addAll(_metadatas, metadatas);
        }

        public void addMetadata(Metadata metadata) {
            _metadatas.add(metadata);
        }

        public Set<Metadata> getMetadatas() {
            return _metadatas;
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            _metadatas.clear();
            int size = in.readInt();
            for (int i = 0; i < size; i++) {
                Metadata metadata = new Metadata();
                metadata.readFields(in);
                _metadatas.add(metadata);
            }
        }

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeInt(_metadatas.size());
            for (Metadata metadata : _metadatas) {
                metadata.write(out);
            }
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (Metadata metadata : _metadatas) {
                s.append("\r\n");
                s.append(metadata);
            }
            return s.toString();
        }

        @Override
        public int hashCode() {
            return _metadatas.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MetadataContainer) {
                MetadataContainer container = (MetadataContainer) obj;
                return _metadatas.equals(container._metadatas);
            } else {
                throw new IllegalArgumentException("Object is no instance of MetadataContainer.");
            }
        }

    }

    public MetadataInjector() {
    }

    /**
     * Creates {@link HostType} - {@link MetadataContainer} tuples from each
     * text line.
     */
    public static class MetadataInjectMapper extends Mapper<WritableComparable<Object>, Text, HostType, MetadataContainer> {

        private URLNormalizers _urlNormalizers;

        @Override
        protected void setup(Mapper<WritableComparable<Object>, Text, HostType, MetadataContainer>.Context context) throws IOException, InterruptedException {
            super.setup(context);
            Configuration conf = context.getConfiguration();
            _urlNormalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_INJECT);
        }

        @Override
        public void map(WritableComparable<Object> key, Text val, Mapper<WritableComparable<Object>, Text, HostType, MetadataContainer>.Context context) throws IOException, InterruptedException {

            String line = val.toString();
            String[] splits = line.split("\t");
            String url = splits[0];
            String meteKey = null;
            Metadata metadata = new Metadata();
            metadata.add("pattern", url);
            for (int i = 1; i < splits.length; i++) {
                String split = splits[i];
                if (split.endsWith(":")) {
                    meteKey = split.substring(0, split.length() - 1);
                    continue;
                }
                metadata.add(meteKey, split);
            }

            if (url != null) {
                String hostStr = null;
                try {
                    hostStr = new URL(url).getHost();
                } catch (Exception e) {
                    LOG.warn("unable to get host from: " + url + " : " + e);
                    return;
                }

                if (hostStr != null) {
                    HostType hostType = new HostType(new Text(hostStr), HostType.METADATA_CONTAINER);
                    MetadataContainer metadataContainer = new MetadataContainer(metadata);
                    context.write(hostType, metadataContainer);
                }
            }
        }

    }

    /**
     * Reduces {@link HostType} - {@link MetadataContainer} tuples
     */
    public static class MetadataInjectReducer extends Reducer<HostType, MetadataContainer, HostType, MetadataContainer> {

        @Override
        public void reduce(HostType key, Iterable<MetadataContainer> values, Reducer<HostType, MetadataContainer, HostType, MetadataContainer>.Context context) throws IOException, InterruptedException {
            MetadataContainer metadataContainer = new MetadataContainer();
            for (MetadataContainer next: values) {
                Set<Metadata> nextMetadatas = next.getMetadatas();
                for (Metadata nextMetadata : nextMetadatas) {
                    metadataContainer.addMetadata(nextMetadata);
                }
            };
            context.write(key, metadataContainer);
        }
    }

    public void inject(Path metadataDb, Path urlDir) throws IOException, InterruptedException, ClassNotFoundException {

        LOG.info("MetadataInjector: starting");
        LOG.info("MetadataInjector: metadataDb: " + metadataDb);
        LOG.info("MetadataInjector: urlDir: " + urlDir);

        Configuration conf = getConf();
        FileSystem fs = FileSystem.get(conf);

        String name = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        name = "/metadata-inject-temp-" + name;
        Path tempDir = new Path(getConf().get("mapred.temp.dir", ".") + name);

        Job sortJob = NutchJob.getInstance(conf);
        sortJob.setJobName("metadata-inject " + urlDir);
        FileInputFormat.addInputPath(sortJob, urlDir);
        sortJob.setMapperClass(MetadataInjectMapper.class);

        FileOutputFormat.setOutputPath(sortJob, tempDir);
        sortJob.setOutputFormatClass(SequenceFileOutputFormat.class);
        sortJob.setOutputKeyClass(HostType.class);
        sortJob.setOutputValueClass(MetadataContainer.class);
        try {
            boolean success = sortJob.waitForCompletion(true);
            if (!success) {
                String message = "Job did not succeed, job status:"
                        + sortJob.getStatus().getState() + ", reason: "
                        + sortJob.getStatus().getFailureInfo();
                LOG.error(message);
                throw new RuntimeException(message);
            }
        } catch (IOException | InterruptedException | ClassNotFoundException e) {
            LOG.error("Job failed: {}", e);
            fs.delete(tempDir, true);
            throw e;
        }

        LOG.info("MetadataInjector: Merging injected urls into metadataDb.");
        String newDbName = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        Path newDb = new Path(metadataDb, newDbName);

        Job job = NutchJob.getInstance(conf);
        job.setJobName("merge metadata " + metadataDb);

        Path current = new Path(metadataDb, "current");
        if (fs.exists(current)) {
            FileInputFormat.addInputPath(job, current);
        }
        FileInputFormat.addInputPath(job, tempDir);
        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setReducerClass(MetadataInjectReducer.class);
        FileOutputFormat.setOutputPath(job, newDb);
        job.setOutputFormatClass(MapFileOutputFormat.class);
        job.setOutputKeyClass(HostType.class);
        job.setOutputValueClass(MetadataContainer.class);
        Path lock = CrawlDb.lock(conf, current, false);
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
            fs.delete(newDb, true);
            throw e;
        } finally {
            LockUtil.removeLockFile(fs, lock);
            fs.delete(tempDir, true);
        }

        LOG.info("rename metadataDb");
        Path old = new Path(metadataDb, "old");
        fs.delete(old, true);
        if (fs.exists(current)) {
            fs.rename(current, old);
        }
        fs.rename(newDb, current);
        fs.delete(old, true);

        LOG.info("MetadataInjector: done");
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new MetadataInjector(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: MetadataInjector <metadataDb> <urls>");
            return -1;
        }
        try {

            Path metadatadb = new Path(args[0]);
            LocalFileSystem localFS = FileSystem.getLocal(getConf());
            if (localFS.exists(metadatadb)) {
                localFS.delete(metadatadb, true);
            }

            inject(new Path(args[0]), new Path(args[1]));

            return 0;
        } catch (Exception e) {
            LOG.error("MetadataInjector: " + StringUtils.stringifyException(e));
            return -1;
        }
    }
}
