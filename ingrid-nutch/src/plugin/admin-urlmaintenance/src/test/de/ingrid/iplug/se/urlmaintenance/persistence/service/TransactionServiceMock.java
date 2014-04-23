package de.ingrid.iplug.se.urlmaintenance.persistence.service;

import java.util.concurrent.Callable;

public class TransactionServiceMock extends TransactionService {

  public TransactionServiceMock() {
    super();
  }

  @Override
  public <K> K executeInTransaction(Callable<K> callable) {
    try {
      return callable.call();
    } catch (Exception e) {
      throw TransactionService.convertToRuntimeException(e);
    }
  }

  @Override
  public void executeInTransaction(Runnable runnable) {
    runnable.run();
  }
}