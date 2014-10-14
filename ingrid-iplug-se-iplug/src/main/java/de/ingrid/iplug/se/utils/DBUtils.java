package de.ingrid.iplug.se.utils;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Url;

public class DBUtils {
    
    public static List<Url> getAllUrlsFromInstance(String instance) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();
        
//        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
//        CriteriaQuery<Url> createQuery = criteriaBuilder.createQuery(Url.class);
//        Root<Url> urlTable = createQuery.from(Url.class);
//        
//        Predicate instanceCriteria = criteriaBuilder.equal( urlTable.get("instance"), instance );
//        
//        createQuery.from( Url.class );
//        createQuery.where( instanceCriteria );
        
        //List<Url> resultList = em.createQuery( createQuery ).getResultList();
        TypedQuery<Url> query = em.createQuery("select u from Url u where u.instance=\"" + instance + "\"", Url.class);
        em.getTransaction().commit();
        return query.getResultList();
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
        em.getTransaction().begin();
        persistUrl( em, url );
        em.getTransaction().commit();
    }

    public static void addUrls(List<Url> fromUrls) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();
        for (Url url : fromUrls) {
            persistUrl( em, url );
        }
        em.flush();
        em.getTransaction().commit();
    }

    public static void deleteUrls(Long[] ids) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();
        for (Long id : ids) {
            Url url = em.find( Url.class, id );
            em.remove( url );
        }
        em.flush();
        em.getTransaction().commit();
    }
}
