package de.ingrid.iplug.se.urlmaintenance.persistence.service;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.eclipse.persistence.jpa.JpaEntityManager;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

  private EntityManagerFactory _entityManagerFactory;

  private final ThreadLocal<EntityManager> _threadLocal = new ThreadLocal<EntityManager>() {
    @Override
    protected EntityManager initialValue() {
      return _entityManagerFactory.createEntityManager();
    }
  };

  private static TransactionService INSTANCE;

  public TransactionService() {
    String persistenceUnitName = System.getProperty("db.mode", "mysql");
    _entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
    INSTANCE = this;
  }

  public void beginTransaction() {
    EntityManager entityManager = getEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    if (!transaction.isActive()) {
      transaction.begin();
    }
  }

  private EntityManager getEntityManager() {
    EntityManager entityManager = _threadLocal.get();
    if (entityManager == null) {
      entityManager = _entityManagerFactory.createEntityManager();
      _threadLocal.set(entityManager);
    }
    return entityManager;
  }

  public void commitTransaction() {
    EntityManager entityManager = getEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction.isActive()) {
      transaction.commit();
    }
  }

  public void flipTransaction() {
    EntityManager entityManager = getEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction.isActive()) {
      transaction.commit();
      transaction.begin();
    }
  }

  public void rollbackTransaction() {
    EntityManager entityManager = getEntityManager();
    EntityTransaction transaction = entityManager.getTransaction();
    if (transaction.isActive()) {
      transaction.rollback();
    }
  }

  public void close() {
    EntityManager entityManager = getEntityManager();
    if (entityManager != null) {
      _threadLocal.set(null);
      entityManager.close();
    }
  }

  public void persist(Object object) {
    EntityManager entityManager = getEntityManager();
    entityManager.persist(object);
  }

  public void remove(Object object) {
    EntityManager entityManager = getEntityManager();
    entityManager.remove(object);
  }

  public Object getById(Class<?> clazz, Serializable id) {
    EntityManager entityManager = getEntityManager();
    return entityManager.find(clazz, id);
  }

  public Query createQuery(String query) {
    EntityManager entityManager = getEntityManager();
    return entityManager.createQuery(query);
  }

  public Query createNamedQuery(String name) {
    EntityManager entityManager = getEntityManager();
    return entityManager.createNamedQuery(name);
  }

  public void flush() {
    EntityManager entityManager = getEntityManager();
    entityManager.flush();
  }

  public void evictCache() {
      EntityManager entityManager = getEntityManager();
      ((JpaEntityManager)entityManager.getDelegate()).getServerSession().getIdentityMapAccessor().invalidateAll();
    }
  
  
  public void refresh(Object object) {
      EntityManager entityManager = getEntityManager();
      entityManager.refresh(object);
  }

  public static TransactionService getInstance() {
    return INSTANCE;
  }

  /**
   * Executes a <code>Runnable</code> within a transaction. See
   * {@linkplain #executeInTransaction(Callable)} for more details.
   * 
   * @param runnable
   *          The <code>Runnable</code> that contains the code.
   */
  public void executeInTransaction(Runnable runnable) {
    executeInTransaction(Executors.callable(runnable));
  }

  /**
   * Executes a <code>Callable</code> within a hibernate transaction. If no
   * transaction is open a new transaction will be openend and committed between
   * the <code>Callable.call()</code> execution. When a transaction is already
   * open this transaction will be used an no <code>commit()</code> or
   * <code>rollback()</code> will be called.
   * 
   * @param <K>
   *          The return type of <code>Callable.call()</code>.
   * @param callable
   *          The Callable than contains the hibernate code.
   * @return An arbitrary result of the {@link Callable#call()} method.
   */
  public <K> K executeInTransaction(Callable<K> callable) {
    EntityManager entityManager = getEntityManager();
    if (entityManager.getTransaction().isActive()) {
      try {
        return callable.call();
      } catch (Exception e) {
        throw convertToRuntimeException(e);
      }
    }

    entityManager.getTransaction().begin();
    boolean success = false;
    try {
      K result = callable.call();
      success = true;
      return result;
    } catch (Exception e) {
      throw convertToRuntimeException(e);
    } finally {
      if (success) {
        if (entityManager.getTransaction().isActive()) {
//          try {
            entityManager.getTransaction().commit();
//          } catch (StaleStateException e) {
//            entityManager.getTransaction().rollback();
//            throw e;
//          }
        }
      } else {
        if (entityManager.getTransaction().isActive()) {
          entityManager.getTransaction().rollback();
        }
      }
    }
  }

  public static RuntimeException convertToRuntimeException(Throwable t) {
    if (t instanceof RuntimeException) {
      return (RuntimeException) t;
    }
    retainInterruptFlag(t);
    return new RuntimeException(t);
  }

  /**
   * This sets the interrupt flag if the catched exception was an
   * {@link InterruptedException}. Catching such an exception always clears the
   * interrupt flag.
   * 
   * @param catchedException
   *          The catched exception.
   */
  public static void retainInterruptFlag(Throwable catchedException) {
    if (catchedException instanceof InterruptedException) {
      Thread.currentThread().interrupt();
    }
  }
}
