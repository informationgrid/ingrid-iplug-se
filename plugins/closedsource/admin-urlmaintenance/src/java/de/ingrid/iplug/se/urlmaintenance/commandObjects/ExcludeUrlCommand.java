package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.ExcludeUrl;

public class ExcludeUrlCommand extends ExcludeUrl {

  private Long _id;

  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

}
