package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("CATALOG")
public class CatalogUrl extends Url {

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
