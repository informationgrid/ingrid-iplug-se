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
package org.apache.nutch.admin.urlupload;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.crawl.IPreCrawl;

public class UrlExporter implements IPreCrawl {

  private Configuration _conf;

  private static final Log LOG = LogFactory.getLog(UrlExporter.class);

  @Override
  public void preCrawl(Path crawlDir) throws IOException {

    boolean enableBw = _conf.getBoolean("bw.enable", false);
    boolean enableMetadata = _conf.getBoolean("metadata.enable", false);
    FileSystem fileSystem = FileSystem.get(_conf);

    // unzip and upload start urls
    File[] zipFiles = getZipFiles(_conf, "url-uploads/start");
    Path out = new Path(crawlDir, "urls/start");
    fileSystem.delete(out, true);
    copyToHdfs(out, zipFiles);

    if (enableBw) {
      // unzip and upload limit urls
      zipFiles = getZipFiles(_conf, "url-uploads/limit");
      out = new Path(crawlDir, "urls/limit");
      fileSystem.delete(out, true);
      copyToHdfs(out, zipFiles);

      // unzip and upload exclude urls
      zipFiles = getZipFiles(_conf, "url-uploads/exclude");
      out = new Path(crawlDir, "urls/exclude");
      fileSystem.delete(out, true);
      copyToHdfs(out, zipFiles);
    }

    if (enableMetadata) {
      // unzip and upload metadata urls
      zipFiles = getZipFiles(_conf, "url-uploads/metadata");
      out = new Path(crawlDir, "urls/metadata");
      fileSystem.delete(out, true);
      copyToHdfs(out, zipFiles);
    }

  }

  private void copyToHdfs(Path out, File[] zipFiles) throws IOException {
    FileSystem fileSystem = FileSystem.get(_conf);
    String tmpDir = System.getProperty("java.io.tmpdir");
    File zipOut = new File(tmpDir, "url-export-" + System.currentTimeMillis());
    for (File file : zipFiles) {
      // unzip zipfile
      LOG.info("unzip file [" + file + "] to [" + zipOut + "]");
      FileUtil.unZip(file, zipOut);
      // list content of extracted zip file
      File[] urlFiles = zipOut.listFiles();
      for (File urlFile : urlFiles) {
        // copy zip content into hdfs
        Path src = new Path(urlFile.getAbsolutePath());
        LOG.info("upload src [" + src + "] to [" + out + "]");
        fileSystem.copyFromLocalFile(true, src, out);
      }
    }
  }

  @Override
  public Configuration getConf() {
    return _conf;
  }

  @Override
  public void setConf(Configuration conf) {
    _conf = conf;
  }

  private File[] getZipFiles(Configuration configuration, String folder) {
    String folderString = configuration.get("nutch.instance.folder");
    File instanceFolder = new File(folderString);
    File file = new File(instanceFolder, folder);
    File[] zipFiles = file.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".zip");
      }
    });
    return zipFiles == null ? new File[] {} : zipFiles;
  }

}
