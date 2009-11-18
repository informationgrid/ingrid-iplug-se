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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.admin.NavigationSelector;
import org.apache.nutch.admin.NutchInstance;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.ByteArrayMultipartFileEditor;

@Controller
public class UrlUploadController extends NavigationSelector {

  @ModelAttribute("isBwEnabled")
  public Boolean isBwEnabled(HttpSession session) {
    return getBoolean(session, "bw.enable", false);
  }

  @ModelAttribute("isMetadataEnabled")
  public Boolean isMetadataEnabled(HttpSession session) {
    return getBoolean(session, "metadata.enable", false);
  }

  @ModelAttribute("startUrls")
  public File[] injectStartUrls(HttpSession session) {
    return getZipFiles(session, "url-uploads/start");
  }

  @ModelAttribute("limitUrls")
  public File[] injectLimitUrls(HttpSession session) {
    return getZipFiles(session, "url-uploads/limit");
  }

  @ModelAttribute("excludeUrls")
  public File[] injectExcludeUrls(HttpSession session) {
    return getZipFiles(session, "url-uploads/exclude");
  }

  @ModelAttribute("metadataUrls")
  public File[] injectMetadataUrls(HttpSession session) {
    return getZipFiles(session, "url-uploads/metadata");
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    binder.registerCustomEditor(byte[].class,
            new ByteArrayMultipartFileEditor());
  }

  @RequestMapping(value = "/index.html", method = RequestMethod.GET)
  public String urlUpload() {
    return "urlUpload";
  }

  @ModelAttribute("uploadBean")
  public UploadBean injectUploadBean() {
    return new UploadBean();
  }

  @RequestMapping(value = "/upload.html", method = RequestMethod.POST)
  public String upload(@ModelAttribute("uploadBean") UploadBean uploadBean,
          @RequestParam(value = "type", required = true) String type,
          HttpSession session) throws IOException {
    MultipartFile file = uploadBean.getFile();
    File out = getOutputFile(type, session);
    if (!out.exists()) {
      out.mkdirs();
    }

    // copy file
    FileOutputStream fileOutputStream = new FileOutputStream(new File(out, file
            .getOriginalFilename()), false);
    InputStream inputStream = file.getInputStream();
    byte[] buffer = new byte[1024];
    int read = -1;
    while ((read = inputStream.read(buffer, 0, 1024)) > -1) {
      fileOutputStream.write(buffer, 0, read);
    }
    fileOutputStream.flush();
    fileOutputStream.close();
    inputStream.close();

    return "redirect:/index.html";
  }

  @RequestMapping(value = "/deleteZip.html", method = RequestMethod.POST)
  public String deleteZipFile(@RequestParam("file") String file,
          @RequestParam("type") String type, HttpSession session) {
    File folder = getOutputFile(type, session);
    File zipFile = new File(folder, file);
    zipFile.delete();
    return "redirect:/index.html";
  }

  private File getOutputFile(String type, HttpSession session) {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
            .getAttribute("nutchInstance");
    File instanceFolder = nutchInstance.getInstanceFolder();
    File out = null;
    if ("start".equals(type)) {
      out = new File(instanceFolder, "url-uploads/start");
    } else if ("limit".equals(type)) {
      out = new File(instanceFolder, "url-uploads/limit");
    } else if ("exclude".equals(type)) {
      out = new File(instanceFolder, "url-uploads/exclude");
    } else if ("metadata".equals(type)) {
      out = new File(instanceFolder, "url-uploads/metadata");
    }
    return out;
  }

  private File[] getZipFiles(HttpSession session, String folder) {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
            .getAttribute("nutchInstance");
    File instanceFolder = nutchInstance.getInstanceFolder();
    File file = new File(instanceFolder, folder);
    File[] zipFiles = file.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        return pathname.getName().endsWith(".zip");
      }
    });
    return zipFiles;
  }

  private Boolean getBoolean(HttpSession session, String key,
          boolean defaultValue) {
    ServletContext servletContext = session.getServletContext();
    NutchInstance nutchInstance = (NutchInstance) servletContext
            .getAttribute("nutchInstance");
    Configuration configuration = nutchInstance.getConfiguration();
    return configuration.getBoolean(key, defaultValue);
  }

}
