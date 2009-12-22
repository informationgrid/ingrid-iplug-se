package de.ingrid.admin;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(ContextListener.class);

    public void contextInitialized(final ServletContextEvent contextEvent) {
        // plug description
        String plugDescription = System.getProperty(IKeys.PLUG_DESCRIPTION);
		if (plugDescription == null) {
            plugDescription = "portalu-nutch-gui/conf/plugdescription-index.xml";
            System.setProperty(IKeys.PLUG_DESCRIPTION, plugDescription);
            LOG.warn("plug description is not defined. using default: " + plugDescription);
        }

        // communication
        String communication = System.getProperty(IKeys.COMMUNICATION);
        if (communication == null) {
            communication = "portalu-nutch-gui/conf/communication-index.xml";
            System.setProperty(IKeys.COMMUNICATION, communication);
            LOG.warn("commmunication is not defined. using default:" + communication);
        }
    }

    public void contextDestroyed(final ServletContextEvent contextEvent) {

    }

}
