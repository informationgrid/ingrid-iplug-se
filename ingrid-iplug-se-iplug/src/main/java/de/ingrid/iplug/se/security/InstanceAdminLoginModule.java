/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
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
package de.ingrid.iplug.se.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.InstanceAdmin;

public class InstanceAdminLoginModule {// extends AbstractLoginModule {

    /*@Override
    protected IngridPrincipal authenticate(String userName, String password) {

        IngridPrincipal ingridPrincipal = null;

        EntityManager em = DBManager.INSTANCE.getEntityManager();

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<InstanceAdmin> createQuery = criteriaBuilder.createQuery( InstanceAdmin.class );
        Root<InstanceAdmin> adminTable = createQuery.from( InstanceAdmin.class );
        List<Predicate> criteria = new ArrayList<Predicate>();
        criteria.add( criteriaBuilder.equal( adminTable.get( "login" ), userName ) );
        criteria.add( criteriaBuilder.equal( adminTable.get( "password" ), password ) );
        createQuery.select( adminTable ).where( criteriaBuilder.and( criteria.toArray( new Predicate[0] ) ) );

        List<InstanceAdmin> resultList = em.createQuery( createQuery ).getResultList();

        if (resultList != null && !resultList.isEmpty()) {
            Set<String> set = new HashSet<String>();
            set.add( "instanceAdmin" );
            ingridPrincipal = new IngridPrincipal.KnownPrincipal( userName, password, set );
        } else {
            ingridPrincipal = new IngridPrincipal.UnknownPrincipal();
        }

        return ingridPrincipal;
    }*/

}
