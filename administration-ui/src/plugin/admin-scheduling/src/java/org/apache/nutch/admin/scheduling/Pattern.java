package org.apache.nutch.admin.scheduling;

import java.io.Serializable;

public class Pattern implements Serializable {

  private String _pattern;

  public String getPattern() {
    return _pattern;
  }

  public void setPattern(String pattern) {
    _pattern = pattern;
  }

}
