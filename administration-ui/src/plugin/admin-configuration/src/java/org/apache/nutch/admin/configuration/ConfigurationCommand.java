package org.apache.nutch.admin.configuration;

public class ConfigurationCommand {

  private String _name;

  private String _value;

  private String _description;

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

}
