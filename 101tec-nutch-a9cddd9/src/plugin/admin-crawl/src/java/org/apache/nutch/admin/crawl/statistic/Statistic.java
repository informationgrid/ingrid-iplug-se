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
package org.apache.nutch.admin.crawl.statistic;

public class Statistic {

  private String _host;
  private Long _overallCount;
  private Long _fetchSuccessCount;

  public Statistic(String host, Long overallCount, Long fetchSuccessCount) {
    _host = host;
    _overallCount = overallCount;
    _fetchSuccessCount = fetchSuccessCount;
  }

  public String getHost() {
    return _host;
  }

  public void setHost(String host) {
    _host = host;
  }

  public Long getOverallCount() {
    return _overallCount;
  }

  public void setOverallCount(Long crawldbCount) {
    _overallCount = crawldbCount;
  }

  public Long getFetchSuccessCount() {
    return _fetchSuccessCount;
  }

  public void setFetchSuccessCount(Long segmentCount) {
    _fetchSuccessCount = segmentCount;
  }

}
