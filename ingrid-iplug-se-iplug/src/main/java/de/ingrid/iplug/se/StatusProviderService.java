package de.ingrid.iplug.se;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import de.ingrid.iplug.se.nutchController.StatusProvider;

@Service
public class StatusProviderService {

    private ConcurrentHashMap<String, StatusProvider> statusProviders;

    public StatusProvider getStatusProvider(String logdir) {
        if (statusProviders == null) {
            statusProviders = new ConcurrentHashMap<>();
        }
        if (!statusProviders.containsKey( logdir )) {
            statusProviders.put( logdir, new StatusProvider( logdir ) );
        }
        return statusProviders.get( logdir );
    }

}
