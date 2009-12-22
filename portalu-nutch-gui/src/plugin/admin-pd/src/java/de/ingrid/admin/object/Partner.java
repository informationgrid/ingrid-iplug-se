package de.ingrid.admin.object;

public class Partner {

    private final String _shortName;
    private final String _displayName;

    public Partner(String shortName, String displayName) {
        _shortName = shortName;
        _displayName = displayName;
    }

    public String getShortName() {
        return _shortName;
    }

    public String getDisplayName() {
        return _displayName;
    }

}
