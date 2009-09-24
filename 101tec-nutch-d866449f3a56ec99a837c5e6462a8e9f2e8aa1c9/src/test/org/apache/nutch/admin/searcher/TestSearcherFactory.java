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
package org.apache.nutch.admin.searcher;

import java.io.File;

import junit.framework.TestCase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileUtil;
import org.apache.nutch.util.NutchConfiguration;

public class TestSearcherFactory extends TestCase {

  private File _folder = new File(System.getProperty("java.io.tmpdir"),
          TestSearcherFactory.class.getName());

  @Override
  protected void setUp() throws Exception {
    assertTrue(_folder.mkdirs());
    assertTrue(new File(_folder, "general").mkdirs());
    // create test crawl folder
    assertTrue(new File(_folder, "testCrawl").mkdirs());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.00")
            .mkdirs());
    assertTrue(new File(_folder,
            "testCrawl/crawls/Crawl-2009.08.17_00.00.00/search.done")
            .createNewFile());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.01")
            .mkdirs());
    assertTrue(new File(_folder,
            "testCrawl/crawls/Crawl-2009.08.17_00.00.01/search.done")
            .createNewFile());
    assertTrue(new File(_folder, "testCrawl/crawls/Crawl-2009.08.17_00.00.02")
            .mkdirs());
    // create second test crawl folder
    assertTrue(new File(_folder, "testCrawl2").mkdirs());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.00")
            .mkdirs());
    assertTrue(new File(_folder,
            "testCrawl2/crawls/Crawl-2009.08.17_00.00.00/search.done")
            .createNewFile());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.01")
            .mkdirs());
    assertTrue(new File(_folder,
            "testCrawl2/crawls/Crawl-2009.08.17_00.00.01/search.done")
            .createNewFile());
    assertTrue(new File(_folder, "testCrawl2/crawls/Crawl-2009.08.17_00.00.02")
            .mkdirs());

  }

  @Override
  protected void tearDown() throws Exception {
    assertTrue(FileUtil.fullyDelete(_folder));
  }

  public void testInstanceCreate() throws Exception {
    Configuration configuration = NutchConfiguration.create();
    configuration.set("nutch.instance.folder", new File(_folder, "testCrawl")
            .getAbsolutePath());
    configuration.set("plugin.folders", "src/plugin");
    configuration
            .set(
                    "plugin.includes",
                    "protocol-http|urlfilter-regex|parse-(text|html|js)|index-(basic|anchor|metadata)|query-(basic|site|url)|response-(json|xml)|summary-basic|scoring-opic|urlnormalizer-(pass|regex|basic)");
    SearcherFactory instance = SearcherFactory.getInstance(configuration);
    MultipleSearcher searcher = instance.get();
    assertEquals(2, searcher.getNutchBeanLength());
  }

  public void testInstanceReloadCreate() throws Exception {
    Configuration configuration = NutchConfiguration.create();
    configuration.set("nutch.instance.folder", new File(_folder, "testCrawl")
            .getAbsolutePath());
    configuration.set("plugin.folders", "src/plugin");
    configuration
            .set(
                    "plugin.includes",
                    "protocol-http|urlfilter-regex|parse-(text|html|js)|index-(basic|anchor|metadata)|query-(basic|site|url)|response-(json|xml)|summary-basic|scoring-opic|urlnormalizer-(pass|regex|basic)");
    SearcherFactory instance = SearcherFactory.getInstance(configuration);
    MultipleSearcher searcher = instance.get();
    assertEquals(2, searcher.getNutchBeanLength());
    assertTrue(new File(_folder,
            "testCrawl/crawls/Crawl-2009.08.17_00.00.02/search.done")
            .createNewFile());
    searcher = instance.get();
    assertEquals(2, searcher.getNutchBeanLength());
    instance.reload();
    searcher = instance.get();
    assertEquals(3, searcher.getNutchBeanLength());
  }

  public void testGeneralReloadCreate() throws Exception {
    Configuration configuration = NutchConfiguration.create();
    configuration.set("nutch.instance.folder", new File(_folder, "general")
            .getAbsolutePath());
    configuration.set("plugin.folders", "src/plugin");
    configuration
            .set(
                    "plugin.includes",
                    "protocol-http|urlfilter-regex|parse-(text|html|js)|index-(basic|anchor|metadata)|query-(basic|site|url)|response-(json|xml)|summary-basic|scoring-opic|urlnormalizer-(pass|regex|basic)");
    SearcherFactory instance = SearcherFactory.getInstance(configuration);
    MultipleSearcher searcher = instance.get();
    assertEquals(4, searcher.getNutchBeanLength());
    assertTrue(new File(_folder,
            "testCrawl/crawls/Crawl-2009.08.17_00.00.02/search.done")
            .createNewFile());
    assertTrue(new File(_folder,
            "testCrawl2/crawls/Crawl-2009.08.17_00.00.02/search.done")
            .createNewFile());
    searcher = instance.get();
    assertEquals(4, searcher.getNutchBeanLength());
    instance.reload();
    searcher = instance.get();
    assertEquals(6, searcher.getNutchBeanLength());

  }
}
