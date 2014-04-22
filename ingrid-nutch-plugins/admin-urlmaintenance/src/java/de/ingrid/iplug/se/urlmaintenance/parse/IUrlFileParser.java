package de.ingrid.iplug.se.urlmaintenance.parse;

import java.io.File;
import java.io.IOException;

public interface IUrlFileParser {

  void parse(File file) throws Exception;

  boolean hasNext() throws IOException;

  UrlContainer next() throws IOException;
}
