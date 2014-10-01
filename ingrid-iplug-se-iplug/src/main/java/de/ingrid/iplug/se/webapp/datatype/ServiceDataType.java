package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class ServiceDataType extends AbstractDataType {

    public ServiceDataType() {
        super("service");
        //setForceActive(true);
    }

}