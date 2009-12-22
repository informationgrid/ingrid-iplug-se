package de.ingrid.admin;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.controller.AdminToolsController;
import de.ingrid.admin.service.PlugDescriptionService;
import de.ingrid.utils.IConfigurable;
import de.ingrid.utils.PlugDescription;

/**
 * The purpose of this class is to ensure that classes are configured correctly
 * during startup. Each class must implement IConfigurable so that its configure-
 * method can be called. 
 *
 */
@Service
public class Environment {

	/**
	 * All classes that implement IConfigurable will be ordered to execute their
	 * configure-method. This way all classes are configured (correctly) during
	 * startup.
	 * AdminToolsController will not be used here, but its actions during creation
	 * must be made before configurations can start.
	 * 
	 * @param adminToolsController, only used to preserve the bean creation order
	 * @param plugDescriptionService, which contains the PlugDescription
	 * @param configurables, classes that implement IConfigurable
	 * @throws IOException
	 */
    @Autowired
    public Environment(final AdminToolsController adminToolsController,
    		final PlugDescriptionService plugDescriptionService, 
    		final IConfigurable... configurables) throws IOException {
        
    	final PlugDescription plugDescription = plugDescriptionService.getPlugDescription();
        if (plugDescription != null) {
            for (final IConfigurable configurable : configurables) {
                configurable.configure(plugDescription);
            }
        }
    }
}
