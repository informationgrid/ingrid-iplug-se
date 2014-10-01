package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class WwwDataType extends AbstractDataType {

    public WwwDataType() {
        super("www");
        setForceActive(true);
    }

}