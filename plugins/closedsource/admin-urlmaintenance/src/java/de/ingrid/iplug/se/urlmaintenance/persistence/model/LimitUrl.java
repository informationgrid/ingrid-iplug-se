package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("LIMIT")
public class LimitUrl extends WebUrl {

  @ManyToOne
  @JoinColumn(name = "startUrl_fk")
  private StartUrl _startUrl;

  public StartUrl getStartUrl() {
    return _startUrl;
  }

  public void setStartUrl(StartUrl startUrl) {
    _startUrl = startUrl;
  }

}
