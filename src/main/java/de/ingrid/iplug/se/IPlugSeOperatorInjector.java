package de.ingrid.iplug.se;

import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

public class IPlugSeOperatorInjector extends AbstractIPlugOperatorInjector {

    private PlugDescription _plugDescription;
	private IPlugSeOperatorFinder _operatorFinder;

	public IPlugSeOperatorInjector() {
		_operatorFinder = new IPlugSeOperatorFinder();
	}

    @Override
    public IPlugOperatorFinder createOperatorFinder() {
		return _operatorFinder;
    }

    @Override
    public void configure(PlugDescription plugDescription) {
        _plugDescription = plugDescription;
		_operatorFinder.configure(_plugDescription);
    }

}
