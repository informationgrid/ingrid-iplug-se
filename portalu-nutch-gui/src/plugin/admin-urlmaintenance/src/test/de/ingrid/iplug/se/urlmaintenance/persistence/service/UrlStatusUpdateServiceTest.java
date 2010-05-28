package de.ingrid.iplug.se.urlmaintenance.persistence.service;

import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import de.ingrid.iplug.se.communication.InterplugInCommunicationConstants;
import de.ingrid.iplug.se.communication.InterplugInQueueCommunication;
import de.ingrid.iplug.se.urlmaintenance.persistence.dao.IUrlDao;
import de.ingrid.iplug.se.util.TestUtil;

public class UrlStatusUpdateServiceTest extends TestCase {

  @Mock
  private IUrlDao _urlDao;
  private TransactionService _transactionService = new TransactionServiceMock();

  private UrlStatusUpdateService _urlStatusUpdateService;

  public void setUp() {
    MockitoAnnotations.initMocks(this);
    _urlStatusUpdateService = new UrlStatusUpdateService(_transactionService, _urlDao, null);
  }

  public void testPoll_validUrl() throws InterruptedException {
    addStatusUrlString("1:http://blah.com");
    addStatusUrlString("2:http://blah.com?x=y");

    _urlStatusUpdateService.start();

    TestUtil.waitUntilVerified(new Runnable() {

      @Override
      public void run() {
        Mockito.verify(_urlDao).updateStatus("http://blah.com", new Integer(1));
        Mockito.verify(_urlDao).updateStatus("http://blah.com?x=y", new Integer(2));
      }
    }, TimeUnit.SECONDS, 10);
  }

  private void addStatusUrlString(String value) {
    InterplugInQueueCommunication<String> instanceForQueues = InterplugInQueueCommunication
        .getInstanceForStringQueues();

    instanceForQueues.offer(InterplugInCommunicationConstants.URLSTATUS_KEY, value);
  }
}
