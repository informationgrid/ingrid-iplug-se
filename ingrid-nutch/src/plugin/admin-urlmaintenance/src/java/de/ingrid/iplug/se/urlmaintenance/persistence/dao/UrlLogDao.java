package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.UrlLog;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;
import de.ingrid.iplug.util.TimeProvider;

@Service
public class UrlLogDao extends Dao<UrlLog> implements IUrlLogDao {

  private static final Log LOG = LogFactory.getLog(UrlLogDao.class);
  private TimeProvider timeProvider;

  @Autowired
  public UrlLogDao(TransactionService transactionService, TimeProvider timeProvider) {
    super(UrlLog.class, transactionService);
    this.timeProvider = timeProvider;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateStatus(String url, Integer status) {
    Query q = transactionService.createQuery("select ulog from UrlLog ulog where ulog.url = :url");
    q.setParameter("url", url);
    List<? extends UrlLog> urlLogs = q.getResultList();

    if (urlLogs != null && urlLogs.size() > 0) {
      for (UrlLog urlLogFromDb : urlLogs) {
        urlLogFromDb.setStatus(status);
        urlLogFromDb.setStatusUpdated(new Date(timeProvider.getTime()));
        makePersistent(urlLogFromDb);
        if (LOG.isDebugEnabled()) {
          LOG.debug("Status updated to '" + status + "' for url '" + url + "' (" + urlLogFromDb.getId() + ") in database.");
        }
      }
    } else {
      UrlLog urlLog = new UrlLog();
      urlLog.setStatus(status);
      urlLog.setStatusUpdated(new Date(timeProvider.getTime()));
      urlLog.setUrl(url);
      urlLog.setCreated(new Date());
      urlLog.setUpdated(new Date());
      makePersistent(urlLog);
      if (LOG.isDebugEnabled()) {
        LOG.debug("Status created '" + status + "' for url '" + url + "' (" + urlLog.getId() + ") in database.");
      }
    }
  }
}
