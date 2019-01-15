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

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

import de.ingrid.iplug.se.nutch.crawl.bw.BWUpdateDb.Entry;

/**
 * Holds a host and a type used as key for {@link BWPatterns} and {@link Entry}
 */
public class HostTypeKey implements WritableComparable<HostTypeKey> {

  public static final long PATTERN_TYPE = 1;

  public static final long CRAWL_DATUM_TYPE = 2;

  public static final long INLINKS_TYPE = 3;

  public static final long INLINK_TYPE = 4;

  public static final long GENERIC_DATUM_TYPE = 5;

  public static final long LINKED_URL_TYPE = 6;

  private Text _host = new Text();

  private LongWritable _type = new LongWritable();

  public HostTypeKey() {
  }

  public HostTypeKey(String host, long type) {
    _host.set(host);
    _type.set(type);
  }

  public void write(DataOutput out) throws IOException {
    _host.write(out);
    _type.write(out);
  }

  public void readFields(DataInput in) throws IOException {
    _host.readFields(in);
    _type.readFields(in);
  }

  public int compareTo(HostTypeKey that) {
    int i = _host.compareTo(that._host);
    if (i == 0) {
      // in case host is identically make sure that PATTERN_TYPE is passing a
      // reducer first
      i = ((_type.get() < that._type.get() ? -1 : (_type.get() == that._type
          .get()) ? 0 : 1));
    }
    return i;
  }

  public int hashCode() {
    return _host.hashCode() + _type.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    HostTypeKey that = (HostTypeKey) obj;
    return _host.equals(that._host) && _type.equals(that._type);
  }
  
  public long getType() {
      return _type.get();
  }

  public String toString() {
    return _host.toString() + " (" + _type.toString() + ")";
  }
}
