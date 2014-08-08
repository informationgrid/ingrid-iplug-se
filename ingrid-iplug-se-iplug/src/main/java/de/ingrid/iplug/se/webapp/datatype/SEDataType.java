package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class SEDataType extends AbstractDataType {

    public SEDataType() {
        super("se");
        setForceActive(true);
    }

}