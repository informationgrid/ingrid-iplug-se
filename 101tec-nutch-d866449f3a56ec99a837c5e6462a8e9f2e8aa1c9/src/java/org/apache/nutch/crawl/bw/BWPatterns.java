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

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;

/**
 * Black and white url prefix patterns container.
 */
public class BWPatterns implements Writable {

  public Text[] _positive;

  public Text[] _negative;

  public BWPatterns(Text[] positivePatterns, Text[] negativePatterns) {
    _positive = positivePatterns;
    _negative = negativePatterns;
  }

  public BWPatterns() {
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(_positive.length);
    for (int i = 0; i < _positive.length; i++) {
      _positive[i].write(out);
    }
    out.writeInt(_negative.length);
    for (int i = 0; i < _negative.length; i++) {
      _negative[i].write(out);
    }
  }

  public void readFields(DataInput in) throws IOException {
    int count = in.readInt();
    _positive = new Text[count];
    for (int i = 0; i < _positive.length; i++) {
      _positive[i] = new Text();
      _positive[i].readFields(in);
    }
    count = in.readInt();
    _negative = new Text[count];
    for (int i = 0; i < _negative.length; i++) {
      _negative[i] = new Text();
      _negative[i].readFields(in);
    }
  }

}
