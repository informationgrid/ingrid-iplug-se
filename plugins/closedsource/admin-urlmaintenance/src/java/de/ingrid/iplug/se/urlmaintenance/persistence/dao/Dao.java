package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import de.ingrid.iplug.se.urlmaintenance.persistence.model.IdBase;
import de.ingrid.iplug.se.urlmaintenance.persistence.service.TransactionService;

public class Dao<T extends IdBase> implements IDao<T> {

  protected Class<T> _clazz;
  protected final TransactionService _transactionService;

  public Dao(Class<T> clazz, TransactionService transactionService) {
    _clazz = clazz;
    _transactionService = transactionService;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T> getAll() {
    Query query = _transactionService.createQuery("select t from "
        + _clazz.getSimpleName() + " as t");
    return query.getResultList();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<T> getAll(int start, int length) {
    Query query = _transactionService.createQuery("select t from "
        + _clazz.getSimpleName() + " as t");
    query.setFirstResult(start);
    query.setMaxResults(length);
    return query.getResultList();

  }

  @SuppressWarnings("unchecked")
  @Override
  public T getById(Serializable id) {
    return (T) _transactionService.getById(_clazz, id);
  }

  @Override
  public void makePersistent(T t) {
    _transactionService.persist(t);
  }

  @Override
  public void makeTransient(T t) {
    _transactionService.remove(t);
  }

}
