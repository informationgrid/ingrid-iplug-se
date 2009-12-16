package de.ingrid.iplug.se.urlmaintenance.util;

import org.springframework.stereotype.Component;

@Component
public class TimeProvider {

  public long getTime() {
    return System.currentTimeMillis();
  }
}
