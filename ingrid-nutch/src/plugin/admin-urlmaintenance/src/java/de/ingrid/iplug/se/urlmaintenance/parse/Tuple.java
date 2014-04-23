package de.ingrid.iplug.se.urlmaintenance.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tuple {

  private final Map<String, String> _map = new HashMap<String, String>();

  private final List<Tuple> _childs = new ArrayList<Tuple>();

  public Tuple() {
  }

  public static Tuple create(final String key, final String value) {
    final Tuple tuple = new Tuple();
    tuple.add(key, value);
    return tuple;
  }

  public Tuple add(final String key, final String value) {
    _map.put(key, value);
    return this;
  }

  public Tuple remove(final String key) {
    _map.remove(key);
    return this;
  }

  public boolean containsKey(final String key) {
    return _map.containsKey(key);
  }

  public Set<String> getKeys() {
    return _map.keySet();
  }

  public String getValue(final String key) {
    return _map.get(key);
  }

  public Tuple addChild(final Tuple tuple) {
    _childs.add(tuple);
    return this;
  }

  public List<Tuple> getChilds() {
    return _childs;
  }
}
