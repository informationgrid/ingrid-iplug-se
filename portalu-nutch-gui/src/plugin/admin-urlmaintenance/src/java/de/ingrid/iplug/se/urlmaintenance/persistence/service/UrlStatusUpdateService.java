package de.ingrid.iplug.se.urlmaintenance.persistence.service;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.io.Text;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.ingrid.iplug.se.communication.InterplugInCommunicationConstants;
import de.ingrid.iplug.se.communication.InterplugInQueueCommunication;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IUrlDao;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IUrlLogDao;

@Component
public class UrlStatusUpdateService {

  private static final Log LOG = LogFactory.getLog(UrlStatusUpdateService.class);

  private final long _waitTimeMs = TimeUnit.SECONDS.toMillis(1);

  private final TransactionService _transactionService;
  private final IUrlDao _urlDao;
  private final IUrlLogDao _urlLogDao;

  @Autowired
  public UrlStatusUpdateService(TransactionService transactionService, IUrlDao urlDao, IUrlLogDao urlLogDao) {
    super();
    _transactionService = transactionService;
    _urlDao = urlDao;
    _urlLogDao = urlLogDao;
  }

  @PostConstruct
  public void start() {
    Runnable pollerThread = new Runnable() {

      @Override
      public void run() {
        poll();
      }
    };

    LOG.info("Starting poller " + getClass().getName());
    new Thread(pollerThread, "URL-Status-Updater").start();
  }

  public void poll() {
    boolean _shouldSleep = false;
    InterplugInQueueCommunication<String> instanceForQueues = InterplugInQueueCommunication
        .getInstanceForStringQueues();
    try {
      while (!Thread.currentThread().isInterrupted()) {
        if (_shouldSleep) {
          Thread.sleep(_waitTimeMs);
        }
        try {
          String statusAndUrl = instanceForQueues.poll(InterplugInCommunicationConstants.URLSTATUS_KEY);

          if (statusAndUrl != null && statusAndUrl.length() > 0) {
            _shouldSleep = false;
            int pos = statusAndUrl.indexOf(':');
            // if third parameter, written in Fetcher, is true then switch on URL-Logging
            final boolean isUrlLog = statusAndUrl.substring(0, pos).equals("true");
            int pos2 = statusAndUrl.indexOf(':', pos + 1);
            final String status = statusAndUrl.substring(pos + 1, pos2);
            final String url = statusAndUrl.substring(pos2+1);

            if (LOG.isDebugEnabled()) {
              LOG.debug("Try to update status '" + status + "' for url '" + url + "'.");
            }
            _transactionService.executeInTransaction(new Runnable() {
              
              @Override
              public void run() {
                updateUrlStatus(Integer.parseInt(status), url, isUrlLog);
              }
            });
          } else {
            _shouldSleep = true;
          }
        } catch (Throwable t) {
          LOG.error("Error occurred.", t);
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    LOG.info("Poller " + getClass().getName() + " terminated.");
  }

  private void updateUrlStatus(int status, String url, boolean isUrlLog) {
    _urlDao.updateStatus(url, status);
    if (isUrlLog)
        _urlLogDao.updateStatus(url, status);
  }

  public static String createString(int status, Text url) {
    return status + ":" + url.toString();
  }
}
