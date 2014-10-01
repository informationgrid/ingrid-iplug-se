package de.ingrid.iplug.se.webapp.datatype;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

@Service
public class TopicsDataType extends AbstractDataType {

    public TopicsDataType() {
        super("topics");
        //setForceActive(true);
    }

}