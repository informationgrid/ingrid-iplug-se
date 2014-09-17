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
