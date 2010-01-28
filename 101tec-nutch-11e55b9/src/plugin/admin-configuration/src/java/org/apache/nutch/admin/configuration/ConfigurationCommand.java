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
package org.apache.nutch.admin.configuration;

public class ConfigurationCommand {

  private String _name;

  private String _value;

  private String _description;

  private int _position;

  private String _finalValue;

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public String getValue() {
    return _value;
  }

  public void setValue(String value) {
    _value = value;
  }

  public String getDescription() {
    return _description;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setPosition(int position) {
    _position = position;
  }

  public int getPosition() {
    return _position;
  }

  public String getFinalValue() {
    return _finalValue;
  }

  public void setFinalValue(String finalValue) {
    _finalValue = finalValue;
  }

  @Override
  public int hashCode() {
    return _name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    ConfigurationCommand command = (ConfigurationCommand) obj;
    return _name.equals(command._name);
  }
}
