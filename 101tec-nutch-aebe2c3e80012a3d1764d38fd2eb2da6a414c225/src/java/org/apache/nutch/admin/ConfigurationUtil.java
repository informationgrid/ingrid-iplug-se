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
package org.apache.nutch.admin;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ConfigurationUtil {

  private final File _workingDirectory;

  public ConfigurationUtil(File workingDirectory) {
    _workingDirectory = workingDirectory;
  }

  public boolean existsConfiguration(String folderName) {
    File folder = new File(_workingDirectory, folderName);
    return folder.isDirectory() && folder.exists();
  }

  public Configuration createNewConfiguration(String folderName)
          throws IOException {
    File folder = new File(_workingDirectory, folderName);
    if (existsConfiguration(folderName)) {
      throw new IllegalArgumentException("configuration already exists: "
              + folder.getAbsolutePath());
    }
    File conf = new File(folder, "conf");
    conf.mkdirs();
    copyConfigurationFiles(conf);
    addInstanceFolderNutchSite(folderName);
    return loadConfiguration(folderName);
  }

  public Configuration loadConfiguration(String folderName) throws IOException {
    Configuration configuration = new Configuration();
    configure(configuration, folderName);
    return configuration;
  }

  public List<Configuration> loadAll() throws IOException {
    List<Configuration> list = new ArrayList<Configuration>();
    String[] allNames = getAllNames();
    for (String folderName : allNames) {
      Configuration configuration = loadConfiguration(folderName);
      list.add(configuration);
    }
    return list;
  }

  public String[] getAllNames() {
    List<String> list = new ArrayList<String>();
    File[] files = _workingDirectory.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        File conf = new File(pathname, "conf");
        return conf.exists() && conf.isDirectory() ? true : false;
      }
    });
    if (files != null) {
      for (File file : files) {
        list.add(file.getName());
      }
    }
    return list.toArray(new String[list.size()]);
  }

  private void copyConfigurationFiles(File target)
          throws FileNotFoundException, IOException {
    InputStream in = ConfigurationUtil.class
            .getResourceAsStream("/nutch-default.xml");
    if (in == null) {
      throw new FileNotFoundException("nutch-default.xml is not in classpath");
    }

    OutputStream out = new FileOutputStream(new File(target,
            "nutch-default.xml"));
    copy(in, out);
    in = ConfigurationUtil.class.getResourceAsStream("/nutch-site.xml");
    if (in == null) {
      throw new FileNotFoundException("nutch-site.xml is not in classpath");
    }
    out = new FileOutputStream(new File(target, "nutch-site.xml"));
    copy(in, out);
  }

  private void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    out.flush();
    in.close();
    out.close();
  }

  private void configure(Configuration configuration, String folderName)
          throws IOException {
    File folder = new File(_workingDirectory, folderName);
    File conf = new File(folder, "conf");
    if (conf.exists()) {
      // nutch default
      File nutchDefault = new File(conf, "nutch-default.xml");
      if (nutchDefault.exists()) {
        configuration.addResource(new Path(nutchDefault.getCanonicalPath()));
      }
      // nutch site
      File nutchSite = new File(conf, "nutch-site.xml");
      if (nutchSite.exists()) {
        configuration.addResource(new Path(nutchSite.getCanonicalPath()));
      }
    }
  }

  private void addInstanceFolderNutchSite(String folderName) throws IOException {
    try {
      File folder = new File(_workingDirectory, folderName);
      File nutchSiteXml = new File(folder, "conf/nutch-site.xml");
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      Document document = factory.newDocumentBuilder().parse(nutchSiteXml);
      Element node = document.getDocumentElement();
      Element property = document.createElement("property");
      Element name = document.createElement("name");
      name.appendChild(document.createTextNode("nutch.instance.folder"));
      Element value = document.createElement("value");
      value.appendChild(document.createTextNode(folder.getCanonicalPath()));
      Element description = document.createElement("description");
      description.appendChild(document.createTextNode("instance folder"));
      Node propertyNode = node.appendChild(property);
      propertyNode.appendChild(name);
      propertyNode.appendChild(value);
      propertyNode.appendChild(description);

      FileOutputStream outputStream = new FileOutputStream(nutchSiteXml);
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(outputStream);
      TransformerFactory transFactory = TransformerFactory.newInstance();
      Transformer transformer = transFactory.newTransformer();
      transformer.setOutputProperty("indent", "yes");
      transformer.transform(source, result);
      outputStream.close();
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

}
