package de.ingrid.iplug.se.urlmaintenance.persistence.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.nutch.protocol.ProtocolStatus;

@Entity
@Table(name = "URL_LOG")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class UrlLog extends IdBase {

  @Column(length=2048)
  private String url;

  @Temporal(TemporalType.TIMESTAMP)
  private Date created = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  private Date updated = new Date();

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

  public UrlLog() {
  }

  public UrlLog(final String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
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
