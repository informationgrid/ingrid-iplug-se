package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.CatalogUrl;

public class CatalogUrlCommand extends CatalogUrl {

  private Long _id = -1L;

  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }
  
  @Override
  public String toString() {
    return getProvider() + "#" + getUrl();
  }

}
