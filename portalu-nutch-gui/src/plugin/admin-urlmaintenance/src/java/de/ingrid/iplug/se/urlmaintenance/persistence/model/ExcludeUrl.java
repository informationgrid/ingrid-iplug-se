package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@Cacheable(false)
@DiscriminatorValue("EXCLUDE")
public class ExcludeUrl extends WebUrl {

  @ManyToOne
  @JoinColumn(name = "startUrl_fk")
  private StartUrl startUrl;

  public StartUrl getStartUrl() {
    return startUrl;
  }

  public void setStartUrl(StartUrl startUrl) {
    this.startUrl = startUrl;
  }

}
