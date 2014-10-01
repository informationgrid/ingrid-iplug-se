package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class LawDataType extends AbstractDataType {

    public LawDataType() {
        super("law");
        //setForceActive(true);
    }

}