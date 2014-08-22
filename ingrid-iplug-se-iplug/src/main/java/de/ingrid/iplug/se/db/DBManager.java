package de.ingrid.iplug.se.db;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public enum DBManager {

    // singleton instance
    INSTANCE;

    // make sure that there is an entity managers for each thread, because
    // instances are not thread safe.
    private static EntityManagerFactory emf = null;

    /**
     * Initialize the manager.
     * @param emf EntityManagerFactory
     */
    public void intialize(EntityManagerFactory emf) {
        DBManager.emf = emf;
    }

    /**
     * Check if the  manager is initialized.
     * @return boolean
     */
    public boolean isIntialized() {
        return (emf != null);
    }

    /**
     * Get an entity manager instance.
     * @return EntityManager
     */
    public EntityManager getEntityManager() {
        if (emf != null) {
            return emf.createEntityManager();
        }
        else {
            throw new RuntimeException("DBManager is not initialized.");
        }
    }

    /**
     * Get a property from the EntityManagerFactory
     * @param key
     * @return Object
     */
    public Object getProperty(String key) {
        if (emf != null) {
            Map<String, Object> properties = emf.getProperties();
            if (properties.containsKey(key)) {
                return properties.get(key);
            }
        }
        return null;
    }

    /**
     * Close the manager.
     */
    public void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
