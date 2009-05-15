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
  private StartUrl _startUrl;


  public StartUrl getStartUrl() {
    return _startUrl;
  }

  public void setStartUrl(StartUrl startUrl) {
    _startUrl = startUrl;
  }

  public List<Metadata> getMetadatas() {
    return _metadatas;
  }

  public void setMetadatas(List<Metadata> metadatas) {
    _metadatas = metadatas;
  }

  public void addMetadata(Metadata metadata) {
    _metadatas.add(metadata);
  }

}
