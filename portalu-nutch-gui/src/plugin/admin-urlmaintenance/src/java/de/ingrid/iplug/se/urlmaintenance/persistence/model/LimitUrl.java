package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("LIMIT")
public class LimitUrl extends WebUrl {

  @ManyToOne
  @JoinColumn(name = "startUrl_fk")
  private StartUrl startUrl;

  public StartUrl getStartUrl() {
    return startUrl;
  }

  public void setStartUrl(StartUrl startUrl) {
    this.startUrl = startUrl;
  }

  public List<Metadata> getMetadatas() {
    return metadatas;
  }

  public void setMetadatas(List<Metadata> metadatas) {
    this.metadatas = metadatas;
  }

  public void addMetadata(Metadata metadata) {
    this.metadatas.add(metadata);
  }

}
