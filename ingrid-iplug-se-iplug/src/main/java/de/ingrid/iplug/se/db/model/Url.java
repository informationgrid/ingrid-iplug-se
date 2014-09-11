/*
 * Copyright (c) 2013 wemove digital solutions. All rights reserved.
 */
package de.ingrid.iplug.se.db.model;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Past;

/**
 * Implementation of a patient.
 * 
 * @author ingo
 */
@Entity
public class Url {

    /**
     * Date format used in hash calculation
     */
    //private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length=20)
    private String type;

    @Past
    @Temporal(TemporalType.DATE)
    //@XmlSchemaType(name="updated")
    private Date updated;

    @Past
    @Temporal(TemporalType.DATE)
    //@XmlSchemaType(name="created")
    private Date created;
    
    @Past
    @Temporal(TemporalType.DATE)
    //@XmlSchemaType(name="statusUpdated")
    private Date statusUpdated;

    @Column(length=1024)
    private String url;
    
    @Past
    @Temporal(TemporalType.DATE)
    //@XmlSchemaType(name="deleted")
    private Date deleted;

    @Column
    private int status;

    @OneToMany(cascade=CascadeType.ALL)
    private List<Metadata> metadata;
    
    @ElementCollection
    private List<String> limitUrls;
    
    @ElementCollection
    private List<String> excludeUrls;
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getStatusUpdated() {
        return statusUpdated;
    }

    public void setStatusUpdated(Date statusUpdated) {
        this.statusUpdated = statusUpdated;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getDeleted() {
        return deleted;
    }

    public void setDeleted(Date deleted) {
        this.deleted = deleted;
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

    
}
