package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class DefaultDataType extends AbstractDataType {

    public DefaultDataType() {
        super("default");
        //setForceActive(true);
    }

}