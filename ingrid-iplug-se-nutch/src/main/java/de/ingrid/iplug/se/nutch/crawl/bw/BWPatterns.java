/*
 * **************************************************-
 * ingrid-iplug-se-nutch
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Black and white url prefix patterns container.
 */
public class BWPatterns implements Writable {

    private static final Logger LOG = LoggerFactory.getLogger(BWPatterns.class);

    private List<Text> _positive;

    private List<Text> _negative;

    private final List<Pattern> _posPattern = new ArrayList<>();

    private final List<Pattern> _negPattern = new ArrayList<>();

    public BWPatterns() {
    }

    public BWPatterns(Text[] positivePatterns, Text[] negativePatterns) {
        _positive = new ArrayList<>(Arrays.asList(positivePatterns));
        _negative = new ArrayList<>(Arrays.asList(negativePatterns));
        syncPosPattern();
        syncNegPattern();
    }

    private void syncPosPattern() {
        syncPattern(_positive, _posPattern);
    }

    private void syncNegPattern() {
        syncPattern(_negative, _negPattern);
    }

    private void syncPattern(List<Text> regExpressions, List<Pattern> patterns) {
        patterns.clear();
        for (Text text : regExpressions) {
            patterns.add(Pattern.compile(text.toString()));
        }
    }

    public List<Text> getPositive() {
        return _positive;
    }

    public void setPositive(List<Text> positive) {
        _positive = positive;
        syncPosPattern();
    }

    public List<Text> getNegative() {
        return _negative;
    }

    public void setNegative(List<Text> negative) {
        _negative = negative;
        syncNegPattern();
    }

    public void write(DataOutput out) throws IOException {
        out.writeInt(_positive.size());
        for (Text text : _positive) {
            text.write(out);
        }
        out.writeInt(_negative.size());
        for (Text text : _negative) {
            text.write(out);
        }
    }

    public void readFields(DataInput in) throws IOException {
        int count = in.readInt();
        _positive = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Text text = new Text();
            text.readFields(in);
            _positive.add(text);
        }
        syncPosPattern();
        count = in.readInt();
        _negative = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            Text text = new Text();
            text.readFields(in);
            _negative.add(text);
        }
        syncNegPattern();
    }

    public boolean willPassBlackList(String url) {
        if (_negPattern.size() == 0) {
            // there is no negative list, so every url will be accepted
            return true;
        }

        for (Pattern pattern : _negPattern) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if <code>url</code> passes any positive
     * patterns.
     * 
     * A positive pattern MUST exist! Otherwise all BW entries with only
     * negative patterns will result in crawling outside the BW url space. (As a
     * result it is not possible to crawl the whole web, excluding urls defined
     * by negative bw patterns ;-).)
     * 
     * 
     * @param url An url.
     * @return True or False
     */
    public boolean willPassWhiteList(String url) {
        if (_posPattern.size() == 0) {
            // there is no positive list, so every url will be accepted
            if (LOG.isWarnEnabled()) {
                // this should not be possible, see below
                LOG.warn("No positive list available for WHITE LIST");
                dumpPatternsToLog();
            }
            return false;
        }

        for (Pattern pattern : _posPattern) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> if <code>url</code> passes any positive
     * patterns AND does NOT pass any negative patterns AND positive
     * pattern has been defined.
     * 
     * @param url An URL.
     * @return True or False
     */
    public boolean willPassBWLists(String url) {
        boolean ret = (willPassWhiteList(url) && willPassBlackList(url));
        if (LOG.isDebugEnabled()) {
            LOG.debug("**********Url '" + url + "' will " + (ret ? "pass" : "NOT pass") + ".");
        }
        return ret;
    }

    private void dumpPatternsToLog() {
        if (_posPattern.size() > 0) {
            for (Pattern pattern : _posPattern) {
                LOG.info("BWPatterns: positive pattern: " + pattern.pattern());
            }
        }
        if (_negPattern.size() > 0) {
            for (Pattern pattern : _negPattern) {
                LOG.info("BWPatterns: negative pattern: " + pattern.pattern());
            }
        }
        if (_positive != null && _positive.size() > 0) {
            for (Text text : _positive) {
                LOG.info("BWPatterns: positive pattern Strings: " + text.toString());
            }
        }
        if (_negative != null && _negative.size() > 0) {
            for (Text text : _negative) {
                LOG.info("BWPatterns: negative pattern Strings: " + text.toString());
            }
        }
    }

}
