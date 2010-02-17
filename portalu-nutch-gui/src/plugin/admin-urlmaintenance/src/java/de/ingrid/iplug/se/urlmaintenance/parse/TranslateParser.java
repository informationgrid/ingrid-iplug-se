package de.ingrid.iplug.se.urlmaintenance.parse;

import java.util.Properties;

public class TranslateParser extends AbstractTupleParser {

  private final String _key;
  private final Properties _properties;

  public TranslateParser(final ITupleParser parser, final String key, final Properties translate) {
    super(parser);
    _key = key;
    _properties = translate;
  }

  @Override
  public Tuple next() {
    final Tuple next = _parser.next();
    replace(next);
    return next;
  }

  private void replace(final Tuple tuple) {
    final String value = tuple.getValue(_key);
    if (value != null) {
      final String[] splits = TokenSplitter.split(value);
      final StringBuilder builder = new StringBuilder();
      for (final String split : splits) {
        final String property = _properties.containsKey(split.trim()) ? _properties.getProperty(split.trim()) : split.trim();
        if (property != null) {
          if (builder.length() > 0) {
            builder.append(",");
          }
          builder.append(property);
        }
      }
      tuple.add(_key, builder.toString());
    }
    replaceChilds(tuple);
  }

  private void replaceChilds(final Tuple tuple) {
    for (final Tuple child : tuple.getChilds()) {
      replace(child);
    }
  }
}
