package de.ingrid.iplug.se.conf;

import java.util.List;

public class UrlMaintenanceSettings {
  
    public class Metadata {
        private String id;
        
        private String label;
        
        private List<MetaElement> children;

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

        public List<MetaElement> getChildren() {
            return children;
        }

        public void setChildren(List<MetaElement> children) {
            this.children = children;
        }
    }
    
    public class MetaElement {
        private String id;
        
        private String label;
        
        private String type;
        
        private boolean multiple;
        
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

        public boolean isMultiple() {
            return multiple;
        }

        public void setMultiple(boolean multiple) {
            this.multiple = multiple;
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
