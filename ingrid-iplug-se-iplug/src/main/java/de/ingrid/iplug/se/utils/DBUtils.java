/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2017 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.utils;

import java.net.URL;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Url;
import de.ingrid.iplug.se.webapp.container.Instance;

public class DBUtils {
    
    private static Logger log = Logger.getLogger(DBUtils.class);
    
    public static List<Url> getAllUrlsFromInstance(String instance) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Url> createQuery = criteriaBuilder.createQuery(Url.class);
        Root<Url> urlTable = createQuery.from(Url.class);
        
        Predicate instanceCriteria = criteriaBuilder.equal( urlTable.get("instance"), instance );
        createQuery.select( urlTable ).where( instanceCriteria );
        
        List<Url> resultList = em.createQuery( createQuery ).getResultList();
        //TypedQuery<Url> query = em.createQuery("select u from Url u where u.instance=\"" + instance + "\"", Url.class);
        return resultList;
    }

    private static void persistUrl(EntityManager em, Url url) {
        if (url.getId() == null) {
            em.persist( url );
            
        } else {
            em.merge( url );            
        }
    }
    
    public static void addUrl(Url url) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        try {
            em.getTransaction().begin();
            persistUrl( em, url );
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("Error add url: " + url.getUrl(), e);
            em.getTransaction().rollback();
        }
    }

    public static void addUrls(List<Url> fromUrls) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();
        try {
            for (Url url : fromUrls) {
                persistUrl( em, url );
            }
            em.flush();
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("Error adding urls.", e);
            em.getTransaction().rollback();
        }
    }

    public static void deleteUrls(Long[] ids) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();
        try {
            for (Long id : ids) {
                Url url = em.find( Url.class, id );
                em.remove( url );
            }
            em.flush();
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("Error deleting urls.", e);
            em.getTransaction().rollback();
        }
    }
    
    public static void setStatus(Instance instance, String srcUrl, String status) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();

        em.getTransaction().begin();
        
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Url> createQuery = criteriaBuilder.createQuery(Url.class);
            Root<Url> urlTable = createQuery.from(Url.class);
            
            Predicate instanceCriteria = criteriaBuilder.equal( urlTable.get("instance"), instance.getName() );
            Predicate urlCriteria = criteriaBuilder.equal( urlTable.get("url"), srcUrl );
            URL tmpUrl = new URL(srcUrl);
            if (tmpUrl.getPath().equals("/")) {
                Predicate urlCriteria2 = criteriaBuilder.equal( urlTable.get("url"), srcUrl.substring(0, srcUrl.length() -1) );
                urlCriteria = criteriaBuilder.or(urlCriteria, urlCriteria2);
            }
            createQuery.select( urlTable ).where( criteriaBuilder.and(instanceCriteria, urlCriteria) );
            
            List<Url> resultList = em.createQuery( createQuery ).getResultList();
            if (!resultList.isEmpty()) {
                for (Url url : resultList) {
                    url.setStatus(status);
                    persistUrl( em, url );
                }
            }
            em.flush();
            em.getTransaction().commit();
        } catch (Exception e) {
            log.error("Error set status for url: " + srcUrl, e);
            em.getTransaction().rollback();
        }
        
    }
}
