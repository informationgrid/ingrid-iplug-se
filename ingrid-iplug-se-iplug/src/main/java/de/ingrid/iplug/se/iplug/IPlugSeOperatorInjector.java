/*
 * **************************************************-
 * ingrid-iplug-se-iplug
 * ==================================================
 * Copyright (C) 2014 - 2022 wemove digital solutions GmbH
 * ==================================================
 * Licensed under the EUPL, Version 1.1 or – as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * 
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl5
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * **************************************************#
 */
package de.ingrid.iplug.se.iplug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.ingrid.ibus.client.BusClientFactory;
import de.ingrid.utils.PlugDescription;
import de.ingrid.utils.metadata.AbstractIPlugOperatorInjector;
import de.ingrid.utils.metadata.IPlugOperatorFinder;

@Service
public class IPlugSeOperatorInjector extends AbstractIPlugOperatorInjector {

    private PlugDescription _plugDescription;

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
