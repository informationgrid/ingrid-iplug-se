package de.ingrid.iplug.se;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

public class IPlugSeOperatorInjector extends AbstractIPlugOperatorInjector {

    private PlugDescription _plugDescription;

    @Override
    public IPlugOperatorFinder createOperatorFinder() {
        IPlugOperatorFinder operatorFinder = new IPlugSeOperatorFinder();
        operatorFinder.configure(_plugDescription);
        return operatorFinder;
    }

    @Override
    public void configure(PlugDescription plugDescription) {
        _plugDescription = plugDescription;

    }

}
