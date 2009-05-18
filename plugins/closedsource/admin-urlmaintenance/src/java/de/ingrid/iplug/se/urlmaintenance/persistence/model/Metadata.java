package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "_metadataKey",
    "_metadataValue" }) })
@NamedQueries( {
    @NamedQuery(name = "getMetadataByKeyAndValue", query = "select m from Metadata as m where m._metadataKey = :key and m._metadataValue = :value"),
    @NamedQuery(name = "getMetadatasByKey", query = "select m from Metadata as m where m._metadataKey = :key") })
public class Metadata extends IdBase {

  @Column(nullable = false)
  private String _metadataKey;

  @Column(nullable = false)
  private String _metadataValue;

  public Metadata() {
  }

  public Metadata(String key, String value) {
    _metadataKey = key;
    _metadataValue = value;
  }

  public String getMetadataKey() {
    return _metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    _metadataKey = metadataKey;
  }

  public String getMetadataValue() {
    return _metadataValue;
  }

  public void setMetadataValue(String metadataValue) {
    _metadataValue = metadataValue;
  }

  @Override
  public String toString() {
    return "key: " + _metadataKey + " value:" + _metadataValue;
  }
}
