package de.ingrid.iplug.se.urlmaintenance.parse;

import java.util.HashMap;
import java.util.Map;

public class MapParser extends AbstractTupleParser {

  private final String _key;

  private final Map<Integer, String> _fields = new HashMap<Integer, String>();

  public MapParser(final ITupleParser parser, final String key, final String... fields) {
    super(parser);
    // key of field of information
    _key = key;
    // defined keys
    if (fields != null) {
      for (int i = 0; i < fields.length; i++) {
        if (fields[i] != null) {
          _fields.put(i, fields[i]);
        }
      }
    }
  }

  @Override
  public Tuple next() {
    final Tuple tuple = _parser.next();
    // splits source field
    final String[] splits = TokenSplitter.split(tuple.getValue(_key));
    // add for every key a field
    if (tuple.containsKey(_key)) {
      for (final Integer i : _fields.keySet()) {
        if (i < splits.length && splits[i] != null && splits[i].trim().length() > 0) {
          tuple.add(_fields.get(i), splits[i]);
        }
      }
    }
    return tuple;
  }
}