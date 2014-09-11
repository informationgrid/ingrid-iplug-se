package de.ingrid.iplug.se.webapp.controller.instance;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Url;

@RestController
@RequestMapping("/rest")
public class RestDataController {

    @RequestMapping(value = { "url.json" }, method = RequestMethod.GET, produces = "application/json")
    public Url getUrl() {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();

        // select patients for the current page
        CriteriaQuery<Url> urlQuery = criteriaBuilder.createQuery(Url.class);
        Root<Url> urlTpl = urlQuery.from(Url.class);
        TypedQuery<Url> urlPagingQuery = em.createQuery(urlQuery);
        Url url = urlPagingQuery.getSingleResult();
//        url.getLimitUrls()
//        url.getMetadata()
        return url;
    }

    @RequestMapping(value = { "addUrl.json" }, method = RequestMethod.POST)
    public ResponseEntity<String> addUrl(@RequestBody Url url) {
        return new ResponseEntity<String>( "URL Added", HttpStatus.OK );
    }
    
    @RequestMapping(value = "meta.json", method = RequestMethod.GET, headers = "Accept=application/json")
    public User getMeta() {
        User user = new User();
        user.setUserid( 1 );
        user.setFirstName( "andre" );
        user.setLastName( "wallat" );
        user.setEmail( "aw@test.de" );

        return user;
    }

}