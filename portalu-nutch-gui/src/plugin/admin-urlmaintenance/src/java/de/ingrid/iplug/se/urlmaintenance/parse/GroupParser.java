package de.ingrid.iplug.se.urlmaintenance.parse;



public class GroupParser extends AbstractTupleParser {

  private final String _key;

  private Tuple _next;

  public GroupParser(final ITupleParser parser, final String key) {
    super(parser);
    _key = key;
  }

  @Override
  public boolean hasNext() {
    return _next != null || _parser.hasNext();
  }

  @Override
  public Tuple next() {
    Tuple tuple = loadNext();
    if (tuple != null && hasGroupKey(tuple)) {
      tuple = Tuple.create(_key, tuple.getValue(_key)).addChild(tuple.remove(_key));

      while (_parser.hasNext()) {
        _next = _parser.next();
        if (hasGroupKey(_next)) {
          break;
        }
        tuple = tuple.addChild(_next.remove(_key));
        _next = null;
      }
    }
    return tuple;
  }

  private Tuple loadNext() {
    if (_next != null) {
      return _next;
    } else if (_parser.hasNext()) {
      _next = _parser.next();
    }
    return _next;
  }

  private boolean hasGroupKey(final Tuple tuple) {
    final String value = tuple.getValue(_key);
    return value != null && !value.equals("");
  }
}
