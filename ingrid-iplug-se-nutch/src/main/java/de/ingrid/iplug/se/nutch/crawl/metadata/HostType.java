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

package de.ingrid.iplug.se.nutch.crawl.metadata;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparable;

public class HostType implements WritableComparable<HostType> {

  public static final int METADATA_CONTAINER = 1;

  public static final int URL_PARSEDATA_CONTAINER = 2;

  private final Text _host = new Text();

  private final IntWritable _type = new IntWritable();

  public HostType() {
  }

  public HostType(Text host, int type) {
    _host.set(host);
    _type.set(type);
  }

  @Override
  public void readFields(DataInput in) throws IOException {
    _host.readFields(in);
    _type.readFields(in);
  }

  @Override
  public void write(DataOutput out) throws IOException {
    _host.write(out);
    _type.write(out);
  }

  @Override
  public int compareTo(HostType that) {
    int i = _host.compareTo(that._host);
    if (i == 0) {
      // in case host is identically make sure that METADATA_CONTAINER is
      // passing a
      // reducer first
      i = ((Integer.compare(_type.get(), that._type.get())));
    }
    return i;
  }

  public Text getHost() {
    return _host;
  }

  @Override
  public int hashCode() {
    return _host.hashCode() + _type.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof HostType) {
      HostType other = (HostType) obj;
      return _host.equals(other._host) && _type.equals(other._type);
    } else {
      throw new IllegalArgumentException("Object not of type HostType!");
    }
  }

  @Override
  public String toString() {
    return _host + " (" + _type + ")";
  }

}
