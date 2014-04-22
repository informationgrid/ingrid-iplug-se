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
package org.apache.nutch.admin.crawl;

import org.apache.hadoop.fs.Path;

public class CrawlPath {

  private Path _path;

  private long _size;

  private boolean _searchable;

  private boolean _running;

  public boolean isRunning() {
    return _running;
  }

  public void setRunning(boolean running) {
    _running = running;
  }

  public boolean isSearchable() {
    return _searchable;
  }

  public void setSearchable(boolean searchable) {
    _searchable = searchable;
  }

  public Path getPath() {
    return _path;
  }

  public void setPath(Path path) {
    _path = path;
  }

  public long getSize() {
    return _size;
  }

  public void setSize(long len) {
    _size = len;
  }

}
