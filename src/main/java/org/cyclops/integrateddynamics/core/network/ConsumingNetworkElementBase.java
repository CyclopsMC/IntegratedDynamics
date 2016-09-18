package org.cyclops.integrateddynamics.core.network;

import org.cyclops.integrateddynamics.api.network.IEnergyConsumingNetworkElement;
import org.cyclops.integrateddynamics.api.network.INetwork;

/**
 * Base implementation for an energy consuming network element.
 * @author rubensworks
 */
public abstract class ConsumingNetworkElementBase extends NetworkElementBase implements IEnergyConsumingNetworkElement {

    @Override
    public int getConsumptionRate() {
        return 0;
    }

    @Override
    public void postUpdate(INetwork network, boolean updated) {

    }
}
