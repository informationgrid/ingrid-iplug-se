package de.ingrid.iplug.se.urlmaintenance.importer;

import java.util.HashMap;
import java.util.Map;

import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer;
import de.ingrid.iplug.se.urlmaintenance.parse.UrlContainer.UrlType;

public class ContainerCommand {

  private UrlType _type;

  private final Map<UrlContainer, Map<String, String>> _containers = new HashMap<UrlContainer, Map<String, String>>();

  public void addContainer(final UrlContainer container, final Map<String, String> errors) {
    _containers.put(container, errors);
  }

  public void addContainer(final UrlContainer container) {
    addContainer(container, new HashMap<String, String>());
  }

  public void clear() {
    _containers.clear();
  }

  public Map<UrlContainer, Map<String, String>> getContainers() {
    return _containers;
  }

  public void setType(final UrlType type) {
    _type = type;
  }

  public UrlType getType() {
    return _type;
  }

  public boolean getIsValid() {
    for (final UrlContainer container : _containers.keySet()) {
      if (!container.isValid()) {
        return false;
      }
    }
    return true;
  }
}
