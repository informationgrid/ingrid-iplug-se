/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2019 wemove digital solutions GmbH
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
    private String esTransportTcpPort;
    private String esHttpHost;
    private String clusterName;
    private String instanceIndexName;

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

    public String getClusterName() {
        return this.clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getInstanceIndexName() {
        return indexName + "_" + name;
    }
}
