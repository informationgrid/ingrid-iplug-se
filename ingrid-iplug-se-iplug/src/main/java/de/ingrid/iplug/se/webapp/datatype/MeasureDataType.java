package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class MeasureDataType extends AbstractDataType {

    public MeasureDataType() {
        super("measure");
        //setForceActive(true);
    }

}