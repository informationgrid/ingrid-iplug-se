package de.ingrid.iplug.se.urlmaintenance.commandObjects;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.LimitUrl;

public class LimitUrlCommand extends LimitUrl {

  private Long _id;

  public LimitUrlCommand(LimitUrl limitUrl) {
    _id = limitUrl.getId();
    setCreated(limitUrl.getCreated());
    setUpdated(limitUrl.getUpdated());
  }

  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }

}
