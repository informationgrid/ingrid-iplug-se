package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.File;

public abstract class AbstractTupleParser implements ITupleParser {

  protected final ITupleParser _parser;

  public AbstractTupleParser(final ITupleParser parser) {
    _parser = parser;
  }

  @Override
  public Tuple next() {
    return _parser.next();
  }

  @Override
  public void parse(final File file) throws Exception {
    _parser.parse(file);
  }

  @Override
  public boolean hasNext() {
    return _parser.hasNext();
  }
}
