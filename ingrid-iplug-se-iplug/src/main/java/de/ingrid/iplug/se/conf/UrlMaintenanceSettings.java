package de.ingrid.iplug.se.conf;

import java.util.List;

import de.ingrid.admin.object.Provider;

public class UrlMaintenanceSettings {
    
    public class IngridPartner {
        private final String _shortName;
        private final String _displayName;

        public IngridPartner(String shortName, String displayName) {
            _shortName = shortName;
            _displayName = displayName;
        }

        public String getShortName() {
            return _shortName;
        }

        public String getDisplayName() {
            return _displayName;
        }
        public List<Provider> provider;
        
        public List<Provider> getProvider() {
            return provider;
        }

        public void setProvider(List<Provider> provider) {
            this.provider = provider;
        }

        public String getName() {
            return getDisplayName();
        }
    }
    
    public class Options {
        public String id;
        
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String value;
    }
    
    public class UrlTypes {
        public String id;
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String name;
        
        public List<Options> options;
        
        public List<Options> getOptions() {
            return options;
        };
    }
    
    private List<IngridPartner> partner;
    
    private List<UrlTypes> types;

    public List<IngridPartner> getPartner() {
        return partner;
    }

    public void setPartner(List<IngridPartner> partner) {
        this.partner = partner;
    }

    public List<UrlTypes> getTypes() {
        return types;
    }

    public void setTypes(List<UrlTypes> types) {
        this.types = types;
    }

}
