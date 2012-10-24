package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
//@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "_metadataKey", "_metadataValue" }) })

@NamedQueries( {
    @NamedQuery(name = "getMetadataByKeyAndValue", query = "select m from Metadata as m where m.metadataKey = :key and m.metadataValue = :value"),
    @NamedQuery(name = "getMetadatasByKey", query = "select m from Metadata as m where m.metadataKey = :key") })
public class Metadata extends IdBase {

  @Column(nullable = false)
  private String metadataKey;

  @Column(nullable = true)
  private String metadataValue;

  public Metadata() {
  }

  public Metadata(String key, String value) {
    this.metadataKey = key;
    this.metadataValue = value;
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

  public String getMetadataValue() {
    return metadataValue;
  }

  public void setMetadataValue(String metadataValue) {
    this.metadataValue = metadataValue;
  }

  @Override
  public String toString() {
    return "key: " + metadataKey + " value:" + metadataValue;
  }
}
