package de.ingrid.iplug.se.iplug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.iplug.se.elasticsearch.bean.ElasticsearchNodeFactoryBean;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

@Service
public class IPlugSeOperatorInjector extends AbstractIPlugOperatorInjector {

    private PlugDescription _plugDescription;

    @Autowired
    private ElasticsearchNodeFactoryBean elasticSearch;

    @Autowired
    private IPlugSeOperatorFinder operatorFinder;

    @Override
    public IPlugOperatorFinder createOperatorFinder() {
        operatorFinder.configure(_plugDescription);
        return operatorFinder;
    }

    @Override
    public void configure(PlugDescription plugDescription) {
        _plugDescription = plugDescription;
        this.setIBus(BusClientFactory.getBusClient().getNonCacheableIBus());
    }

}
