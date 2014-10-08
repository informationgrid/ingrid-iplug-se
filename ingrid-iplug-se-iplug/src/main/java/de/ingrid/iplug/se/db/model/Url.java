/*
 * Copyright (c) 2013 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iplug.se.db.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Url {

    /**
     * Date format used in hash calculation
     */
    //private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column
    private String instance;

//    @Past
//    @Temporal(TemporalType.DATE)
//    //@XmlSchemaType(name="updated")
//    private Date updated;
//
//    @Past
//    @Temporal(TemporalType.DATE)
//    //@XmlSchemaType(name="created")
//    private Date created;
//    
//    @Past
//    @Temporal(TemporalType.DATE)
//    //@XmlSchemaType(name="statusUpdated")
//    private Date statusUpdated;

    @Column(length=1024)
    private String url;
    
//    @Past
//    @Temporal(TemporalType.DATE)
//    //@XmlSchemaType(name="deleted")
//    private Date deleted;

    @Column
    private int status;

    @OneToMany(cascade=CascadeType.ALL)
    private List<Metadata> metadata;
    
    @ElementCollection
    private List<String> limitUrls;
    
    @ElementCollection
    private List<String> excludeUrls;
    
    public Url() {}
    
    public Url( String instance ) {
        setInstance( instance );
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }

    public List<String> getLimitUrls() {
        return limitUrls;
    }

    public void setLimitUrls(List<String> limitUrls) {
        this.limitUrls = limitUrls;
    }

    public List<String> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(List<String> excludeUrls) {
        this.excludeUrls = excludeUrls;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    
}
