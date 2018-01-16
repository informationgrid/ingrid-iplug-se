/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2018 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
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
