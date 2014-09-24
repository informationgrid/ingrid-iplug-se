package de.ingrid.iplug.se.db;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.ingrid.iplug.se.db.model.Metadata;
import de.ingrid.iplug.se.db.model.Url;

public class UrlHandler {
    public static List<Url> getUrlsByInstance( String instance ) {
        return getUrlsByInstanceFiltered( instance, null );
    }
    
    public static List<Url> getUrlsByInstanceFiltered( String instance, String[] metadata ) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<Url> createQuery = em.getCriteriaBuilder().createQuery( Url.class );
        Root<Url> urlTable = createQuery.from( Url.class );

        List<Predicate> criteria = new ArrayList<Predicate>();
        criteria.add( criteriaBuilder.equal( urlTable.<String> get ("instance"), instance ) );
        
        if (metadata != null && metadata.length > 0) {
            for (String meta : metadata) {
                Join<Url, Metadata> j = urlTable.join("metadata", JoinType.LEFT );
                String[] metaSplit = meta.split( ":" );
                if (metaSplit.length == 2) {
                    criteria.add( criteriaBuilder.equal( j.get( "metaKey" ), metaSplit[0] ) );
                    criteria.add( criteriaBuilder.equal( j.get( "metaValue" ), metaSplit[1] ) );
                }
            }
        }
        
        createQuery.select( urlTable ).where( criteriaBuilder.and(criteria.toArray(new Predicate[0])) );
        TypedQuery<Url> tq = em.createQuery(createQuery);
        return tq.getResultList();
    }
}
