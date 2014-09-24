package de.ingrid.iplug.se.webapp.controller.instance;

import java.nio.file.Paths;

import de.ingrid.admin.controller.AbstractController;
import de.ingrid.iplug.se.SEIPlug;
import de.ingrid.iplug.se.webapp.container.Instance;

public abstract class InstanceController extends AbstractController {

    protected Instance getInstanceData(String name) {
        Instance instance = new Instance();
        instance.setName( name );
        instance.setWorkingDirectory( Paths.get( SEIPlug.conf.getInstancesDir(), name ).toString() );
        
        if (SEIPlug.conf.activeInstances.contains( name )) {
            instance.setIsActive( true );
            
        } else {
            instance.setIsActive( false );            
        }
    
        return instance;
    }
}
