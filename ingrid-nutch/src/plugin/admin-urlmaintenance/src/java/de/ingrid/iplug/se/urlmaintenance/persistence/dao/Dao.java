package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.IdBase;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public abstract class Dao<T extends IdBase> implements IDao<T> {

  protected Class<T> clazz;
  protected final TransactionService transactionService;

  public Dao(Class<T> clazz, TransactionService transactionService) {
    this.clazz = clazz;
    this.transactionService = transactionService;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T> getAll() {
    Query query = transactionService.createQuery("select t from "
        + clazz.getSimpleName() + " as t order by t.id asc");
    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T> getAll(int start, int length) {
    Query query = transactionService.createQuery("select t from "
        + clazz.getSimpleName() + " as t");
    query.setFirstResult(start);
    query.setMaxResults(length);
    return query.getResultList();

  }

  @SuppressWarnings("unchecked")
  @Override
  public T getById(Serializable id) {
    return (T) transactionService.getById(clazz, id);
  }

  @Override
  public void makePersistent(T t) {
    transactionService.persist(t);
  }

  @Override
  public void makeTransient(T t) {
    transactionService.remove(t);
  }

  @Override
  public void flush() {
    transactionService.flush();
  }

  @Override
  public void flipTransaction() {
    transactionService.flipTransaction();
  }
  
  @Override
  public void evictCache() {
    transactionService.evictCache();
  }
  

}
