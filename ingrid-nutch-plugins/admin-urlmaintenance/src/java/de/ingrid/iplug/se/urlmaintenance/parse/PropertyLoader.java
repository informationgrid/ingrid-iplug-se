package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

public class PropertyLoader {

  public static Properties load(final String path) throws IOException {
    return load(path, false);
  }

  public static Properties load(final String path, final boolean switched) throws IOException {
    final Properties props = new Properties();
    props.load(PropertyLoader.class.getResourceAsStream(path));
    if (switched) {
      final Enumeration<Object> keys = props.keys();
      while (keys.hasMoreElements()) {
        final Object key = keys.nextElement();
        final Object value = props.remove(key);
        props.put(value, key);
      }
    }
    return props;
  }
}
