package de.ingrid.admin.service;

import java.io.File;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.admin.IKeys;
import de.ingrid.ibus.client.BusClient;
import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.IBus;
import de.ingrid.utils.IPlug;

@Service
public class CommunicationService {

    protected static final Logger LOG = Logger.getLogger(CommunicationService.class);

    private final File _communicationFile;

    @Autowired
    public CommunicationService(final IPlug iPlug) throws Exception {
        _communicationFile = new File(System.getProperty(IKeys.COMMUNICATION));
        if (!_communicationFile.exists()) {
            LOG.warn("communication does not exist. please create one via ui setup.");
        }
        BusClientFactory.createBusClient(_communicationFile);
        getBusClient().setIPlug(iPlug);
    }

    public String getPeerName() {
        return getBusClient().getPeerName();
    }

    public boolean isConnected() {
        return getBusClient().allConnected();
    }

    public void start() throws Exception {
        getBusClient().start();
    }

    public void shutdown() throws Exception {
        getBusClient().shutdown();
    }

    public void restart() throws Exception {
        getBusClient().restart();
    }

    public IBus getIBus() {
        return getBusClient().getNonCacheableIBus();
    }

    public File getCommunicationFile() {
        return _communicationFile;
    }

    private BusClient getBusClient() {
        return BusClientFactory.getBusClient();
    }
}
