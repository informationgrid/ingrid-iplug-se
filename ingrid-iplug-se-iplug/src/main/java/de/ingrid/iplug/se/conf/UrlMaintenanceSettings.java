/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2025 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.2 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.conf;

import java.util.List;

public class UrlMaintenanceSettings {
  
    public class Metadata {
        private String id;
        
        private String label;
        
        private Boolean isDefault;
        
        private List<Metadata> children;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public List<Metadata> getChildren() {
            return children;
        }

        public void setChildren(List<Metadata> children) {
            this.children = children;
        }

        public Boolean getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Boolean isDefault) {
            this.isDefault = isDefault;
        }

    }
    
    public class MetaElement {
        private String id;
        
        private String label;
        
        private String type;
        
        private Boolean isMultiple;
        
        private Boolean isDisabled;
        
        private List<Metadata> children;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Metadata> getChildren() {
            return children;
        }

        public void setChildren(List<Metadata> children) {
            this.children = children;
        }

        public Boolean getIsMultiple() {
            return isMultiple;
        }

        public void setIsMultiple(Boolean multiple) {
            this.isMultiple = multiple;
        }
        
        public Boolean getIsDisabled() {
            return isDisabled;
        }

        public void setIsDisabled(Boolean disabled) {
            this.isDisabled = disabled;
        }
    }
    
    private List<MetaElement> metadata;
    
    public List<MetaElement> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<MetaElement> metadata) {
        this.metadata = metadata;
    }

}
