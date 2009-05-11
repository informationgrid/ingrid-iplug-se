package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "URL")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING, length = 20)
public class Url extends IdBase {

  private String _url;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date _timeStamp;

  @ManyToMany
  private List<Metadata> _metadatas = new ArrayList<Metadata>();

  @ManyToOne
  @JoinColumn(nullable = false, name = "provider_fk")
  private Provider _provider;

  public String getUrl() {
    return _url;
  }

  public void setUrl(String url) {
    _url = url;
  }

  public List<Metadata> getMetadatas() {
    return _metadatas;
  }

  public void setMetadatas(List<Metadata> metadatas) {
    _metadatas = metadatas;
  }

  public Provider getProvider() {
    return _provider;
  }

  public void setProvider(Provider provider) {
    _provider = provider;
  }

  public Date getTimeStamp() {
    return _timeStamp;
  }

  public void setTimeStamp(Date timeStamp) {
    _timeStamp = timeStamp;
  }

  
}
