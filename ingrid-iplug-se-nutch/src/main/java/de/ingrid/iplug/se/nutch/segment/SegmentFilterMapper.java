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
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.nutch.metadata.MetaWrapper;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;

import java.io.IOException;

/**
 * This tool takes several segments and filters them against the crawlDB. It can
 * be used to reduce the segments data if the crawlDB has been filtered.
 * 
 * @author Joachim Müller
 */
public class SegmentFilterMapper extends Mapper<Text, MetaWrapper, Text, MetaWrapper> {

    private static final Log LOG = LogFactory.getLog(SegmentFilterMapper.class);

    private URLFilters filters = null;
    private URLNormalizers normalizers = null;

    @Override
    protected void setup(Mapper<Text, MetaWrapper, Text, MetaWrapper>.Context context) throws IOException, InterruptedException {
        super.setup(context);
        Configuration conf = context.getConfiguration();
        if (conf == null)
            return;
        if (conf.getBoolean("segment.filter.filter", false)) {
            filters = new URLFilters(conf);
        }
        if (conf.getBoolean("segment.filter.normalizer", false))
            normalizers = new URLNormalizers(conf, URLNormalizers.SCOPE_DEFAULT);

    }

    private Text newKey = new Text();

    @Override
    public void map(Text key, MetaWrapper value, Mapper<Text, MetaWrapper, Text, MetaWrapper>.Context context) throws IOException, InterruptedException {
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
            context.write(newKey, value);
        }
    }

}
