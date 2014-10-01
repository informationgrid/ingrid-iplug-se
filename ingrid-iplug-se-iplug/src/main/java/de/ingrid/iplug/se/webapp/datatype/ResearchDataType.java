package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class ResearchDataType extends AbstractDataType {

    public ResearchDataType() {
        super("research");
        //setForceActive(true);
    }

}