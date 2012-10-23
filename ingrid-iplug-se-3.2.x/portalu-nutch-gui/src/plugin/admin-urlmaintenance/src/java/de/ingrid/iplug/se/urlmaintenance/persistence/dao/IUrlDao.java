package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.List;

import org.apache.nutch.protocol.Protocol;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;

public interface IUrlDao extends IDao<Url> {

  /**
   * Searches a given url in database and upates the status when a result is
   * found. If no url exists in database (the mostly case during fetching) no
   * status is set.
   * 
   * @param url
   *          The url to which the status is delivered.
   * @param status
   *          The status as provided from
   *          {@linkplain Protocol#getProtocolOutput()} method call. The integer
   *          value is mapped in {@linkplain ProtocolStatus} object.
   */
  void updateStatus(String url, Integer status);

  /**
   * Provides the number of urls that refer to one or more providers.
   * 
   * @param providersIds
   *          The ids of providers to test.
   * @return Number of urls that have a foreign key to given providers.
   */
  int countByProvider(List<Long> providersIds);
}
