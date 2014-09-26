package de.ingrid.iplug.se.webapp.controller.instance;

import java.nio.file.Paths;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.webapp.container.Instance;

public class InstanceController {

    public static Instance getInstanceData(String name) {
        Instance instance = new Instance();
        instance.setName( name );
        instance.setWorkingDirectory( Paths.get( SEIPlug.conf.getInstancesDir(), name ).toString() );
        instance.setIndexName( SEIPlug.conf.index );
        
        if (SEIPlug.conf.activeInstances.contains( name )) {
            instance.setIsActive( true );
            
        } else {
            instance.setIsActive( false );            
        }
        instance.setEsTransportTcpPort(SEIPlug.conf.esTransportTcpPort);
        instance.setEsHttpHost(SEIPlug.conf.esHttpHost);
        
    
        return instance;
    }
}
