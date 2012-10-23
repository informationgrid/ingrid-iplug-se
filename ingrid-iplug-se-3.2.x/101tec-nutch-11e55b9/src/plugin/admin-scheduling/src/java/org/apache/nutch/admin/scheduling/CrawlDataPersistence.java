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
package org.apache.nutch.admin.scheduling;

import java.io.IOException;

public class CrawlDataPersistence extends Persistence<CrawlData> {

  public void saveCrawlData(Integer depth, Integer topn) throws IOException {
    CrawlData crawlData = new CrawlData();
    crawlData.setDepth(depth);
    crawlData.setTopn(topn);
    crawlData.setWorkingDirectory(_workingDirectory);
    makePersistent(crawlData);
  }

  public CrawlData loadCrawlData() throws IOException, ClassNotFoundException {
    return load(CrawlData.class);
  }

  public boolean existsCrawlData() {
    return exists(CrawlData.class);
  }

  public void deleteCrawlData() throws IOException {
    makeTransient(CrawlData.class);
  }

}
