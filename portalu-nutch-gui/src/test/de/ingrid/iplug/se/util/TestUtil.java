package de.ingrid.iplug.se.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mockito.exceptions.base.MockitoAssertionError;

public class TestUtil {

  private static final Log LOG = LogFactory.getLog(TestUtil.class);
  public static final String JUNIT_TEST_PLUGIN_FOLDERS = "101tec-nutch-a9cddd9/src/plugin";
  
  /**
   * This waits until the provided {@link Callable} returns an object that is equals to the given
   * expected value or the timeout has been reached. In both cases this method will return the
   * return value of the latest {@link Callable} execution.
   * 
   * @param expectedValue
   *            The expected value of the callable.
   * @param callable
   *            The callable.
   * @param <T>
   *            The return type of the callable.
   * @param timeUnit
   *            The timeout timeunit.
   * @param timeout
   *            The timeout.
   * @return the return value of the latest {@link Callable} execution.
   * @throws Exception
   * @throws InterruptedException
   */
  public static <T> T waitUntil(T expectedValue, Callable<T> callable, TimeUnit timeUnit, long timeout) throws Exception {
      long startTime = System.currentTimeMillis();
      do {
          T actual = callable.call();
          if (expectedValue.equals(actual)) {
              return actual;
          }
          if (System.currentTimeMillis() > startTime + timeUnit.toMillis(timeout)) {
              return actual;
          }
          Thread.sleep(50);
      } while (true);
  }

  /**
   * This waits until a mockito verification passed (which is provided in the runnable). This
   * waits until the verification passed or the timeout has been reached. If the timeout has been
   * reached this method will rethrow the {@link MockitoAssertionError} that comes from the
   * mockito verification code.
   * 
   * @param runnable
   *            The runnable containing the mockito verification.
   * @param timeUnit
   *            The timeout timeunit.
   * @param timeout
   *            The timeout.
   * @throws InterruptedException
   */
  public static void waitUntilVerified(Runnable runnable, TimeUnit timeUnit, int timeout) throws InterruptedException {
      LOG.debug("Waiting for " + timeout + " " + timeUnit + " until verified.");
      long startTime = System.currentTimeMillis();
      do {
          MockitoAssertionError exception = null;
          try {
              runnable.run();
          } catch (MockitoAssertionError e) {
              exception = e;
          }
          if (exception == null) {
              return;
          }
          if (System.currentTimeMillis() > startTime + timeUnit.toMillis(timeout)) {
              LOG.debug("Timeout reached without satifying expectations.");
              throw exception;
          }
          Thread.sleep(50);
      } while (true);
  }

  public static void waitUntilNoExceptionThrown(Runnable runnable, TimeUnit timeUnit, int timeout) throws InterruptedException {
      long startTime = System.currentTimeMillis();
      do {
          RuntimeException exception = null;
          try {
              runnable.run();
          } catch (RuntimeException e) {
              exception = e;
          }
          if (exception == null) {
              return;
          }
          if (System.currentTimeMillis() > startTime + timeUnit.toMillis(timeout)) {
              throw exception;
          }
          Thread.sleep(50);
      } while (true);
  }

  public static String stringify(Collection<?> items) {
      List<String> itemsAsStrings = new ArrayList<String>();
      for (Object o : items) {
          itemsAsStrings.add(o.toString());
      }
      Collections.sort(itemsAsStrings);
      StringBuilder builder = new StringBuilder();
      for (String item : itemsAsStrings) {
          builder.append(item);
          builder.append("\n");
      }
      return builder.toString();
  }

  public static void assertCollectionEquals(Collection<?> actual, Collection<?> expected) {
    TestCase.assertEquals(stringify(expected), stringify(actual));
  }
}
