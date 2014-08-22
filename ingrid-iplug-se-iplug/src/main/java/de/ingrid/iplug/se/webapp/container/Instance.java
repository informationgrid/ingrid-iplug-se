package de.ingrid.iplug.se.webapp.container;

import java.util.List;

import de.ingrid.iplug.se.db.model.Url;

public class Instance {

    private String name;
    private String status;
    private List<Url> urls;

    public Instance() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Url> getUrls() {
        return this.urls;    
    }
    
    public void setUrls(List<Url> urls) {
        this.urls = urls;        
    }
}
