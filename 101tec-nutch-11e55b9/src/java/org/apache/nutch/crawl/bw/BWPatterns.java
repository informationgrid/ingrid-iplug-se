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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * Black and white url prefix patterns container.
 */
public class BWPatterns implements Writable {

  private static final Log LOG = LogFactory.getLog(BWPatterns.class);

  private List<Text> _positive;
  private List<Text> _negative;

  private List<Pattern> _posPattern = new ArrayList<Pattern>();
  private List<Pattern> _negPattern = new ArrayList<Pattern>();

  public BWPatterns(Text[] positivePatterns, Text[] negativePatterns) {
    _positive = new ArrayList<Text>(Arrays.asList(positivePatterns));
    _negative = new ArrayList<Text>(Arrays.asList(negativePatterns));
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
      patterns.add(Pattern.compile(text.toString().toLowerCase()));
    }
  }

  public BWPatterns() {
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
    _positive = new ArrayList<Text>(count);
    for (int i = 0; i < count; i++) {
      Text text = new Text();
      text.readFields(in);
      _positive.add(text);
    }
    syncPosPattern();
    count = in.readInt();
    _negative = new ArrayList<Text>(count);
    for (int i = 0; i < count; i++) {
      Text text = new Text();
      text.readFields(in);
      _negative.add(text);
    }
    syncNegPattern();
  }

  public boolean willPassBlackList(String url) {
    if (_negative == null || _negative.size() == 0) {
      // there is no negative list, so every url will be accepted
      return true;
    }

    String lowerCaseUrl = url.toLowerCase();

    for (Pattern pattern : _negPattern) {
      Matcher matcher = pattern.matcher(lowerCaseUrl);
      if (matcher.find()) {
        return false;
      }
    }
    return true;
  }

  public boolean willPassWhiteList(String url) {
    if (_positive == null || _positive.size() == 0) {
      // there is no positive list, so every url will be accepted
      return true;
    }

    String lowerCaseUrl = url.toLowerCase();

    for (Pattern pattern : _posPattern) {
      Matcher matcher = pattern.matcher(lowerCaseUrl);
      if (matcher.find()) {
        return true;
      }
    }
    return false;
  }

  public boolean willPassBWLists(String url) {
    boolean ret = (willPassWhiteList(url) && willPassBlackList(url));
    LOG.debug("**********Url '" + url + "' will " + (ret ? "pass" : "NOT pass")
            + ".");
    return ret;
  }
}
