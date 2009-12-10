package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LineParser implements ITupleParser {

  public static final String KEY = "line";

  private final int _skip;

  private Tuple _current;

  private Tuple _next;

  private BufferedReader _reader;

  public LineParser() {
    _skip = 0;
  }

  public LineParser(final int skip) {
    _skip = skip;
  }

  @Override
  public boolean hasNext() {
    return readNext();
  }

  @Override
  public Tuple next() {
    nextToCurrent();
    return _current;
  };

  @Override
  public final void parse(final File file) throws Exception {
    _reader = new BufferedReader(new FileReader(file));
    // skip first lines
    for (int i = 0; i < _skip; i++) {
      _reader.readLine();
    }
  }

  private boolean readNext() {
    if (_next == null) {
      if (_reader != null) {
        try {
          final String line = _reader.readLine();
          if (line != null && line.replace(",", "").trim().length() > 1) {
            _next = Tuple.create(KEY, line);
            return true;
          }
        } catch (final IOException e) {
        }
      }
      return false;
    }
    return true;
  }

  private void nextToCurrent() {
    readNext();
    _current = _next;
    _next = null;
  }
}
