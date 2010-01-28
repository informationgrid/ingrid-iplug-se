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

import org.apache.hadoop.conf.Configuration;

/**
 * Container for a set of information related to a configuration instance of
 * nutch
 */
public class NutchInstance {

  private File _instanceFolder;

  private Configuration _configuration;

  private String _instanceName;

  public NutchInstance(String name, File folder, Configuration instanceConf) {
    _instanceName = name;
    _instanceFolder = folder;
    _configuration = instanceConf;
  }

  /**
   * @return the name of the instance
   */
  public String getInstanceName() {
    return _instanceName;
  }

  /**
   * @return configuration of this instance
   */
  public Configuration getConfiguration() {
    return _configuration;
  }

  /**
   * @return the folder the instance life in
   */
  public File getInstanceFolder() {
    return _instanceFolder;
  }

}
