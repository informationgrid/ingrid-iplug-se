package de.ingrid.admin;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

public class ContextListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(ContextListener.class);

    public void contextInitialized(final ServletContextEvent contextEvent) {
        final File confPath = new File("conf");
        final File developConfPath = new File("portalu-nutch-gui/conf");
        // plug description
        String plugDescription = System.getProperty(IKeys.PLUG_DESCRIPTION);
        if (plugDescription == null) {
            // test, if we are running in development or installation
            // environment
            if (developConfPath.isDirectory()) {
                plugDescription = new File(developConfPath, "plugdescription-index.xml").getAbsolutePath();
            } else {
                plugDescription = new File(confPath, "plugdescription-index.xml").getAbsolutePath();
            }
            System.setProperty(IKeys.PLUG_DESCRIPTION, plugDescription);
            LOG.info("plug description is not defined. using default: " + plugDescription);
        }

        // communication
        String communication = System.getProperty(IKeys.COMMUNICATION);
        if (communication == null) {
            if (developConfPath.isDirectory()) {
                communication = new File(developConfPath, "communication-index.xml").getAbsolutePath();
            } else {
                communication = new File(confPath, "communication-index.xml").getAbsolutePath();
            }
            System.setProperty(IKeys.COMMUNICATION, communication);
            LOG.info("commmunication is not defined. using default:" + communication);
        }
    }

    public void contextDestroyed(final ServletContextEvent contextEvent) {

    }

}
