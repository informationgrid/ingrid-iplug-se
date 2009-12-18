package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.Url;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;
import de.ingrid.iplug.se.urlmaintenance.util.TimeProvider;

@Service
public class UrlDao extends Dao<Url> implements IUrlDao {

  private static final Log LOG = LogFactory.getLog(UrlDao.class);
  private TimeProvider _timeProvider;

  @Autowired
  public UrlDao(TransactionService transactionService, TimeProvider timeProvider) {
    super(Url.class, transactionService);
    _timeProvider = timeProvider;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void updateStatus(String url, Integer status) {
    Query q = _transactionService.createQuery("select u from Url u where u._url = :url");
    q.setParameter("url", url);
    List<? extends Url> urls = q.getResultList();

    if (urls != null && urls.size() > 0) {
      for (Url urlFromDb : urls) {
        urlFromDb.setStatus(status);
        urlFromDb.setStatusUpdated(new Date(_timeProvider.getTime()));
        makePersistent(urlFromDb);
        LOG.info("Status updated to '" + status + "' for url '" + url + "' (" + urlFromDb.getId() + ") in database.");
      }
    }
  }

  @Override
  public int countByProvider(List<Long> providersIds) {
    if (providersIds.size() == 0) {
      return 0;
    }
    Query query = _transactionService.createNamedQuery("countUrlsThatUsesSpecialProviders");
    query.setParameter("providersIds", providersIds);
    Number count = (Number) query.getSingleResult();
    return count.intValue();
  }
}
