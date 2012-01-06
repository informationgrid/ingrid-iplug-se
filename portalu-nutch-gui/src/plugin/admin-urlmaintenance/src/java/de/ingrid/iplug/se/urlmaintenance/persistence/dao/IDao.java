package de.ingrid.iplug.se.urlmaintenance.persistence.dao;

import java.io.Serializable;
import java.util.List;

public interface IDao<T> {

  T getById(Serializable id);

  void makePersistent(T t);

  void makeTransient(T t);

  List<T> getAll();

  List<T> getAll(int start, int length);

  void flush();

  void flipTransaction();
  
  void evictCache();

}
