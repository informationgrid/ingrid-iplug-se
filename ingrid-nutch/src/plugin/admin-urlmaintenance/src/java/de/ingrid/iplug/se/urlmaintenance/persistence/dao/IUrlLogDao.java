package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import org.apache.nutch.protocol.Protocol;
import org.apache.nutch.protocol.ProtocolStatus;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.UrlLog;

public interface IUrlLogDao extends IDao<UrlLog> {

  /**
   * Searches a given url in database and inserts/updates the status when a
   * result is found.
   * 
   * @param url
   *          The url to which the status is delivered.
   * @param status
   *          The status as provided from
   *          {@linkplain Protocol#getProtocolOutput()} method call. The integer
   *          value is mapped in {@linkplain ProtocolStatus} object.
   */
  void updateStatus(String url, Integer status);

}
