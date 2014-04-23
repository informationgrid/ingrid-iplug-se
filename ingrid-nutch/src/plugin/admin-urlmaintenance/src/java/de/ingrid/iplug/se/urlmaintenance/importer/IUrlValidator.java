package de.ingrid.iplug.se.urlmaintenance.importer;

import java.util.Map;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer;

public interface IUrlValidator {

  boolean validate(UrlContainer urlContainer, Map<String, String> errorCodes);
}
