package de.ingrid.iplug.se.webapp.controller.instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.db.DBManager;
import de.ingrid.iplug.se.db.model.Url;

@RestController
@RequestMapping("/rest")
public class RestDataController {
    
    @RequestMapping(value = { "/test" }, method = RequestMethod.GET)
    public String test() {
        return "OK";
    }

    @RequestMapping(value = { "url/{id}" }, method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<Url> getUrl(@PathVariable("id") Long id) {
//    @RequestMapping(value = { "url.json" }, method = RequestMethod.GET, produces = "application/json")
//    public Url getUrl(@RequestParam("id") String id) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        
        Url url = em.find( Url.class, id );
        
//        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
//
//        // select patients for the current page
//        CriteriaQuery<Url> urlQuery = criteriaBuilder.createQuery(Url.class);
//        Root<Url> urlTpl = urlQuery.from(Url.class);
//        TypedQuery<Url> urlPagingQuery = em.createQuery(urlQuery);
//        Url url = urlPagingQuery.getSingleResult();
        //return url;
        return new ResponseEntity<Url>( url, url != null ? HttpStatus.OK : HttpStatus.NOT_FOUND );
    }

    @RequestMapping(value = { "addUrl.json" }, method = RequestMethod.POST)
    public ResponseEntity<Url> addUrl(@RequestBody Url url) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();
        if (url.getId() == null) {
            em.persist( url );            
            
        } else {
            em.merge( url );            
        }
        em.getTransaction().commit();
        return new ResponseEntity<Url>( url, HttpStatus.OK );
    }
    
    @RequestMapping(value = { "deleteUrls.json" }, method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> deleteUrls(@RequestBody Long[] ids) {
        EntityManager em = DBManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();
        for (Long id : ids) {
            Url url = em.find( Url.class, id );
            em.remove( url );
        }
        em.getTransaction().commit();
        Map<String, String> result = new HashMap<String, String>();
        result.put( "result", "OK" );
        return new ResponseEntity<Map<String, String>>( result, HttpStatus.OK );
    }

    @RequestMapping(value = { "instance/{name}/{value}" }, method = RequestMethod.POST)
    public ResponseEntity<Map<String, String>> toggleInstanceActive(@PathVariable("name") String name, @PathVariable("value") String value) {
        
        List<String> activeInstances = SEIPlug.conf.activeInstances;
        if ("on".equals( value )) {
            activeInstances.add( name );
        } else {
            activeInstances.remove( name );            
        }
        
        return generateOkResponse();
    }
    
    private ResponseEntity<Map<String, String>> generateOkResponse() {
        Map<String, String> result = new HashMap<String, String>();
        result.put( "result", "OK" );
        return new ResponseEntity<Map<String, String>>( result, HttpStatus.OK );
    }
}