package de.ingrid.iplug.util;

import org.springframework.stereotype.Component;

@Component
public class TimeProvider {

  public long getTime() {
    return System.currentTimeMillis();
  }
}
