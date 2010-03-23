package de.ingrid.admin.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.iplug.HeartBeatPlug;
import de.ingrid.iplug.se.IKeys;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;

@Service
public class CommunicationService {

    protected static final Logger LOG = Logger.getLogger(CommunicationService.class);

    private final IPlug _iPlug;

    private final File _communicationFile;

    private boolean _error = false;

    @Autowired
    public CommunicationService(final IPlug iPlug) {
        _iPlug = iPlug;
        _communicationFile = new File(System.getProperty(IKeys.COMMUNICATION));
        if (!_communicationFile.exists()) {
            LOG.warn("communication does not exist. please create one via ui setup.");
        }
        getBusClient();
    }

    public String getPeerName() {
        final BusClient busClient = getBusClient();
        return busClient == null ? "no bus" : busClient.getPeerName();
    }

    public boolean isConnected() {
        final BusClient busClient = getBusClient();
        return busClient == null ? false : busClient.allConnected();
    }

    private void reconfigure() {
        if (_iPlug instanceof HeartBeatPlug) {
            ((HeartBeatPlug) _iPlug).reconfigure();
        }
    }

    public void start() {
        try {
            getBusClient().start();
            _error = false;
        } catch (final Exception e) {
            LOG.warn("some of the busses are not available");
            _error = true;
        }
        reconfigure();
    }

    public void shutdown() throws Exception {
        try {
            getBusClient().shutdown();
            _error = false;
        } catch (final Exception e) {
            LOG.warn("some of the busses are not available");
            _error = true;
        }
        reconfigure();
    }

    public void restart() {
        try {
            getBusClient().restart();
            _error = false;
        } catch (final Exception e) {
            LOG.warn("some of the busses are not available");
            _error = true;
        }
        reconfigure();
    }

    public IBus getIBus() {
        final BusClient busClient = getBusClient();
        return busClient == null ? null : busClient.getNonCacheableIBus();
    }

    public File getCommunicationFile() {
        return _communicationFile;
    }

    public boolean hasErrors() {
        return _communicationFile.exists() ? _error || !isConnected() : false;
    }

    private BusClient getBusClient() {
        BusClient busClient = BusClientFactory.getBusClient();
        if (busClient == null) {
            try {
                busClient = BusClientFactory.createBusClient(_communicationFile, _iPlug);
                _error = false;
            } catch (final Exception e) {
                LOG.warn("error creating bus client");
                _error = true;
            }
        }
        return busClient;
    }
}
