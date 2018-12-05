package de.ingrid.iplug.se;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.nutchController.StatusProvider;

@Service
public class StatusProviderService {

    private ConcurrentHashMap<String, StatusProvider> statusProviders;

    public StatusProvider getStatusProvider(String logdir) {
        return getStatusProvider(logdir, "last_status.xml");
    }
    public StatusProvider getStatusProvider(String logdir, String statusFilename) {
        if (statusProviders == null) {
            statusProviders = new ConcurrentHashMap<>();
        }
        String mapKey = logdir + statusFilename;
        if (!statusProviders.containsKey( mapKey )) {
            statusProviders.put( mapKey, new StatusProvider( logdir, statusFilename ) );
        }
        return statusProviders.get( mapKey );
    }

}
