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
    return _next != null || _parser.hasNext() ;
  }

  @Override
  public Tuple next() {
    Tuple tuple = loadNext();
    _next = null;
    if (tuple != null && hasGroupKey(tuple)) {
      tuple = Tuple.create(_key, tuple.getValue(_key)).addChild(tuple.remove(_key));

      while (_parser.hasNext()) {
        final Tuple next = _parser.next();
        if (hasGroupKey(next)) {
          _next = next;
          break;
        }
        tuple = tuple.addChild(next.remove(_key));
      }
    }
    return tuple;
  }

  private Tuple loadNext() {
      System.out.println("GroupParser.loadNext()");
    if (_next == null && _parser.hasNext()) {
      _next = _parser.next();
    }
    return _next;
  }

  private boolean hasGroupKey(final Tuple tuple) {
    final String value = tuple.getValue(_key);
    return value != null && !value.equals("");
  }
}
