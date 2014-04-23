package de.ingrid.admin.object;

import org.springframework.stereotype.Service;

import de.ingrid.admin.object.AbstractDataType;

/**
 * At least one datatype is needed when using the base-webapp.
 * 
 * @author Andre
 *
 */

@Service
public class DefaultDataType extends AbstractDataType {

    public DefaultDataType() {
        super("indexer");
    }

}