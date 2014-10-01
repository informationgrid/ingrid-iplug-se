package de.ingrid.iplug.se.webapp.controller.instance;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.webapp.container.Instance;

public class InstanceController {

    public static Instance getInstanceData(String name) {
        // first check if the path really exists, in case we called an URL with a wrong parameter
        Path workPath = Paths.get( SEIPlug.conf.getInstancesDir(), name );
        if (!workPath.toFile().exists()) return null;
        
        Instance instance = new Instance();
        instance.setName( name );
        instance.setWorkingDirectory( workPath.toString() );
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
