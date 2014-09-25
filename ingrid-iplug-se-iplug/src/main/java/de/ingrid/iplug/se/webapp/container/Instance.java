package de.ingrid.iplug.se.webapp.container;

import java.util.List;

import de.ingrid.iplug.se.db.model.Url;

public class Instance {

    private String name;
    private String workingDirectory;
    private String indexName;
    private String status;
    private boolean isActive;
    private List<Url> urls;
    private boolean indexTypeExists;
    private String esTransportTcpPort;
    private String esHttpHost;

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

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean getIndexTypeExists() {
        return this.indexTypeExists;        
    }
    
    public void setIndexTypeExists(boolean typeExists) {
        this.indexTypeExists = typeExists;        
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getEsTransportTcpPort() {
        return esTransportTcpPort;
    }

    public void setEsTransportTcpPort(String esTransportTcpPort) {
        this.esTransportTcpPort = esTransportTcpPort;
    }

    public String getEsHttpHost() {
        return esHttpHost;
    }

    public void setEsHttpHost(String esHttpHost) {
        this.esHttpHost = esHttpHost;
    }
    
    
    
    
}
