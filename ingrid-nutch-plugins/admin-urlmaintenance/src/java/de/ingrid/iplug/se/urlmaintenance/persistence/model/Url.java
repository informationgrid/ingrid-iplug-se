package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.nutch.protocol.ProtocolStatus;

@Entity
@Cacheable(false)
@Table(name = "URL")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING, length = 20)
@NamedQueries(value = {
    @NamedQuery(name = "countUrlsThatUsesSpecialProviders", query = "select count(u) from Url as u where u.provider.id in :providersIds") })
public class Url extends IdBase {

  @Column(columnDefinition = "VARCHAR(1024)")
  private String url;

  @Temporal(TemporalType.TIMESTAMP)
  private Date created = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  private Date updated = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  private Date deleted = null;
  
  @ManyToOne
  @JoinColumn(nullable = false, name = "provider_fk")
  private Provider provider;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(name="URL_METADATA",
                 joinColumns=
                      @JoinColumn(name="Url__ID"),
                 inverseJoinColumns=
                      @JoinColumn(name="metadatas__ID")
     )
  protected List<Metadata> metadatas = new ArrayList<Metadata>();

  /**
   * The status given as defined in {@link ProtocolStatus}. db-update: alter
   * table url add column _STATUS INTEGER;
   */
  protected Integer status = null;
  /**
   * The timestamp, the status was updated by a fetch for this url db-update:
   * alter table url add column _STATUSUPDATED DATETIME;
   */
  @Temporal(TemporalType.TIMESTAMP)
  private Date statusUpdated = null;

  public Url() {
  }

  public Url(final String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public Provider getProvider() {
    return provider;
  }

  public void setProvider(final Provider provider) {
    this.provider = provider;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(final Date timeStamp) {
    created = timeStamp;
  }

  public Date getUpdated() {
    return updated;
  }

  public void setUpdated(final Date edited) {
    updated = edited;
  }

  public Date getDeleted() {
      return deleted;
    }

    public void setDeleted(final Date deleted) {
      this.deleted = deleted;
    }
  
  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public Date getStatusUpdated() {
    return statusUpdated;
  }

  public void setStatusUpdated(Date statusUpdated) {
    this.statusUpdated = statusUpdated;
  }

  public String getStatusAsText() {
    if (status == null) {
      return "";
    }
    // As the ProtocollStatus does not provide a method to resolve the code to a
    // human readable message, we have to fix it here.
    String strOutput = new ProtocolStatus(status).toString();
    int pos = strOutput.indexOf('(');
    String ret = "";
    if (pos > 0) {
      ret = strOutput.substring(0, pos);
      if (statusUpdated != null) {
        ret += " (" + new SimpleDateFormat("yyyy-MM-dd").format(statusUpdated) + ")";
      }
    }
    return ret;
  }

  @Override
  public String toString() {
    String s = super.toString();
    return s += (" url:" + url);
  }
}
