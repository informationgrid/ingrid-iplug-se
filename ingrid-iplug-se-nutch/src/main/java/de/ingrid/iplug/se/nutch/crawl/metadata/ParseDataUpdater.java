/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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

package de.ingrid.iplug.se.nutch.crawl.metadata;

import java.io.IOException;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.util.NutchConfiguration;

public class ParseDataUpdater extends Configured implements Tool {

    public static final Log LOG = LogFactory.getLog(ParseDataUpdater.class);

    public ParseDataUpdater(Configuration configuration) {
        super(configuration);
    }

    public ParseDataUpdater() {
    }

    public void update(Path metadataDb, Path segment) throws IOException {
        LOG.info("metadata update: starting");
        LOG.info("metadata update: db: " + metadataDb);
        LOG.info("metadata update: segment: " + segment);

        // tmp dir for all jobs
        Path tempDir = new Path(getConf().get("mapred.temp.dir", System.getProperty("java.io.tmpdir")));
        String id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));
        LOG.info("write tmp files into: " + tempDir);
        LOG.info("metadata update: wrap parsedata: " + segment);

        String name = "metadata-wrap-parsedata-temp-" + id;
        Path wrappedParseData = new Path(tempDir, name);
        ParseDataWrapper parseDataWrapper = new ParseDataWrapper(getConf());
        parseDataWrapper.wrap(segment, wrappedParseData);

        LOG.info("metadata update: merge metadatadb and wrapped parse_data: " + segment);
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

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(NutchConfiguration.create(), new ParseDataUpdater(), args);
        System.exit(res);
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: ParseDataUpdater <metadatadb> <segment>");
            return -1;
        }
        try {
            update(new Path(args[0]), new Path(args[1]));
            return 0;
        } catch (Exception e) {
            LOG.error("ParseDataUpdater: " + StringUtils.stringifyException(e));
            return -1;
        }
    }
}
