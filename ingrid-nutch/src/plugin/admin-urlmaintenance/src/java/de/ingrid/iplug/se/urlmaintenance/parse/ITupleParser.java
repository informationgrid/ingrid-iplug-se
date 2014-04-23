package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.File;

public interface ITupleParser {

  void parse(final File file) throws Exception;

  boolean hasNext();

  Tuple next();
}
