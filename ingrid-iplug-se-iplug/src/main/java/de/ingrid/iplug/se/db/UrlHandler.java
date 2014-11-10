/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 wemove digital solutions GmbH
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
